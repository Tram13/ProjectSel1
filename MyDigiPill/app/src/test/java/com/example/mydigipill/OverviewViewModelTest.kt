package com.example.mydigipill

import androidx.lifecycle.MutableLiveData
import be.ugent.mydigipill.data.Alarm
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import kotlin.random.Random

class OverviewViewModelTest {

    @Before
    fun setUp() {
        //update de medications (van de databank)
        val randomListOfMedication = ArrayList<Medication>()
        for (x in 0 until rg.nextInt(200)) {
            val med = getRandomMedication()
            med.id += "_$x" // making sure the ID is unique
            randomListOfMedication.add(med)
        }
        whenever(overviewViewModel.medicationList).thenReturn(MutableLiveData(randomListOfMedication))
        whenever(overviewViewModel.filterList(any())).thenCallRealMethod()
    }

    private val overviewViewModel = mock<OverviewViewModel>()
    private val rg = java.util.Random()

    private fun randomString(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val stringLength = rg.nextInt(30)
        return (1..stringLength)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

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

    private fun getRandomMedication(): Medication {
        val alarms = ArrayList<Alarm>()
        for (y in 0 until rg.nextInt(200)) {
            alarms.add(getRandomAlarm())
        }
        val name = randomString()
        val note = randomString()
        val intake = randomString()
        val id = randomString()
        val hasPhoto = rg.nextBoolean()
        return Medication(name, note, intake, alarms, null, id, null, null, hasPhoto)
    }

    @Test
    fun filterMedicationList_OneLetter_HasCorrectItems() {
        // setting search query
        whenever(overviewViewModel.searchQuery).thenReturn(MutableLiveData("a"))
        checkMedicationlist_ValidNameForMedicationsInList(overviewViewModel)
    }

    @Test
    fun filterMedicationList_OneLetter_WithReverse_IsComplete() {
        // setting search query
        whenever(overviewViewModel.searchQuery).thenReturn(MutableLiveData("a"))
        val sizeBeforeFilter = overviewViewModel.medicationList.value?.size
        val sumSizeAfterFilterAndNegativeFilter =
            getMedicationCount_SumOf_NotInFilteredList_InFilteredList(overviewViewModel)
        Assert.assertEquals(sizeBeforeFilter, sumSizeAfterFilterAndNegativeFilter)
    }

    // kijkt of alle medications in de meegegeven lijst de "query" in de naam hebben
    private fun checkMedicationlist_ValidNameForMedicationsInList(
        overviewViewModel: OverviewViewModel
    ): Int {
        // er moet altijd een medicationsDisplayList zijn
        // zou niet mogen voorkomen
        val medicationList = overviewViewModel.medicationList.value
            ?: throw AssertionError("Er is geen MedList!")

        // Controleren dat de query correct is ingesteld
        if (overviewViewModel.searchQuery.value.isNullOrBlank()) {
            return medicationList.size
        }
        val query = overviewViewModel.searchQuery.value!!

        val filteredList = overviewViewModel.filterList(medicationList)
        //kijk of elke medication i de medicationsDisplayList de string "query" bevat
        for (med in filteredList) {
            //indien het niet bevat
            Assert.assertTrue(med.name!!.contains(query, ignoreCase = true))
        }
        return filteredList.size
    }

    private fun getMedicationCount_SumOf_NotInFilteredList_InFilteredList(
        overviewViewModel: OverviewViewModel
    ): Int {

        val medicationList = overviewViewModel.medicationList.value
            ?: throw AssertionError("Er is geen MedList!")

        // indien de query null of leeg is zijn alle medications in displaylist geldig
        // => count = 0
        if (overviewViewModel.searchQuery.value.isNullOrBlank()) {
            return 0 // no items are not in filteredList
        }

        val query = overviewViewModel.searchQuery.value!!
        val filteredList = ArrayList<Medication>()
        for (med in medicationList) {
            if (!med.name!!.contains(query, ignoreCase = true)) {
                filteredList.add(med)
            }
        }

        val shown = checkMedicationlist_ValidNameForMedicationsInList(overviewViewModel)
        return filteredList.size + shown
    }
}
