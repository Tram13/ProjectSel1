package be.ugent.mydigipill.receivers

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.MedicationDAO
import be.ugent.mydigipill.notifications.AlarmMaker
import be.ugent.mydigipill.notifications.NotificationMaker
import be.ugent.mydigipill.notifications.NotificationType
import be.ugent.mydigipill.notifications.TimeConverter
import com.google.firebase.auth.FirebaseAuth
import org.joda.time.DateTime


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationType.ALARM.toString() -> {
                // Received broadcast from Alarmmanager to create notification
                showNotification(context, intent)
            }
            NotificationType.SNOOZE.toString() -> {
                // Received broadcast from Notification to snooze current notification
                scheduleSnooze(context, intent)
            }
            "android.intent.action.BOOT_COMPLETED" -> {
                // Reschedule all alarms after a reboot
                scheduleOnBoot(context)
            }
        }
    }

    private fun showNotification(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification =
            intent.getParcelableExtra<Notification>(NOTIFICATION)
        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        notificationManager.notify(id, notification)
    }

    private fun scheduleSnooze(context: Context, intent: Intent) {
        // creating snooze reminder
        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        val stringID = intent.getStringExtra(NOTIFICATION_STRINGID)!!
        val alarmString = intent.getStringExtra(NOTIFICATION_ALARMSTRING)!!
        val notification: Notification = createNewNotification(context, id, stringID, alarmString)
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        val snoozedTimeDelta: Int = prefs.getInt(context.getString(R.string.snooze_time), 1)
        val snoozedTime = DateTime.now().plusMinutes(snoozedTimeDelta)
        val pendingIntent = AlarmMaker().createPendingIntent(
            context,
            notification,
            id,
            stringID,
            alarmString,
            NotificationType.ALARM
        )

        // Close current notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)

        // Schedule snoozed reminder
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            TimeConverter().convert(snoozedTime).timeInMillis,
            pendingIntent
        )
    }

    private fun createNewNotification(context: Context, id: Int, stringID: String, alarmString: String): Notification {
        // Create notification to display after snooze
        return NotificationMaker().buildNotification(
            context.getString(R.string.please_take_medication, ""),
            stringID,
            id,
            context,
            false,
            alarmString
        )
    }

    private fun scheduleOnBoot(context: Context) {
        if (!FirebaseAuth.getInstance().currentUser?.uid.isNullOrBlank()) { // If there is a logged in user
            MedicationDAO().scheduleOnBoot(context)
        }
    }

    companion object {
        const val NOTIFICATION_ID = "notification-id"
        const val NOTIFICATION = "notification"
        const val REMINDER_CHANNEL = "REMINDER_CHANNEL"
        const val NOTIFICATION_STRINGID = "notification-stringid"
        const val NOTIFICATION_ALARMSTRING = "notification-alarmstring"
        private const val TAG = "NotificationReceiver"
    }
}