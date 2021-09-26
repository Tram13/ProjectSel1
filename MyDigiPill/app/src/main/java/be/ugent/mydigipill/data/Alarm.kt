package be.ugent.mydigipill.data

import androidx.lifecycle.MutableLiveData
import java.time.DayOfWeek

data class AlarmUnwrapped(
    var hour: Int? = null,
    var minute: Int? = null,
    var enabled: Boolean? = null,
    var weekData: MutableList<DayOfWeek>? = null,
    var id: String? = null
)

data class Alarm(
    val hour: MutableLiveData<Int>? = null,
    val minute: MutableLiveData<Int>? = null,
    val enabled: MutableLiveData<Boolean>? = null,
    val weekData: MutableLiveData<MutableList<DayOfWeek>>? = null,
    var id: String? = null
) : AbstractDataClass() {

    override fun getCopy(): Alarm {
        return Alarm(
            hour,
            minute,
            enabled,
            weekData,
            id
        )
    }

    fun deepCopy(): Alarm {
        return Alarm(
            hour?.let { MutableLiveData(hour.value!!) },
            minute?.let { MutableLiveData(minute.value!!) },
            enabled?.let { MutableLiveData(enabled.value!!) },
            weekData?.let { MutableLiveData(weekData.value!!) },
            id?.let { id }
        )
    }

}