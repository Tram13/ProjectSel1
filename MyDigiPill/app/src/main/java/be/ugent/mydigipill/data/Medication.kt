package be.ugent.mydigipill.data

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.joda.time.DateTime
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList

data class MedicationUnwrapped(
    var name: String? = null,
    var note: String? = null,
    var intake: String? = null,
    var alarms: MutableList<AlarmUnwrapped>? = ArrayList(),
    var infoLeaflet: Map<String, String>? = null,
    var hasPhoto: Boolean = false,
    var id: String? = null
)

data class Medication(
    var name: String? = null,
    var note: String? = null,
    var intake: String? = null,
    var alarms: MutableList<Alarm>? = ArrayList(),
    var infoLeaflet: InfoLeaflet? = null,
    var id: String? = null,
    var timeToNext: String? = null,
    var image: Uri? = null, // Temporary store for image
    var hasPhoto: Boolean = false

) : Comparable<Medication>, AbstractDataClass() {

    /**
     * loads the medication image out of the database
     * if it has none, there none will load
     */
    fun loadImage(medicationList: MutableLiveData<MutableList<Medication>>) {
        if (id == null) {
            throw IllegalStateException()
        }
        if (hasPhoto) {
            val storageRef = Firebase.storage.reference
            storageRef.child("${id}/medication_picture").downloadUrl.addOnSuccessListener {
                image = it
                medicationList.postValue(medicationList.value)
            }.addOnFailureListener {
                Log.d("MEDICATION", "Loading failed")
            }
        }
    }

    /**
     * Get the nextIngestion:
     * @return optional triple of
     * int: amount of DAYS remaining until next ingestion
     * int: amount of HOURS remaining until next ingestion
     * int: amount of MINUTES remaining until next ingestion
     */
    fun getNextIngestion(): Optional<Triple<Int, Int, Int>> {

        // huidige tijd
        val now: DateTime = DateTime.now().withHourOfDay(
            Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY) // Because timezones don't really work in emulators
        )
        //geen alarmen
        //zou niet mogen voorkomen
        if (alarms == null) {
            return Optional.empty()
            //throw IllegalStateException("Medication without initialized alarms in database. \'alarms\' is null.")
        }

        // kijk voor elk alarm op elke dag en bepaal wat het eerstvolgende alarm is
        val tripleOfFirstDayFirstAlarm = calculateFirstDayAndAlarm(now)
        // zet de firstDay en firstAlarm juist
        val firstDay = tripleOfFirstDayFirstAlarm.first
        val firstAlarmTime = tripleOfFirstDayFirstAlarm.second

        // derde resultaat all deze null is staan er geen alarmen aan
        if (tripleOfFirstDayFirstAlarm.third == null) {
            return Optional.empty()
        }

        // bepaal het resultaat
        val result =
            firstAlarmTime.withDayOfWeek(1).plusDays(firstDay - 1).minusHours(now.hourOfDay)
                .minusMinutes(now.minuteOfHour)

        return Optional.of(
            Triple(
                result.dayOfWeek.rem(7),
                result.hourOfDay,
                result.minuteOfHour
            )
        )
    }

    /**
     * kijk voor elk alarm op elke dag van de medicatie kijken en bepalen wat het eerstvolgende alarm is
     * @return triple van
     * int: amount of days remaining until next ingestion (Int.MAX_VALUE if no alarms set)
     * DateTime: Time when first alarm will go off (END_OF_TIME if no alarms set)
     * Alarm? : the next alarm that will go off (null if not present)
     */
    fun calculateFirstDayAndAlarm(now: DateTime): Triple<Int, DateTime, Alarm?> {
        // aantaldagen to volgende dag
        var firstDay = Int.MAX_VALUE
        // tijd dat het volgende alarm afgaat
        var firstAlarmTime =
            END_OF_TIME

        var firstAlarm: Alarm? = null

        this.alarms!!.forEach {
            if (it.enabled?.value!!) {
                //kijk voor elke dag waarvoor het alarm actief is of het vroeger is dan de huidige
                for (day in it.weekData?.value!!) {
                    val triple = getSoonerAlarm(day, firstDay, firstAlarmTime, now, it)
                    //indien er een nieuwe firstAlarm is update de velden
                    triple.ifPresent {
                        firstDay = triple.get().first
                        firstAlarmTime = triple.get().second
                        firstAlarm = triple.get().third
                    }
                }
            }
        }
        return Triple(firstDay, firstAlarmTime, firstAlarm)
    }

    /**
     *  checks if alarm is sooner then firstalarm
     *  @return if the alarm is not sooner than the first alarm -> EMPTY triple
     *  else triple of:
     *  int: amount of days remaining until next ingestion
     *  DateTime: Time when first alarm will go off
     *  Alarm : the next alarm that will go off
     */
    fun getSoonerAlarm(
        day: DayOfWeek,
        firstDay: Int,
        firstAlarmTime: DateTime,
        now: DateTime,
        alarm: Alarm
    ): Optional<Triple<Int, DateTime, Alarm>> {
        // zet uur, min van alarm
        val alarmH = alarm.hour?.value!!
        val alarmM = alarm.minute?.value!!
        val alarmToday = DateTime.now().withHourOfDay(alarmH)
            .withMinuteOfHour(alarmM)

        //aantal dagen tot dit alarm -> 0 = vandaag
        var daysToPotentialNextAlarm = Math.floorMod(
            (day.ordinal + 1) - now.dayOfWeek, // +1 because ordinal starts counting from 0
            7
        )
        // alarm moet vandaag afgaan maar is al afgegaan
        // => binnen 7 dagen dus, anders mag het op 0 blijven staan
        if ((daysToPotentialNextAlarm == 0) && (alarmToday < now)) {
            daysToPotentialNextAlarm = 7
        }

        // het huidige alarm is voor firstday -> nieuwe firstday & firstAlarm
        // OF huidige alarm komt op dezelfde dag maar voor het eerste alarm van firstday -> nieuwe firstday & firstAlarm
        // else geeft huidige firstDay en firstAlarm terug
        return if ((daysToPotentialNextAlarm < firstDay) ||
            ((daysToPotentialNextAlarm == firstDay) && (alarmToday < firstAlarmTime))
        ) {
            Optional.of(Triple(daysToPotentialNextAlarm, alarmToday, alarm))
        } else {
            Optional.empty()
        }
    }

    /**
     * creates a deep copy of this medication
     */
    override fun getCopy(): Medication {
        return Medication(
            name,
            note,
            intake,
            alarms,
            infoLeaflet,
            id,
            timeToNext,
            image,
            hasPhoto
        )
    }

    /**
     * Compare medications using time to next ingestion
     * first medication is the one with the soonest alarm
     */
    override fun compareTo(other: Medication): Int {
        val thisIngestion = this.getNextIngestion()
        val otherIngestion = other.getNextIngestion()
        return if (!thisIngestion.isPresent) {
            if (!otherIngestion.isPresent) {
                0 // If both don't have an alarm
            } else {
                1 // Only other has an alarm so this one is bigger
            }
        } else {
            if (!otherIngestion.isPresent) {
                -1 // Only this has an alarm so this one is smaller
            } else {
                // Both have an alarm set: calculating which one goes off first
                COMPARATOR.compare(thisIngestion.get(), otherIngestion.get())
            }
        }
    }

    /**
     * const values
     */
    private companion object {
        private val COMPARATOR =
            Comparator.comparingInt<Triple<Int, Int, Int>> { it.first }
                .thenComparingInt { it.second }
                .thenComparingInt { it.third }
        val END_OF_TIME =
            DateTime(
                9999,
                1,
                1,
                0,
                0
            ) // 9999-01-01 00:00 --> This is supposed to be 'infinite'. We're hoping we don't have to provide support after this date.
    }
}