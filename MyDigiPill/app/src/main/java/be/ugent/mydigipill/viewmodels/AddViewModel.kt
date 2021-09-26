package be.ugent.mydigipill.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.ugent.mydigipill.data.*
import be.ugent.mydigipill.requests.GetRequest
import org.json.JSONException
import org.json.JSONObject
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList


class AddViewModel : ViewModel() {

    //values
    var name: MutableLiveData<String> = MutableLiveData<String>()
    var note: MutableLiveData<String> = MutableLiveData<String>()
    var intake: MutableLiveData<String> = MutableLiveData<String>()
    var alarms: MutableLiveData<MutableList<Alarm>> =
        MutableLiveData<MutableList<Alarm>>(ArrayList())
    private var infoLeaflet: MutableLiveData<InfoLeaflet> = MutableLiveData<InfoLeaflet>()

    var image: MutableLiveData<Uri?> = MutableLiveData()
    private val medicationDAO: MedicationDAOInterface = MedicationDAO()
    var addedAlarm: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var takePhotoImageUri: Uri? = null
    var days: MutableLiveData<MutableList<DayOfWeek>> = MutableLiveData(ArrayList())
    private var deleteAlarmList: MutableList<Alarm> = mutableListOf()
    private var medicationOfAlarms: Medication? = null
    var originalAlarms: MutableLiveData<MutableList<Alarm>> =
        MutableLiveData<MutableList<Alarm>>(ArrayList())

    /**
     * This function will search
     * @param name in the american FDA api as a brand name.
     * This makes the api call and if successful it also wraps it in a
     * @see InfoLeaflet class so that it can easily be added to a
     * @see Medication class.
     */
    fun searchInformationLeaflet(name: String) {
        GetRequest.makeGetRequestToURI(
            type = GetRequest.SearchTypes.BRAND,
            value = name,
            onComplete = {
                infoLeaflet.postValue(it?.let { makeInfoLeafletFromJsonObject(it) })
            },
            onError = {
                if (it.errorCode != 0) {
                    /**
                     * Print the full errors.
                     */
                    Log.d(TAG, "onError errorCode : " + it.errorCode)
                    Log.d(TAG, "onError errorBody : " + it.errorBody)
                    Log.d(TAG, "onError errorDetail : " + it.errorDetail)
                } else {
                    // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                    Log.d(TAG, "onError errorDetail : " + it.errorDetail)
                }
                // Just to make sure nothing gets saved
                infoLeaflet.postValue(null)
            }
        )
    }

    /**
     * This is a help function that takes a JsonObject and returns the parsed
     * @return InfoLeaflet object.
     * It assumes that the
     * @param jsonObject is parsable into a InfoLeaflet.
     */
    private fun makeInfoLeafletFromJsonObject(jsonObject: JSONObject): InfoLeaflet {
        val disclaimer: Optional<String> = try {
            Optional.of(jsonObject.getJSONObject("meta").getString("disclaimer"))
        } catch (ex: JSONException) {
            Optional.empty()
        }

        val result = try {
            jsonObject.getJSONArray("results")
                .getJSONObject(0)
        } catch (ex: JSONException) {
            null
        }

        val openfda = try {
            result?.getJSONObject("openfda")
        } catch (ex: JSONException) {
            null
        }

        return InfoLeaflet(
            disclaimer = disclaimer.orElse(null),
            route = getStringFromListFromJsonObject(openfda, "route")
                .orElse(null),
            brand = getStringFromListFromJsonObject(openfda, "brand_name")
                .orElse(null),
            description = getStringFromListFromJsonObject(result, "description")
                .orElse(null),
            pediatricUse = getStringFromListFromJsonObject(result, "pediatric_use")
                .orElse(null),
            pregnancy = getStringFromListFromJsonObject(result, "pregnancy")
                .orElse(null),
            warningsSmall = getStringFromListFromJsonObject(result, "boxed_warning")
                .orElse(null),
            warnings = getStringFromListFromJsonObject(result, "warnings_and_cautions")
                .orElse(null),
            dosage = getStringFromListFromJsonObject(result, "dosage_and_administration")
                .orElse(null),
            reactions = getStringFromListFromJsonObject(result, "adverse_reactions")
                .orElse(null),
            overdosage = getStringFromListFromJsonObject(result, "overdosage")
                .orElse(null)
        )
    }

    /**
     * This is a help function that will take a given string from a JsonObject
     * and assume that the value is a list.
     * From that list it will take the first element as a string and return that.
     */
    private fun getStringFromListFromJsonObject(
        jsonObject: JSONObject?,
        value: String
    ): Optional<String> {
        if (jsonObject == null) return Optional.empty()
        return try {
            Optional.of(jsonObject.getJSONArray(value).getString(0))
        } catch (ex: JSONException) {
            Optional.empty()
        }
    }


    /**
     * update the image, this will also notify the observers
     */
    fun updateMedicationImage(uri: Uri?) {
        image.postValue(uri)
    }

