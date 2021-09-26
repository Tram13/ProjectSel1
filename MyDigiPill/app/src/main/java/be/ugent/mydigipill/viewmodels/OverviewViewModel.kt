package be.ugent.mydigipill.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.*
import org.joda.time.DateTime
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList

open class OverviewViewModel(
    private val ingestionString: String,
    private val ingestionString_empty: String
) : ViewModel() {

    //values
    open val medicationList: MutableLiveData<MutableList<Medication>> =
        MutableLiveData(ArrayList())

    var selectedFromNotificationID: String? = null
    var selectedFromNotificationAlarmString: String? = null

    open val searchQuery: MutableLiveData<String> = MutableLiveData("")

    val showBottomsheetDialog: MutableLiveData<String> = MutableLiveData()

    private val medicationDAO: MedicationDAOInterface =
        MedicationDAO()

    val alarmEvents = MutableLiveData<MutableList<AlarmEvent>>(ArrayList())

    private var timer: Timer = Timer()

    /**
     * As soon as the Overview is openend for the first time, this task will be created.
     */
    init {
        scheduleCountdownTask()
    }

    /**
     * request the medications from the database
     */
    private fun getMedicationList() {
        medicationDAO.getMedicationList(this)
    }

    /**
     * deletes a medication from the database, offline list and updates the view
     */
    fun deleteMedication(medication: Medication) {
        MedicationDAO().deleteMedicationByID(medication.id!!) // Will remove Medication object from database
        medicationList.value?.remove(medication) // Removes Medication from current (offline) list
        medicationList.postValue(medicationList.value) // Updates current view
    }

    open fun filterList(list: MutableList<Medication>): MutableList<Medication> {
        val result: MutableList<Medication> = ArrayList()
        list.forEach { medication ->
            if (medication.name!!.contains(
                    searchQuery.value
                        ?: throw IllegalStateException("This may never happen"),
                    true
                )
            ) {
                result.add(medication)
            }
        }
        return result
    }

    /**
     * Every minute a task on another thread will update the list of Medications displayed.
     * This will also update the next ingestion time.
     */
    private fun scheduleCountdownTask() {
        val task = object : TimerTask() {
            override fun run() {
                medicationList.postValue(medicationList.value)
            }
        }
        timer.schedule(task, 0, MINUTE)
    }

    fun setAlarmStatistics(currentMed: Medication, status: TakenStatus) {
        /* AlarmString gets determined by either:
            - The app was openend by a notification -> update statistics for the notification
            - The app was opened by the launcher -> update statistics for the next Medication
        */
        val alarmString: String
        alarmString = if (selectedFromNotificationAlarmString.isNullOrBlank()) {
            val now: DateTime = DateTime.now().withHourOfDay(
                android.icu.util.Calendar.getInstance()
                    .get(android.icu.util.Calendar.HOUR_OF_DAY) // Because timezones don't really work in emulators
            )
            val next = currentMed.calculateFirstDayAndAlarm(now)
            val result =
                next.second.withSecondOfMinute(0).withMillisOfSecond(0).plusDays(next.first)
                    .toString()
            result

        } else {
            selectedFromNotificationAlarmString!!
        }
        if (alarmString != "5889609-07-11T00:00:00.000+02:00") { // The default date if it doesn't have an alarm set
            MedicationDAO().setAlarmStatistics(alarmString, currentMed.id!!, status)
        }
    }

    /**
     *  load the images for each medication
     */
    fun loadImages() {
        if (medicationList.value != null) {
            medicationList.value!!.forEach {
                it.loadImage(medicationList)
            }
        }
    }

    /**
     * update the ingestion text for each medication
     */
    fun getNextIngestions() {
        if (medicationList.value != null) {
            for (med in medicationList.value!!) {
                val tripleOpt = med.getNextIngestion()
                if (tripleOpt.isPresent) {
                    val triple = tripleOpt.get()
                    med.timeToNext =
                        ingestionString.format(triple.first, triple.second, triple.third)
                } else {
                    med.timeToNext = ingestionString_empty
                }
            }
        }
    }

    /**
     * To prevent the scheduled task from executing after the viewmodel has been destroyed.
     * Otherwise, there would be NullpoointerExceptions
     */
    fun cancelTimer() {
        timer.cancel()
        timer.purge()
    }

    /**
     * Initializing the viewModel from the fragment
     */
    fun setStart() {
        searchQuery.value = ""
        getMedicationList()
    }

    /**
     *  find a medication given an ID
     *  @param medicationId ID of the medication to be searched
     *  @return Optional Medication, empty when no medication with given id is found
     *  else the it contains the found medication
     */
    fun findMedicationById(medicationId: String): Medication? {
        val filtered = medicationList.value?.filter { it.id == medicationId }
        return if (filtered.isNullOrEmpty()) {
            null
        } else {
            filtered[0]
        }
    }

    /**
     * Const values
     */
    private companion object {
        private const val MINUTE = 60000L
        private const val TAG = "OVERVIEWVIEWMODEL"
    }

    /**
     * this map is used when clicked on an item in the weekview
     * since
     * @see AlarmEvent has a Long id and
     * @see Medication has a string id
     */

    /**
     * fetchEvents creates a list of
     * @see AlarmEvent to be put in the weekview
     * @param alarms are all the alarms that should be displayed in the weekview
     */
    fun fetchEvents(alarms: List<Alarm>) {
        alarmEvents.value = ArrayList()
        var id: Long = 0
        alarms.forEach { alarm ->
            if (alarm.enabled?.value
                    ?: throw IllegalStateException("Alarm enabled can't be null")
            ) {
                alarm.weekData?.value?.forEach { weekday: DayOfWeek ->

                    val start: Calendar = GregorianCalendar()
                    start.set(Calendar.SECOND, 0)
                    start.set(Calendar.MILLISECOND, 0)
                    start.set(Calendar.DAY_OF_WEEK, (weekday.ordinal + 1) % 7 + 1)
                    //American sunday is special, because it's the start of the week so DAY_OF_WEEK goes to the previous sunday
                    if (weekday.ordinal == 6) {
                        start.add(Calendar.DAY_OF_MONTH, 7)
                    }
                    start.set(
                        Calendar.HOUR_OF_DAY, alarm.hour?.value
                            ?: throw IllegalStateException("Hour of alarm can't be null")
                    )
                    start.set(
                        Calendar.MINUTE, alarm.minute?.value
                            ?: throw IllegalStateException("Hour of alarm can't be null")
                    )

                    val end: Calendar = GregorianCalendar()
                    end.time = start.time
                    //hardcoded 1 hour so that the alarm isn't too small
                    end.add(Calendar.HOUR, 1)

                    alarmEvents.value?.add(
                        AlarmEvent(
                            id, "",
                            start, end,
                            "",
                            false
                        )
                    )
                    alarmEvents.postValue(alarmEvents.value)
                    id++
                }
            }
        }
    }
}