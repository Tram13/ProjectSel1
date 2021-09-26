package be.ugent.mydigipill.data

import androidx.lifecycle.MutableLiveData
import java.lang.IllegalStateException
import java.time.DayOfWeek

/**
 * this class contains wrapper functions to wrap and unwrap alarms & medications
 */
class DatabaseConverter {

    fun unwrapAlarmList(
        alarmList: MutableList<Alarm>?,
        listIDs: List<String> // List of IDs
    ): MutableList<AlarmUnwrapped> { // App to Database
        return if (alarmList != null) {
            val alarmListUnwrapped = mutableListOf<AlarmUnwrapped>()
            for (i in 0 until alarmList.size) {
                alarmListUnwrapped.add(unwrapAlarm(alarmList[i], listIDs[i]))
            }
            alarmListUnwrapped
        } else {
            ArrayList() // this shouldn't happen
        }
    }

    fun wrapMedication(mu: MedicationUnwrapped): Medication { // Database to App
        return Medication(
            mu.name,
            mu.note,
            mu.intake,
            wrapAlarmList(mu.alarms),
            mu.infoLeaflet?.let { InfoLeaflet(it) },
            mu.id,
            null,
            null,
            mu.hasPhoto
        )
    }

    fun wrapAlarmList(alarmListUnwrapped: MutableList<AlarmUnwrapped>?): MutableList<Alarm>? {
        return if (alarmListUnwrapped != null) {
            val alarmList = mutableListOf<Alarm>()
            for (alarmUnwrapped in alarmListUnwrapped) {
                alarmList.add(wrapAlarm(alarmUnwrapped))
            }
            alarmList
        } else {
            null
        }
    }

    private fun unwrapAlarm(alarm: Alarm, id: String): AlarmUnwrapped { // App to Database
        // These fields should never be null
        return AlarmUnwrapped(
            alarm.hour?.value,
            alarm.minute?.value,
            alarm.enabled?.value,
            alarm.weekData?.value,
            id
        )
    }

    private fun wrapAlarm(alarmUnwrapped: AlarmUnwrapped): Alarm { // Database to App
        val hour: MutableLiveData<Int> =
            alarmUnwrapped.hour?.let { MutableLiveData(it) }
                ?: throw IllegalStateException("Corrupted data in alarm")
        val minute: MutableLiveData<Int> =
            alarmUnwrapped.minute?.let { MutableLiveData(it) }
                ?: throw IllegalStateException("Corrupted data in alarm")
        val enabled: MutableLiveData<Boolean> =
            alarmUnwrapped.enabled?.let { MutableLiveData(it) }
                ?: throw IllegalStateException("Corrupted data in alarm")
        val weekData: MutableLiveData<MutableList<DayOfWeek>> =
            alarmUnwrapped.weekData?.let { MutableLiveData(it) }
                ?: throw IllegalStateException("Corrupted data in alarm")
        return Alarm(
            hour,
            minute,
            enabled,
            weekData,
            alarmUnwrapped.id
        )
    }
}