    /**
     * save a medication to the database and add it to the list
     * submission is only valid if it has a name and at
     * least one alarm is created (does not need to be enabled)
     */
    fun saveMedication(
        medicationList: MutableLiveData<MutableList<Medication>>,
        context: Context
    ): Optional<Int> {
        // There must be at least a name and 1 alarm
        return Optional.of(
            if (name.value == null || name.value == "") {
                1
            } else if (alarms.value!!.size == 0) { // alarms.value is always initialised as an empty list
                2
            } else {
                //Save the medication
                val med =
                    Medication(
                        name = name.value,
                        note = note.value,
                        intake = intake.value,
                        alarms = alarms.value,
                        infoLeaflet = infoLeaflet.value,
                        hasPhoto = image.value != null,
                        image = image.value
                    )
                val id = medicationDAO.addMedication(med, context = context)
                med.id = id
                medicationList.value?.add(med)
                medicationList.postValue(medicationList.value)

                //update the alarms in the database
                deleteScheduledAlarmsFromDatabase()

                Log.d(TAG, "Saved successfully")
                0
            }
        )
    }

    /**
     *  clears the values of the just added alarm
     */
    fun clearAddedAlarm() {
        addedAlarm = MutableLiveData<Boolean>()
        days.value = ArrayList()
    }

    /**
     * delete an alarm from the view and
     * schedule an alarm to be deleted in the database on save
     * in the add fragment there are no alarms saved from the medication so only delete it from the view
     */
    fun deleteAlarm(alarm: Alarm, medication: Medication?) {
        //always delete the alarm from the view
        deleteAlarmFromView(alarm)

        //if the alarm was saved in the database, it has a medication
        //we save the medication so we can remove it from the database
        alarm.id?.let {
            medicationOfAlarms = medication
            deleteAlarmList.add(alarm)
        }
    }

    /**
     * deletes all alarms from the database that are in the deleteAlarmIDList
     */
    private fun deleteScheduledAlarmsFromDatabase() {
        deleteAlarmList.forEach {
            if (it.id != null) {
                medicationDAO.deleteAlarmByID(it.id!!, medicationOfAlarms!!.id!!, null)
            }
        }
        resetDeleteAlarms()
    }

    /**
     * resets the deleteAlarmIDList & current medication
     * used after deleting alarms from database or on cancel
     */
    private fun resetDeleteAlarms() {
        deleteAlarmList = mutableListOf()
        medicationOfAlarms = null
    }

    /**
     * deletes an alarm from the view
     */
    private fun deleteAlarmFromView(alarm: Alarm) {
        val list = this.alarms.value
        list!!.remove(alarm)
        this.alarms.postValue(list)
    }

    /**
     * set the alarms correct of the medication
     * happens when change is cancelled
     */
    fun cancelChange(med: Medication?) {
        /*var allAlarms = alarms.value

        //remove alarms that were added this update
        allAlarms = allAlarms?.filter { alarm -> (alarm.id != null) }?.toMutableList()

        //add deleted alarms that were not added this update
        allAlarms?.addAll(deleteAlarmList)

        //set the alarms of the medication
        alarms.value = allAlarms
        alarms.postValue(allAlarms)
        */
        originalAlarms.value?.let {
            med?.alarms = it
        }
    }

    /**
     * makes the viewmodel empty
     */
    fun makeEmpty() {
        name = MutableLiveData()
        note = MutableLiveData()
        intake = MutableLiveData()
        alarms = MutableLiveData<MutableList<Alarm>>(ArrayList())
        infoLeaflet = MutableLiveData()
        addedAlarm = MutableLiveData<Boolean>()
        image = MutableLiveData()
        originalAlarms = MutableLiveData<MutableList<Alarm>>(ArrayList())
        resetDeleteAlarms()
    }

    /**
     * set the original alarms
     * deep copy because of enabled/disabled boolean
     */
    fun updateOriginalAlarms(alarms: MutableList<Alarm>?) {
        val copies: MutableList<Alarm> = mutableListOf()
        alarms?.forEach {
            copies.add(it.deepCopy())
        }
        originalAlarms.postValue(copies)
    }

    /**
     * add new alarm to alarms
     * and update the livedata afterwards
     */
    fun addAlarm(hour: Int, minute: Int) {

        val sortedDays = days.value!!
        sortedDays.sortWith(compareBy { it.ordinal })
        val alarm = Alarm(
            MutableLiveData(hour),
            MutableLiveData(minute),
            MutableLiveData(true),
            MutableLiveData(sortedDays)
        )
        alarms.value?.add(alarm)
        alarms.postValue(alarms.value)

        addedAlarm.postValue(true)
    }

    /**
     * update a medication
     * does NOT update the livedata afterwards
     */
    fun updateMedication(medication: Medication, context: Context): Optional<Int> {

        // There must be at least a name and 1 alarm
        return Optional.of(
            if (name.value == null || name.value == "") {
                1
            } else if (alarms.value!!.size == 0) { // alarms.value is always initialised as an empty list
                2
            } else {
                medication.alarms = alarms.value
                medication.name = name.value
                medication.intake = intake.value
                medication.infoLeaflet = infoLeaflet.value
                medication.hasPhoto = (image.value != null) || medication.hasPhoto
                medication.note = note.value
                medication.image = image.value
                medicationDAO.updateMedication(medication, context)

                //update the alarms in the database
                deleteScheduledAlarmsFromDatabase()

                Log.d(TAG, "Saved successfully")
                0
            }
        )
    }

    /**
     * const values
     */
    companion object {
        const val PICK_IMAGE_REQUEST = 2
        const val TAKE_PHOTO_REQUEST = 3
        const val REQUEST_CAMERA_ACCESS = 100
        const val TAG = "ADDVIEWMODEL"
    }

}