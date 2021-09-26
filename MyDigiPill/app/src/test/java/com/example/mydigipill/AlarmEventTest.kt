package com.example.mydigipill

import be.ugent.mydigipill.data.AlarmEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AlarmEventTest {

    @Test
    fun alarmEventCreate_shouldPutTheValuesCorrectIntoTheWeekViewEvent() {
        val id: Long = 1
        val title = "test"
        val start = GregorianCalendar()
        val end = GregorianCalendar()
        end.add(Calendar.HOUR, 1)
        val location = "somewhere"
        val isAllDay = false
        val alarmEvent = AlarmEvent(id, title, start, end, location, isAllDay)
        val weekViewEvent = alarmEvent.toWeekViewEvent()

        assertEquals(weekViewEvent.id, id)
        assertEquals(weekViewEvent.title, title)
        assertEquals(weekViewEvent.startTime, start)
        assertEquals(weekViewEvent.endTime, end)
        assertEquals(weekViewEvent.location, location)
        assertEquals(weekViewEvent.isAllDay, isAllDay)
    }
}