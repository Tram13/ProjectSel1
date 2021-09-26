package be.ugent.mydigipill.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import be.ugent.mydigipill.receivers.NotificationReceiver

class AlarmMaker {

    private fun createAlarmIntent(
        context: Context,
        notification: Notification?,
        id: Int,
        stringID: String,
        alarmString: String,
        action: NotificationType
    ): Intent {
        val intent = Intent(context, NotificationReceiver::class.java)
        if (notification != null) { // When the intent is not created to cancel a previously set alarm
            intent.putExtra(NotificationReceiver.NOTIFICATION_ID, id)
            intent.putExtra(NotificationReceiver.NOTIFICATION, notification)
            intent.putExtra(NotificationReceiver.NOTIFICATION_STRINGID, stringID)
            intent.putExtra(NotificationReceiver.NOTIFICATION_ALARMSTRING, alarmString)
        }
        intent.action = action.toString()
        return intent
    }

    fun createPendingIntent(
        context: Context,
        notification: Notification?,
        id: Int,
        stringID: String,
        alarmString: String,
        action: NotificationType
    ): PendingIntent {
        val intent = createAlarmIntent(context, notification, id, stringID, alarmString, action)
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}