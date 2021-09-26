package com.example.mydigipill

import androidx.lifecycle.MutableLiveData
import be.ugent.mydigipill.data.Alarm
import be.ugent.mydigipill.data.Medication
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.DayOfWeek
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class MedicationTest {

    private fun createAlarm(
        hour: Int,
        min: Int,
        enabled: Boolean,
        weekData: MutableList<DayOfWeek>
    ): Alarm {
        return Alarm(
            MutableLiveData(hour), MutableLiveData(min), MutableLiveData(enabled), MutableLiveData(
                weekData
            ), null
        )
    }

    //voor als er geen vroeger alarm is
    private val noAlarmInt = Int.MAX_VALUE
    private val noAlarmDateTime =
        DateTime(
            9999,
            1,
            1,
            0,
            0
        )

    private val intToDayOfWeek = hashMapOf(
        0 to DayOfWeek.MONDAY,
        1 to DayOfWeek.TUESDAY,
        2 to DayOfWeek.WEDNESDAY,
        3 to DayOfWeek.THURSDAY,
        4 to DayOfWeek.FRIDAY,
        5 to DayOfWeek.SATURDAY,
        6 to DayOfWeek.SUNDAY
    )

    /**
     *   mock the returnresult for getNextIngestion of a medication
     */
    private fun getNextIngestionResult(
        med: Medication,
        now: DateTime
    ): Optional<Triple<Int, Int, Int>> {
        val resultBig = med.calculateFirstDayAndAlarm(now)
        return resultBig.third?.let {
            val result = resultBig.second.withDayOfWeek(1).plusDays(resultBig.first - 1)
                .minusHours(now.hourOfDay)
                .minusMinutes(now.minuteOfHour)
            Optional.of(
                Triple(
                    result.dayOfWeek.rem(7),
                    result.hourOfDay,
                    result.minuteOfHour
                )
            )
        } ?: Optional.empty()
    }

    @Test
    fun medication_UpdateAlarmWithSoonerAlarmSameDayShouldReturnSoonerAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        val today = intToDayOfWeek[today15h00.dayOfWeek - 1]!!

        //create alarm 12h12 today
        val alarmH = 12
        val alarmM = 12
        val alarm12h12Today = createAlarm(alarmH, alarmM, true, arrayListOf(today))

        //create the medication with dummy values for non alarm fields
        val med = Medication(
            null,
            null,
            null,
            arrayListOf(alarm12h12Today),
            null,
            null,
            null
        )
        // alarm is voor vandaag 12h12
        // meest recente vandaag om 15h00
        // now = vandaag 10h00
        val result = med.getSoonerAlarm(
            today,
            0,
            today15h00,
            today15h00.withHourOfDay(10),
            alarm12h12Today
        )

        // firstDay moet 0 zijn want het megegeven alarm is recenter en vandaag
        assertEquals(0, result.get().first)
        // juiste uur en minuut van nieuw alarm
        assertEquals(alarmH, result.get().second.hourOfDay)
        assertEquals(alarmM, result.get().second.minuteOfHour)
    }

    @Test
    fun medication_UpdateAlarmWithLaterAlarmSameDayShouldReturnEmptyTriple() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        val today = intToDayOfWeek[today15h00.dayOfWeek - 1]!!

        //create alarm 18h10 today
        val alarmH = 18
        val alarmM = 10
        val alarm18h10Today = createAlarm(alarmH, alarmM, true, arrayListOf(today))

        //create the medication with dummy values for non alarm fields
        val med = Medication(
            null,
            null,
            null,
            arrayListOf(alarm18h10Today),
            null,
            null,
            null
        )
        // alarm is voor vandaag 18h10
        // meest recente vandaag om 15h00
        // now = vandaag 05h00
        val result = med.getSoonerAlarm(
            today,
            0,
            today15h00,
            today15h00.withHourOfDay(5),
            alarm18h10Today
        )

        // result moet leeg zijn want het megegeven alarm is niet recenter
        assert(!result.isPresent)
    }

    @Test
    fun medication_UpdateAlarmWithSoonerAlarmOtherDayShouldReturnSoonerAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        val todayPlus2at15h00 =
            today15h00.withDayOfWeek(1 + Math.floorMod(today15h00.dayOfWeek + 1, 7))

        val tomorrow = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek, 7)]!!

        //create alarm 15h45 tomorrow
        val alarmH = 15
        val alarmM = 45
        val alarm15h45Tomorrow = createAlarm(alarmH, alarmM, true, arrayListOf(tomorrow))

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                arrayListOf(alarm15h45Tomorrow),
                null,
                null,
                null
            )
        // alarm is voor morgen 15h45
        // meest recente overmorgen om 15h00
        // now = vandaag 18h00
        val result = med.getSoonerAlarm(
            tomorrow,
            2,
            todayPlus2at15h00,
            today15h00.withHourOfDay(18),
            alarm15h45Tomorrow
        )

        // firstDay moet 1 zijn want het megegeven alarm is recenter en morgen
        assertEquals(1, result.get().first)
        // juiste uur en minuut van nieuw alarm
        assertEquals(alarmH, result.get().second.hourOfDay)
        assertEquals(alarmM, result.get().second.minuteOfHour)
    }

    @Test
    fun medication_UpdateAlarmWithLaterAlarmOtherDayShouldReturnEmptyTriple() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        val todayPlus2at15h00 =
            today15h00.withDayOfWeek(1 + Math.floorMod(today15h00.dayOfWeek + 1, 7))

        val todayPlus3 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 2, 7)]!!

        //create alarm 14h02 in 3 days
        val alarmH = 14
        val alarmM = 2
        val alarm14h02In3Days = createAlarm(alarmH, alarmM, true, arrayListOf(todayPlus3))

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                arrayListOf(alarm14h02In3Days),
                null,
                null,
                null
            )
        // alarm is voor binnen 3 dagen om 14h02
        // meest recente overmorgen om 15h00
        // now = vandaag 18h00
        val result = med.getSoonerAlarm(
            todayPlus3,
            2,
            todayPlus2at15h00,
            today15h00.withHourOfDay(18),
            alarm14h02In3Days
        )

        // result moet leeg zijn want het megegeven alarm is niet recenter
        assert(!result.isPresent)
    }

    @Test
    fun medication_UpdateAlarmFirstTimeShouldAlwaysReturnAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        //create alarm always at 12h12 (doesn't matter here)
        val alarmH = 12
        val alarmM = 12
        for (i in 0..6) {
            // set the day and alarm
            val day = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek - 1 + i, 7)]!!
            val alarm12h12 = createAlarm(alarmH, alarmM, true, arrayListOf(day))

            //create the medication with dummy values for non alarm fields
            val medication = Medication(
                null,
                null,
                null,
                arrayListOf(alarm12h12),
                null,
                null,
                null
            )

            // alarm is voor dag i om 12h12
            // meest recente is er niet
            // now = vandaag 10h00
            val result = medication.getSoonerAlarm(
                day,
                noAlarmInt,
                noAlarmDateTime,
                today15h00.withHourOfDay(10),
                alarm12h12
            )

            // firstDay moet i zijn want het meegegeven alarm is altijd recenter
            assertEquals(i, result.get().first)
        }
    }

    @Test
    fun medication_UpdateAlarmFirstTimeTodayButPassedShouldReturnSoonerAlarmIn7Days() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        val today = intToDayOfWeek[today15h00.dayOfWeek - 1]!!

        //create alarm 10h00 today
        val alarmH = 10
        val alarmM = 0
        val alarm10h00Today = createAlarm(alarmH, alarmM, true, arrayListOf(today))

        //create the medication with dummy values for non alarm fields
        val med = Medication(
            null,
            null,
            null,
            arrayListOf(alarm10h00Today),
            null,
            null,
            null
        )
        // alarm is voor vandaag 10h00
        // meest recente is er niet
        // now = vandaag 15h00
        val result = med.getSoonerAlarm(
            today,
            noAlarmInt,
            noAlarmDateTime,
            today15h00,
            alarm10h00Today
        )

        // firstDay moet 7 zijn want het meegegeven alarm is vandaag maar al voorbij
        assertEquals(7, result.get().first)
        // juiste uur en minuut van nieuw alarm
        assertEquals(alarmH, result.get().second.hourOfDay)
        assertEquals(alarmM, result.get().second.minuteOfHour)
    }

    @Test
    fun medication_UpdateAlarmWithPassedAlarmTodayShouldReturnEmptyTriple() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        val todayPlus2at15h00 =
            today15h00.withDayOfWeek(1 + Math.floorMod(today15h00.dayOfWeek + 1, 7))

        val today = intToDayOfWeek[today15h00.dayOfWeek - 1]!!

        //create alarm 10h00 today
        val alarmH = 10
        val alarmM = 0
        val alarm10h00Today = createAlarm(alarmH, alarmM, true, arrayListOf(today))

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                arrayListOf(alarm10h00Today),
                null,
                null,
                null
            )
        // alarm is voor vandaag om 10h00 => binnen 7 dagen
        // meest recente overmorgen om 15h00
        // now = vandaag 18h00
        val result = med.getSoonerAlarm(
            today,
            2,
            todayPlus2at15h00,
            today15h00.withHourOfDay(18),
            alarm10h00Today
        )

        // result moet leeg zijn want het megegeven alarm is niet recenter
        assert(!result.isPresent)
    }


    /////////////////////////////////////////////////////////////////////////////////////


    @Test
    fun calculateFirstDayAndAlarm_MedicationWithNoAlarmsShouldReturnNullAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                arrayListOf(),
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be null
        assertEquals(null, result.third)

    }

    @Test
    fun calculateFirstDayAndAlarm_MedicationWithOneAlarmShouldReturnAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        // day
        val todayPlus3 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 2, 7)]!!

        //create Alarms
        val alarm10h00TodayPlus3 = createAlarm(10, 0, true, arrayListOf(todayPlus3))

        //create list of all the alarms
        val alarmlist = arrayListOf(alarm10h00TodayPlus3)

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                alarmlist,
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be the only one present withing 3 days
        assertEquals(3, result.first)
        assertEquals(alarm10h00TodayPlus3, result.third)
    }

    @Test
    fun calculateFirstDayAndAlarm_MedicationWithMultipleAlarmsSameHourDiffDayShouldReturnFirstAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        // day
        val todayPlus1 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek, 7)]!!
        val todayPlus3 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 2, 7)]!!
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!
        val todayPlus6 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 5, 7)]!!

        //create Alarms
        val alarm10h00TodayPlus1 = createAlarm(10, 0, true, arrayListOf(todayPlus1))
        val alarm10h00TodayPlus3 = createAlarm(10, 0, true, arrayListOf(todayPlus3))
        val alarm10h00TodayPlus4 = createAlarm(10, 0, true, arrayListOf(todayPlus4))
        val alarm10h00TodayPlus6 = createAlarm(10, 0, true, arrayListOf(todayPlus6))

        //create list of all the alarms
        val alarmlist = arrayListOf(
            alarm10h00TodayPlus1,
            alarm10h00TodayPlus3,
            alarm10h00TodayPlus4,
            alarm10h00TodayPlus6
        )

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                alarmlist,
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be the one tomorrow at 10h00
        assertEquals(1, result.first)
        assertEquals(alarm10h00TodayPlus1, result.third)
    }

    @Test
    fun calculateFirstDayAndAlarm_MedicationWithMultipleAlarmsDifferentHourSameDayShouldReturnFirstAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        //day
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!

        //create Alarms
        val alarm11h00TodayPlus4 = createAlarm(11, 0, true, arrayListOf(todayPlus4))
        val alarm15h04TodayPlus4 = createAlarm(15, 4, true, arrayListOf(todayPlus4))
        val alarm19h50TodayPlus4 = createAlarm(19, 50, true, arrayListOf(todayPlus4))
        val alarm15h40TodayPlus4 = createAlarm(15, 40, true, arrayListOf(todayPlus4))

        //create list of all the alarms
        val alarmlist = arrayListOf(
            alarm11h00TodayPlus4,
            alarm15h04TodayPlus4,
            alarm19h50TodayPlus4,
            alarm15h40TodayPlus4
        )

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                alarmlist,
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be the one in 4 days at 11h00
        assertEquals(4, result.first)
        assertEquals(alarm11h00TodayPlus4, result.third)
    }

    @Test
    fun calculateFirstDayAndAlarm_MedicationWithMultipleAlarmsDifferentHourDifferentDayShouldReturnFirstAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        // day
        val today = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek - 1, 7)]!!
        val todayPlus1 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek, 7)]!!
        val todayPlus3 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 2, 7)]!!
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!
        val todayPlus6 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 5, 7)]!!

        //create Alarms
        val alarm10h00TodayPassed = createAlarm(10, 0, true, arrayListOf(today, todayPlus4))
        val alarm23h50TodayPlus1 =
            createAlarm(23, 50, true, arrayListOf(todayPlus1, todayPlus3, todayPlus6))
        val alarm17h04TodayPlus3 = createAlarm(17, 4, true, arrayListOf(todayPlus3))
        val alarm02h12TodayPlus4 = createAlarm(2, 12, true, arrayListOf(todayPlus4))
        val alarm15h32TodayPlus6 = createAlarm(15, 32, true, arrayListOf(todayPlus6))

        //create list of all the alarms
        val alarmlist = arrayListOf(
            alarm10h00TodayPassed,
            alarm23h50TodayPlus1,
            alarm17h04TodayPlus3,
            alarm02h12TodayPlus4,
            alarm15h32TodayPlus6
        )

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                alarmlist,
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be the one tomorrow at 23h50
        assertEquals(1, result.first)
        assertEquals(alarm23h50TodayPlus1, result.third)
    }

    @Test
    fun calculateFirstDayAndAlarm_MedicationWithMultipleAlarmsWithDisabledShouldReturnNoneDisabledFirstAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        // day
        val today = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek - 1, 7)]!!
        val todayPlus1 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek, 7)]!!
        val todayPlus3 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 2, 7)]!!
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!
        val todayPlus6 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 5, 7)]!!

        //create Alarms
        val alarm10h00TodayPassed = createAlarm(10, 0, false, arrayListOf(today, todayPlus4))
        val alarm23h50TodayPlus1 =
            createAlarm(23, 50, false, arrayListOf(todayPlus1, todayPlus3, todayPlus6))
        val alarm17h04TodayPlus3 = createAlarm(17, 4, true, arrayListOf(todayPlus3))
        val alarm02h12TodayPlus4 = createAlarm(2, 12, true, arrayListOf(todayPlus4))
        val alarm15h32TodayPlus6 = createAlarm(15, 32, false, arrayListOf(todayPlus6))

        //create list of all the alarms
        val alarmlist = arrayListOf(
            alarm10h00TodayPassed,
            alarm23h50TodayPlus1,
            alarm17h04TodayPlus3,
            alarm02h12TodayPlus4,
            alarm15h32TodayPlus6
        )

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                alarmlist,
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be the one tomorrow at 23h50
        assertEquals(3, result.first)
        assertEquals(alarm17h04TodayPlus3, result.third)
    }

    @Test
    fun calculateFirstDayAndAlarm_MedicationWithOnlyDisabledShouldReturnNullAlarm() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        //day
        val todayPlus2 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 1, 7)]!!
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!
        val todayPlus6 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 5, 7)]!!

        //create Alarms all disabled!
        val alarm11h00TodayPlus4 = createAlarm(11, 0, false, arrayListOf(todayPlus2))
        val alarm15h04TodayPlus4 = createAlarm(15, 4, false, arrayListOf(todayPlus4, todayPlus2))
        val alarm19h50TodayPlus4 = createAlarm(19, 50, false, arrayListOf(todayPlus4, todayPlus6))
        val alarm15h40TodayPlus4 =
            createAlarm(15, 40, false, arrayListOf(todayPlus4, todayPlus2, todayPlus6))

        //create list of all the alarms
        val alarmlist = arrayListOf(
            alarm11h00TodayPlus4,
            alarm15h04TodayPlus4,
            alarm19h50TodayPlus4,
            alarm15h40TodayPlus4
        )

        //create the medication with dummy values for non alarm fields
        val med =
            Medication(
                null,
                null,
                null,
                alarmlist,
                null,
                null,
                null
            )

        val result = med.calculateFirstDayAndAlarm(today15h00)

        //result it's alarm should be null because no alarms are enabled
        assertEquals(null, result.third)
    }

    /*
    @Test
    fun compareMedicationASmallerOrBiggerThenB() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        //day
        val todayPlus1 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek, 7)]!!
        val todayPlus2 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 1, 7)]!!
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!
        val todayPlus6 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 5, 7)]!!

        //create Alarms
        val alarm16h00TodayPlus1Disabled = createAlarm(16,0,false, arrayListOf(todayPlus1))
        val alarm11h00TodayPlus2 = createAlarm(11, 0, true, arrayListOf(todayPlus2))
        val alarm19h50TodayPlus4and6 = createAlarm(19, 50, true, arrayListOf(todayPlus4, todayPlus6))
        val alarm15h40TodayPlus2and4and6Disabled =
            createAlarm(15, 40, false, arrayListOf(todayPlus4, todayPlus2, todayPlus6))

        //create list of all the alarms
        val alarmlistSmall = arrayListOf(
            alarm11h00TodayPlus2,
            alarm15h40TodayPlus2and4and6Disabled
        )

        val alarmlistBig = arrayListOf(
            alarm16h00TodayPlus1Disabled,
            alarm19h50TodayPlus4and6
        )

        //create the small & big medication
        val medSmaller = mock<Medication>()
        val medBig = mock<Medication>()

        whenever(medSmaller.alarms).thenReturn(alarmlistSmall)
        whenever(medBig.alarms).thenReturn(alarmlistBig)

        whenever(medSmaller.getNextIngestion()).thenReturn(getNextIngestionResult(medSmaller, today15h00))
        whenever(medBig.getNextIngestion()).thenReturn(getNextIngestionResult(medBig, today15h00))

        // A smaller then B -> -1
        assertEquals(-1, medSmaller.compareTo(medBig))

        // A bigger then B -> 1
        assertEquals(1, medBig.compareTo(medSmaller))

        // A is not equal to B -> not 0
        assertNotEquals(0, medBig.compareTo(medSmaller))
        assertNotEquals(0, medSmaller.compareTo(medBig))
    }

    @Test
    fun compareMedicationAequalsB() {
        //datetime
        val today15h00 = DateTime.now().withHourOfDay(15)
            .withMinuteOfHour(0)

        //day
        val todayPlus1 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek, 7)]!!
        val todayPlus2 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 1, 7)]!!
        val todayPlus4 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 3, 7)]!!
        val todayPlus6 = intToDayOfWeek[Math.floorMod(today15h00.dayOfWeek + 5, 7)]!!

        //create Alarms
        val alarm16h00TodayPlus1Disabled = createAlarm(16,0,false, arrayListOf(todayPlus1))
        val alarm11h00TodayPlus2 = createAlarm(11, 0, true, arrayListOf(todayPlus2))
        val alarm15h04TodayPlus2and4 = createAlarm(15, 4, true, arrayListOf(todayPlus4, todayPlus2))
        val alarm19h50TodayPlus4and6 = createAlarm(19, 50, true, arrayListOf(todayPlus4, todayPlus6))
        val alarm15h40TodayPlus2and4and6Disabled =
            createAlarm(15, 40, false, arrayListOf(todayPlus4, todayPlus2, todayPlus6))

        //create list of all the alarms
        val alarmlistSmall = arrayListOf(
            alarm11h00TodayPlus2,
            alarm15h40TodayPlus2and4and6Disabled
        )

        val alarmlistBig = arrayListOf(
            alarm16h00TodayPlus1Disabled,
            alarm11h00TodayPlus2,
            alarm15h04TodayPlus2and4,
            alarm19h50TodayPlus4and6
        )

        //create the small & big medication
        val medSmaller = mock<Medication>()
        val medBig = mock<Medication>()

        whenever(medSmaller.alarms).thenReturn(alarmlistSmall)
        whenever(medBig.alarms).thenReturn(alarmlistBig)

        whenever(medSmaller.getNextIngestion()).thenReturn(getNextIngestionResult(medSmaller, today15h00))
        whenever(medBig.getNextIngestion()).thenReturn(getNextIngestionResult(medBig, today15h00))

        // A == B should result in 0
        assertEquals(0, medSmaller.compareTo(medBig))
        assertEquals(0, medBig.compareTo(medSmaller))
    }

     */
}