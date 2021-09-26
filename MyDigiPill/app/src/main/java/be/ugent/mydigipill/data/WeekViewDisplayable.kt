package be.ugent.mydigipill.data

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.util.*


data class AlarmEvent(
    val id: Long,
    val title: String,
    val startTime: Calendar,
    val endTime: Calendar,
    val location: String,
    val isAllDay: Boolean
) : WeekViewDisplayable<AlarmEvent> {

    /** This function is used to change an
     * @see AlarmEvent to an
     * @see WeekViewEvent, this will be displayed in the weekview
     */
    override fun toWeekViewEvent(): WeekViewEvent<AlarmEvent> {
        val style = WeekViewEvent.Style.Builder()
            .build()
        return WeekViewEvent.Builder(this)
            .setId(id)
            .setTitle(title)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setLocation(location)
            .setAllDay(isAllDay)
            .setStyle(style)
            .build()
    }

}