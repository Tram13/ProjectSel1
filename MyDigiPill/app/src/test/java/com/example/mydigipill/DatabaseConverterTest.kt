package com.example.mydigipill

import androidx.lifecycle.MutableLiveData
import be.ugent.mydigipill.data.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList

@RunWith(MockitoJUnitRunner::class)
class DatabaseConverterTest {

    // Setting up all the required variables for this test
    private val converter = DatabaseConverter()
    private val rg = Random()
    val filledInMedication1: Medication = Medication(
        "Paracetamol",
        "Bij eten",
        "Oraal, met water",
        ArrayList(),
        null,
        "medication1",
        null,
        null,
        false
    )

    val filledInMedicationUnwrapped1: MedicationUnwrapped = MedicationUnwrapped(
        "Paracetamol",
        "Bij eten",
        "Oraal, met water",
        ArrayList(),
        null,
        false,
        "medication1"
    )

    private val filledInAlarm1 = Alarm(
        MutableLiveData(10),
        MutableLiveData(20),
        MutableLiveData(true),
        MutableLiveData(mutableListOf(DayOfWeek.FRIDAY)),
        "alarm1"
    )

    private val filledInAlarm2 = Alarm(
        MutableLiveData(10),
        MutableLiveData(30),
        MutableLiveData(true),
        MutableLiveData(mutableListOf(DayOfWeek.FRIDAY)),
        "alarm2"
    )

    private val filledInAlarmUnwrapped1 = AlarmUnwrapped(
        10,
        20,
        true,
        mutableListOf(DayOfWeek.FRIDAY),
        "alarm1"
    )

    private fun getRandomAlarm(): Alarm {
        val hour = rg.nextInt(24)
        val minute = rg.nextInt(60)
        val enable = rg.nextBoolean()
        val weekdata = getRandomWeekData()
        val id = randomString()
        return Alarm(
            MutableLiveData(hour),
            MutableLiveData(minute),
            MutableLiveData(enable),
            MutableLiveData(weekdata),
            id
        )
    }

