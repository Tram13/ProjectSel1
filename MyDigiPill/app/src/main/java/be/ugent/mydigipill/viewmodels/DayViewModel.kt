package be.ugent.mydigipill.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.ugent.mydigipill.data.AlarmEvent
import be.ugent.mydigipill.data.Medication
import org.joda.time.DateTime
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList

class DayViewModel : ViewModel() {
    val alarmEvents = MutableLiveData<MutableList<AlarmEvent>>(ArrayList())

    /**
     * in this hasmap will the
     * @see AlarmEvent id's have te corresponding
     * @see Medication id
     */
    var mapLongToStringKey: HashMap<Long, String> = hashMapOf()


    /**
     * fetchEvents creates a list of
     * @see AlarmEvent to be put in the weekview
     * @param medications are all the medications that should be displayed in the weekview
     * @author Wout Van Kets
     */
    fun fetchEvents(medications: List<Medication>) {
        alarmEvents.value = ArrayList()
        var id: Long = 0
        mapLongToStringKey = hashMapOf()
        val today = DateTime.now().dayOfWeek
        medications.forEach { medication ->
            medication.alarms?.forEach { alarm ->
                if (alarm.enabled?.value
                        ?: throw IllegalStateException("Alarm enabled can't be null")
                ) {
                    alarm.weekData?.value?.forEach { weekday: DayOfWeek ->
                        if (weekday.ordinal + 1 == today) {

                            val start: Calendar = GregorianCalendar()
                            start.set(Calendar.DAY_OF_WEEK, (weekday.ordinal + 1) % 7 + 1)
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

                            val title = medication.name
                                ?: throw IllegalStateException("Name of medication can't be null")
                            mapLongToStringKey[id] =
                                medication.id
                                    ?: throw IllegalStateException("Medication id can't be null")

                            alarmEvents.value?.add(
                                AlarmEvent(
                                    id, title,
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
    }

}