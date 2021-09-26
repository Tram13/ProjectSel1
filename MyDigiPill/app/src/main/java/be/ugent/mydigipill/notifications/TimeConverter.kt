package be.ugent.mydigipill.notifications

import android.icu.util.Calendar
import org.joda.time.DateTime

class TimeConverter {
    fun convert(time: DateTime): Calendar {
        // Function to convert JodaTime to build-in Android Calendar Time
        //This function is used to later convert the Calendar object to millis
        return Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time.hourOfDay)
            set(Calendar.MINUTE, time.minuteOfHour)
            set(Calendar.DAY_OF_MONTH, time.dayOfMonth)
            set(Calendar.MONTH, time.monthOfYear - 1)
            set(Calendar.YEAR, time.year)
            set(Calendar.SECOND, 0)
        }
    }
}