    private fun getRandomWeekData(): MutableList<DayOfWeek> {
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
        for (i in 0 until rg.nextInt(7)) {
            val randomDay = rg.nextInt(days.size)
            result.add(days[randomDay])
            days.removeAt(randomDay)
        }
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

    @Test
    fun convertedUnwrappedAlarmListEmpty_shouldBeEqualToGivenWrappedList() {
        // Empty lists
        val emptyAlarmList = ArrayList<Alarm>()
        checkIfAlarmsEqual(
            emptyAlarmList,
            converter.wrapAlarmList(arrayListOf())!!
        )
    }

    @Test
    fun convertedUnwrappedAlarmListOneAlarm_shouldBeEqualToGivenWrappedList() {
        // List with one alarm
        val listWithOneAlarm = arrayListOf(filledInAlarm1)
        checkIfAlarmsEqual(
            listWithOneAlarm,
            converter.wrapAlarmList(arrayListOf(filledInAlarmUnwrapped1))!!
        )
    }

    @Test
    fun convertedUnwrappedAlarmListRandom_shouldBeEqualToGivenWrappedList() {
        // List with random alarms
        val randomAlarmList = ArrayList<Alarm>()
        val randomAlarmUnwrappedList = ArrayList<AlarmUnwrapped>()
        for (i in 0 until rg.nextInt(20)) {
            val hour = rg.nextInt(24)
            val minute = rg.nextInt(60)
            val enable = rg.nextBoolean()
            val weekdata = getRandomWeekData()
            val id = randomString()
            randomAlarmList.add(
                Alarm(
                    MutableLiveData(hour),
                    MutableLiveData(minute),
                    MutableLiveData(enable),
                    MutableLiveData(weekdata),
                    id
                )
            )
            randomAlarmUnwrappedList.add(AlarmUnwrapped(hour, minute, enable, weekdata, id))
        }
        checkIfAlarmsEqual(randomAlarmList, converter.wrapAlarmList(randomAlarmUnwrappedList)!!)
    }

    @Test
    fun convertedUnwrappedAlarmListOne_shouldBeDifferentToGivenWrappedListEmpty() {
        // empty Alarmlist, list with AlarmUnwrapped
        val emptyAlarmList = ArrayList<Alarm>()
        val filledInAlarmUnwrappedList = arrayListOf(filledInAlarmUnwrapped1)
        Assert.assertNotEquals(
            emptyAlarmList.size,
            converter.wrapAlarmList(filledInAlarmUnwrappedList)!!.size
        )
    }

    @Test
    fun convertedUnwrappedAlarmListEmpty_shouldBeDifferentToGivenWrappedListOne() {
        // list with Alarm, empty AlarmUnwrappedlist
        val filledInAlarmList = arrayListOf(filledInAlarm1)
        val emptyAlarmUnwrappedList = ArrayList<AlarmUnwrapped>()
        Assert.assertNotEquals(
            filledInAlarmList.size,
            converter.wrapAlarmList(emptyAlarmUnwrappedList)!!.size
        )
    }

    @Test
    fun convertedUnwrappedAlarmListOne_shouldBeDifferentToGivenWrappedListOther() {
        // 2 lists with different alarm
        val alarmList = arrayListOf(filledInAlarm2)
        val alarmListUnwrapped = arrayListOf(filledInAlarmUnwrapped1)
        Assert.assertNotEquals(alarmList[0].id, converter.wrapAlarmList(alarmListUnwrapped)!![0].id)
        Assert.assertNotEquals(
            alarmList[0].minute!!.value,
            converter.wrapAlarmList(alarmListUnwrapped)!![0].minute!!.value
        )
    }

    @Test
    fun convertToUnwrapAndBack_shouldBeEqualToStartingList() {
        val originalList = mutableListOf(filledInAlarm1)
        val convertedTwiceList =
            converter.wrapAlarmList(converter.unwrapAlarmList(originalList, listOf("alarm1")))!!
        checkIfAlarmsEqual(convertedTwiceList, originalList)
    }

    @Test
    fun convertTowrapAndBack_shouldBeEqualToStartingList() {
        /** We already established that the converter.wrapAlarmList() works.
         *  We will now convert an Alarm to an AlarmUnwrapped, and then convert it back
         *  to an Alarm, so we can reuse checkIfAlarmsEqual(.., ..)
         */
        val originalList2 = mutableListOf(filledInAlarmUnwrapped1)
        val convertedTwiceList2 =
            converter.unwrapAlarmList(converter.wrapAlarmList(originalList2)!!, listOf("alarm1"))
        checkIfAlarmsEqual(
            converter.wrapAlarmList(convertedTwiceList2)!!,
            converter.wrapAlarmList(originalList2)!!
        )
    }

    @Test
    fun convertedWrappedAlarmListEmpty_shouldBeEqualToGivenUnwrappedList() {
        /** We already established that the converter.wrapAlarmList() works.
         *  We will now convert an Alarm to an AlarmUnwrapped, and then convert it back
         *  to an Alarm, so we can reuse checkIfAlarmsEqual(.., ..)
         */
        // Empty lists
        val emptyList1 = converter.wrapAlarmList(ArrayList())!!
        val emptyList2 =
            converter.wrapAlarmList(converter.unwrapAlarmList(ArrayList(), arrayListOf()))!!
        checkIfAlarmsEqual(
            emptyList1,
            emptyList2
        )
    }

    @Test
    fun convertedWrappedAlarmListOne_shouldBeEqualToGivenUnwrappedList() {
        // List with one alarm
        val listWithOneAlarmUnwrapped = arrayListOf(filledInAlarmUnwrapped1)
        val listWithOneAlarm = arrayListOf(filledInAlarm1)
        checkIfAlarmsEqual(
            converter.wrapAlarmList(listWithOneAlarmUnwrapped)!!,
            converter.wrapAlarmList(
                converter.unwrapAlarmList(
                    listWithOneAlarm,
                    arrayListOf("alarm1")
                )
            )!!
        )
    }

    @Test
    fun convertedWrappedAlarmList2000Random_shouldBeEqualToGivenUnwrappedList() {
        // 2.000 lists with random alarms
        for (x in 0 until 2000) {
            val randomAlarmList = ArrayList<Alarm>()
            val randomAlarmUnwrappedList = ArrayList<AlarmUnwrapped>()
            for (i in 0 until rg.nextInt(200)) {
                val hour = rg.nextInt(24)
                val minute = rg.nextInt(60)
                val enable = rg.nextBoolean()
                val weekdata = getRandomWeekData()
                val id = randomString()
                randomAlarmList.add(
                    Alarm(
                        MutableLiveData(hour),
                        MutableLiveData(minute),
                        MutableLiveData(enable),
                        MutableLiveData(weekdata),
                        id
                    )
                )
                randomAlarmUnwrappedList.add(AlarmUnwrapped(hour, minute, enable, weekdata, id))
            }
            val idList = randomAlarmUnwrappedList.map { it.id!! }
            checkIfAlarmsEqual(
                converter.wrapAlarmList(randomAlarmUnwrappedList)!!,
                converter.wrapAlarmList(converter.unwrapAlarmList(randomAlarmList, idList))!!
            )
        }
    }

    @Test
    fun convertedUnwrappedMedicationMinimal_shouldBeEqualToGivenWrappedMedication() {
        // Bare minimum filled in
        val alarmList = mutableListOf(getRandomAlarm())
        val alarmUnwrappedList = converter.unwrapAlarmList(
            alarmList,
            alarmList.map { it.id!! }) // We already tested unwrapAlarmList()
        // bare necessities filled in (name, id and one alarm), default value for hasPhoto, all explicitly written here to make it clear
        val medMin = Medication("Minimal", null, null, alarmList, null, "min", null, null, false)
        val medUnwrappedMin =
            MedicationUnwrapped("Minimal", null, null, alarmUnwrappedList, null, false, "min")
        checkIfMedicationsEqual(medMin, converter.wrapMedication(medUnwrappedMin))
    }

    @Test
    fun convertedUnwrappedMedication20000RandomOneAlarm_shouldBeEqualToGivenWrappedMedication() {
        // Check 20000 randomly filled in Medication, with 1 alarm
        for (x in 0 until 20000) {
            val alList = mutableListOf(getRandomAlarm())
            val alUnwrappedList = converter.unwrapAlarmList(
                alList,
                alList.map { it.id!! }) // We already tested unwrapAlarmList()
            val name = randomString()
            val note = randomString()
            val intake = randomString()
            val id = randomString()
            val hasPhoto = rg.nextBoolean()
            val med = Medication(name, note, intake, alList, null, id, null, null, hasPhoto)
            val medUnwrapped =
                MedicationUnwrapped(name, note, intake, alUnwrappedList, null, hasPhoto, id)
            checkIfMedicationsEqual(med, converter.wrapMedication(medUnwrapped))
        }
    }

    @Test
    fun convertedUnwrappedMedication20000RandomUpTo200Alarms_shouldBeEqualToGivenWrappedMedication() {
        // Check 20000 randomly filled in Medication, with up to 200 alarms
        for (x in 0 until 20000) {
            val alarms = ArrayList<Alarm>()
            for (y in 0 until rg.nextInt(200)) {
                alarms.add(getRandomAlarm())
            }
            val alarmsUnwrapped = converter.unwrapAlarmList(alarms, alarms.map { it.id!! })
            val name = randomString()
            val note = randomString()
            val intake = randomString()
            val id = randomString()
            val hasPhoto = rg.nextBoolean()
            val med = Medication(name, note, intake, alarms, null, id, null, null, hasPhoto)
            val medUnwrapped =
                MedicationUnwrapped(name, note, intake, alarmsUnwrapped, null, hasPhoto, id)
            checkIfMedicationsEqual(med, converter.wrapMedication(medUnwrapped))
        }
    }


    // Functions to check if Medications are equal

    private fun checkIfMedicationsEqual(med1: Medication, med2: Medication) {
        // Ignored some fields, since those will be generated by other code, not to be tested here
        // Most notably: timeToNext: must be checked in the MedicationTest class
        Assert.assertEquals(med1.name, med2.name)
        Assert.assertEquals(med1.note, med2.note)
        Assert.assertEquals(med1.intake, med2.intake)
        Assert.assertEquals(med1.hasPhoto, med2.hasPhoto)
        Assert.assertEquals(med1.infoLeaflet.toString(), med2.infoLeaflet.toString())
        Assert.assertEquals(med1.id, med2.id)
        checkIfAlarmsEqual(med1.alarms!!, med2.alarms!!)
    }

    private fun checkIfAlarmsEqual(alarms1: MutableList<Alarm>, alarms2: MutableList<Alarm>) {
        Assert.assertEquals(alarms1.size, alarms2.size)
        for (i in 0 until alarms1.size) {
            val al1 = alarms1[i]
            val al2 = alarms2[i]
            Assert.assertTrue(al1.enabled!!.value == al2.enabled!!.value)
            Assert.assertTrue(al1.hour!!.value == al2.hour!!.value)
            Assert.assertTrue(al1.minute!!.value == al2.minute!!.value)
            Assert.assertTrue(al1.id == al2.id)
            checkIfWeekDataEqual(al1.weekData!!.value!!, al2.weekData!!.value!!)
        }
    }

    private fun checkIfWeekDataEqual(wk1: MutableList<DayOfWeek>, wk2: MutableList<DayOfWeek>) {
        Assert.assertEquals(wk1.size, wk2.size)
        for (i in 0 until wk1.size) {
            Assert.assertTrue(wk1[i].value == wk2[i].value)
        }
    }
}