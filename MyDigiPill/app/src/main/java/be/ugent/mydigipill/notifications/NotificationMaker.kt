package be.ugent.mydigipill.notifications

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import be.ugent.mydigipill.MainActivity
import be.ugent.mydigipill.R
import be.ugent.mydigipill.receivers.NotificationReceiver

class NotificationMaker {
    fun buildNotification(
        message: String,
        stringID: String,
        id: Int,
        context: Context,
        allowSnooze: Boolean,
        alarmString: String
    ): Notification {
        val pendingIntent: PendingIntent? =
            createPendingIntent(context, id, stringID, alarmString)
        val builder = NotificationCompat.Builder(context, NotificationReceiver.REMINDER_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOngoing(true)
        if (allowSnooze) {
            builder.addAction(0, "snooze", createSnoozePendingIntent(context, id, stringID))
        }
        return builder.build()
    }

    private fun createSnoozePendingIntent(
        context: Context,
        id: Int,
        stringID: String
    ): PendingIntent {
        val intent = createSnoozeIntent(context, id, stringID)
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createSnoozeIntent(context: Context, id: Int, stringID: String): Intent {
        val snoozeIntent = Intent(context, NotificationReceiver::class.java)
        snoozeIntent.action = NotificationType.SNOOZE.toString()
        snoozeIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, id)
        snoozeIntent.putExtra(NotificationReceiver.NOTIFICATION_STRINGID, stringID)
        return snoozeIntent
    }

    private fun createPendingIntent(
        context: Context,
        id: Int,
        stringID: String,
        alarmString: String
    ): PendingIntent? {
        val intent = createNotificationIntent(context, stringID, alarmString, id)
        return TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(intent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun createNotificationIntent(
        context: Context,
        stringID: String,
        alarmString: String,
        id: Int
    ): Intent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(
            NotificationReceiver.NOTIFICATION_STRINGID,
            stringID
        ) // Used in MainActivity.processIntent() and NotificationReceiver.scheduleSnooze()
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, id)
        intent.putExtra(NotificationReceiver.NOTIFICATION_ALARMSTRING, alarmString)
        return intent
    }
}