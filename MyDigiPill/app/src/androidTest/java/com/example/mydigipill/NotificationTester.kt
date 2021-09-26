package com.example.mydigipill

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario.launch
import be.ugent.mydigipill.LoginActivity
import be.ugent.mydigipill.data.Alarm
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.notifications.NotificationMaker
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList


class NotificationTester {

    private val rg = Random()
    private fun getAlarmInOneMinute(): Alarm {
        val now = DateTime.now().plusMinutes(1)
        val today = now.dayOfWeek
        val hour = now.hourOfDay
        val minute = now.minuteOfHour
        val enable = true
        val weekdata = getToday(now.dayOfWeek - 1)
        val id = randomString()
        return Alarm(
            MutableLiveData(hour),
            MutableLiveData(minute),
            MutableLiveData(enable),
            MutableLiveData(weekdata),
            id
        )
    }

    private fun getToday(day: Int): MutableList<DayOfWeek> {
        val result = ArrayList<DayOfWeek>()
        val days = mutableListOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
        result.add(days[day])
        return result
    }

    private fun randomString(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val stringLength = rg.nextInt(30)
        return (1..stringLength)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun getRandomMedication(): Medication {
        val alarms = ArrayList<Alarm>()
        alarms.add(getAlarmInOneMinute())
        val name = randomString()
        val note = randomString()
        val intake = randomString()
        val id = randomString()
        val hasPhoto = rg.nextBoolean()
        return Medication(name, note, intake, alarms, null, id, null, null, hasPhoto)
    }

    @Test
    fun createNotificationWithNotificationMaker_ShouldBeVisible() {
        // Quick test to check if the notification does get handled by Android
        val scenario = launch(LoginActivity::class.java)
        val id = Random().nextInt()
        scenario.onActivity {
            val not = NotificationMaker().buildNotification(
                "hi",
                "hi",
                id,
                it,
                true,
                "dag"
            )
            val notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(id, not)
            sleep(500)
            var found: Boolean = false
            for (noti in notificationManager.activeNotifications) {
                if (noti.id == id) {
                    found = true
                    Assert.assertTrue(noti.isOngoing)
                }
            }
            Assert.assertTrue("Check if notification in statusbar", found)
        }
    }

    @Test
    fun createNotificationWithNotificationMaker_AfterCancelById_ShouldNotBeVisible() {
        val scenario = launch(LoginActivity::class.java)
        scenario.onActivity {
            val not = NotificationMaker().buildNotification(
                "hi",
                "hi",
                59,
                it,
                true,
                "dag"
            )
            val notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(59, not)
            notificationManager.cancel(59)
            var found: Boolean = false
            for (noti in notificationManager.activeNotifications) {
                if (noti.id == 59) {
                    found = true
                }
            }
            Assert.assertFalse("Check if notification in statusbar", found)
        }
    }

    /*
    @Test
    fun testIfAlarmManagerWorks() {
        val scenario = launch(LoginActivity::class.java)
        scenario.onActivity {
            val act = it
            val med = getRandomMedication()
            val next: Triple<Int, DateTime, Alarm?> =
                med.calculateFirstDayAndAlarm(DateTime.now()) //firstDay, firstAlarmTime, firstAlarm?
            val timeToSchedule = next.second.withSecondOfMinute(0).withMillisOfSecond(0).plusDays(next.first)
            NotificationHandler(act).createScheduledNotification(
                "Please take medication: ${med.name}",
                timeToSchedule,
                med.id!!,
                true
            )
            Assert.assertTrue("Check if notification in statusbar", found)
        }
    }
    */
}