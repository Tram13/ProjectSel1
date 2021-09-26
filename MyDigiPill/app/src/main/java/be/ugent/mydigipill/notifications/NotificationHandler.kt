package be.ugent.mydigipill.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Build
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Alarm
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.data.MedicationDAO
import be.ugent.mydigipill.receivers.NotificationReceiver.Companion.REMINDER_CHANNEL
import org.joda.time.DateTime
import org.json.JSONObject

enum class NotificationType {
    ALARM,
    SNOOZE;

    override fun toString(): String {
        return this.ordinal.toString()
    }
}

class NotificationHandler(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private val stringToIntMap: MutableMap<String, Int> = loadMap()
    private var mapCounter = loadCounter()

    private fun createNotificationChannel() {
        // Must be called in onCreate()!
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                REMINDER_CHANNEL,
                context.getString(R.string.notification_channelname),
                importance
            ).apply {
                description = context.getString(R.string.notification_channeldescription)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAllNotifications(medList: MutableList<Medication>) {
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        val snooze: Boolean = prefs.getBoolean(
            context.getString(R.string.snooze),
            true
        ) // If the snooze button should be shown in the notification
        for (med in medList) {
            // Schedule for the first occuring alarm of each medication
            val now: DateTime = DateTime.now().withHourOfDay(
                Calendar.getInstance()
                    .get(Calendar.HOUR_OF_DAY) // Because timezones don't really work in emulators
            )
            val next: Triple<Int, DateTime, Alarm?> =
                med.calculateFirstDayAndAlarm(now) //firstDay, firstAlarmTime, firstAlarm?
            if (next.third != null) { // An alarm can be scheduled
                val timeToSchedule = next.second.withSecondOfMinute(0).withMillisOfSecond(0).plusDays(next.first)
                val notTakenAction: () -> Unit = {
                    createScheduledNotification(
                        "Please take medication: ${med.name}", //TODO: R.Strings
                        timeToSchedule,
                        med.id!!,
                        snooze
                    )
                }
                MedicationDAO().getAlarmStatistics(timeToSchedule.toString(), med.id!!, notTakenAction) // Checking if the user didn't already take their medication for this alarm, if not, schedule the notification
            }
        }
        // After scheduling all notifications, save the mapping of the IDs
        saveMap()
        saveCounter()
    }

    private fun createScheduledNotification(
        content: String,
        time: DateTime,
        id: String,
        snooze: Boolean
    ) {
        val notification: Notification =
            NotificationMaker().buildNotification(content, id, toInt(id), context, snooze, time.toString())
        scheduleNotification(notification, time, id)
    }


    private fun scheduleNotification(notification: Notification, time: DateTime, stringID: String) {
        val pendingIntent =
            AlarmMaker().createPendingIntent(
                context,
                notification,
                toInt(stringID),
                stringID,
                time.toString(),
                NotificationType.ALARM
            )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            TimeConverter().convert(time).timeInMillis,
            pendingIntent
        )
    }

    fun cancelNotification(medication: Medication) { // Will cancel the scheduled notification for the given Medicaton
        val pendingIntent = AlarmMaker().createPendingIntent(
            context,
            null,
            toInt(medication.id!!),
            medication.id!!,
            "",
            NotificationType.ALARM
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent) // Cancelling alarm
        stringToIntMap.remove(medication.id!!) // Clean up the hashmap, so it doesn't get too cluttered
        saveMap() // save edited hashmap
        // Counter hasn't changed, so doesn't need to be updated
    }

    // HULPFUNCTIES //

    fun toInt(id: String): Int { // Create 1 on 1 mapping of String to Int
        // This is necessary because Android Alarm Manager uses Int as ID while FireStore uses String as ID
        var i: Int = stringToIntMap.getOrDefault(id, -1)
        if (i == -1) {
            stringToIntMap[id] = mapCounter // If the ID-mapping didn't exist yet, create it
            i = mapCounter
            if (mapCounter == Int.MAX_VALUE) { // We assume a user will never get to this limit anyway, and if they do, they won't have 2.147.483.647 medications
                mapCounter = 0
            } else {
                ++mapCounter
            }
        }
        return i
    }

    private fun loadMap(): MutableMap<String, Int> { // Restore the saved JSON and convert it to a map
        val stringToIntMap: MutableMap<String, Int> = HashMap() // Default init as empty map
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        val jsonString: String? = prefs.getString(
            prefsMapName,
            JSONObject().toString()
        )

        if (!jsonString.isNullOrBlank()) {
            val jsonObject = JSONObject(jsonString)
            val keysItr = jsonObject.keys()
            while (keysItr.hasNext()) {
                val key = keysItr.next() as String
                val value = jsonObject[key] as Int
                stringToIntMap[key] = value
            }
        }
        return stringToIntMap
    }

    private fun saveMap() {
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        if (prefs != null) {
            val jsonObject = JSONObject(stringToIntMap as Map<*, *>)
            val jsonString = jsonObject.toString()
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putString(prefsMapName, jsonString) // Overwrite old saved map
            editor.apply()
        }
    }

    private fun loadCounter(): Int { // https://developer.android.com/training/data-storage/shared-preferences
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        return prefs.getInt(prefsCounterName, 0)
    }

    private fun saveCounter() { // https://developer.android.com/training/data-storage/shared-preferences
        val sharedPref = context.getSharedPreferences(
            context.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        ) ?: return
        with(sharedPref.edit()) {
            putInt(prefsCounterName, mapCounter)
            apply()
        }
    }

    private companion object {
        private const val TAG = "NotificationHandler"
        private const val prefsMapName = "stringToIntMap"
        private const val prefsCounterName = "mapCounter"
    }
}