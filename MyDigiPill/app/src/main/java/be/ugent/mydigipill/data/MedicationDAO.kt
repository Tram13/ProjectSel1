package be.ugent.mydigipill.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import be.ugent.mydigipill.R
import be.ugent.mydigipill.notifications.NotificationHandler
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

enum class TakenStatus(val code: Int) {
    TAKEN(1),
    SKIPPED(0),
    NULL(-1)
}

class MedicationDAO : MedicationDAOInterface {

    private val userID =
        FirebaseAuth.getInstance()
            .currentUser?.uid
    private val db = Firebase.firestore
    private var medDB: CollectionReference
    private val messageAddOrEdit: MutableLiveData<String> = MutableLiveData()
    private val messageImage: MutableLiveData<String> = MutableLiveData()
    private var onBootCompleted: Int = 0

    init {
        if (userID != null) {
            medDB = db.collection("users").document(userID).collection("medication")
        } else {
            throw userNotLoggedInException // This shouldn't happen
        }
    }

    enum class InputMode {
        EDIT,
        ADD;
    }

    /**
     * adds a medication to the database
     * @param med The medication to be saved
     * @param id the id of the updated medication, if nothing or empty string passed
     * this function will create a new medication object in the database
     * and firestore will generate a id for the medication
     * @param context the context for toasts
     */
    override fun addMedication(
        med: Medication,
        id: String,
        context: Context
    ): String {
        if (userID != null) { // User is logged in
            // PREPARING DATA FOR BATCHED WRITE TO DB
            var inputMode = InputMode.ADD
            if (id != "") {
                inputMode = InputMode.EDIT
            }
            // document to save the medication (except alarms) in
            val medDoc: DocumentReference = if (id == "") {
                medDB.document()
            } else {
                medDB.document(id)
            }
            val alarmDocs =
                ArrayList<DocumentReference>() // list of documents to save the alarms in
            for (i in med.alarms!!) { // filling up the list
                alarmDocs.add(medDoc.collection("alarms").document())
            }

            val info = med.infoLeaflet

            val infoLeafletData = med.infoLeaflet?.let {
                hashMapOf(
                    "disclaimer" to info?.disclaimer,
                    "description" to info?.description,
                    "route" to info?.route,
                    "brand" to info?.brand,
                    "warnings" to info?.warnings,
                    "warningsSmall" to info?.warningsSmall,
                    "pediatricUse" to info?.pediatricUse,
                    "dosage" to info?.dosage,
                    "reactions" to info?.reactions,
                    "pregnancy" to info?.pregnancy,
                    "overdosage" to info?.overdosage
                )
            }

            val medDocData = hashMapOf( // all medication data except alarms
                "name" to med.name,
                "note" to med.note,
                "intake" to med.intake,
                "infoLeaflet" to infoLeafletData,
                "hasPhoto" to med.hasPhoto,
                "id" to medDoc.id
            )
            val medDocAlarms = DatabaseConverter().unwrapAlarmList(
                med.alarms,
                alarmDocs.map {it.id}
            ) // all alarms data with corresponding ID

            // WRITING TO DB
            db.runBatch { batch ->
                batch.set(medDoc, medDocData)
                for (i in 0 until alarmDocs.size) {
                    batch.set(alarmDocs[i], medDocAlarms[i])
                }
            }
                .addOnSuccessListener {
                    if (inputMode == InputMode.ADD) {
                        messageAddOrEdit.postValue(context.getString(R.string.medication_add_succes))
                    } else {
                        messageAddOrEdit.postValue(context.getString(R.string.medication_update_succes))
                    }
                    Log.d(TAG, "Medication successfully added")
                }
                .addOnFailureListener {
                    if (inputMode == InputMode.ADD) {
                        messageAddOrEdit.postValue(context.getString(R.string.medication_add_fail))
                    } else {
                        messageAddOrEdit.postValue(context.getString(R.string.medication_update_fail))
                    }
                    Log.d(TAG, "Failed to add medication")
                }
            med.image?.let { saveImage(medDoc.id, it, context) }
            return medDoc.id
        } else {
            throw userNotLoggedInException // This shouldn't happen
        }
    }

    /**
     * Saves an image in the firestore
     * @param id the id of the medication where the image belongs to
     * @param uri the image Uri
     * @param context the context for toasts
     */
    private fun saveImage(id: String, uri: Uri, context: Context) {
        val storageRef = Firebase.storage.reference
        val profilePictureRef = storageRef.child("${id}/medication_picture")
        val uploadTask = profilePictureRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            messageImage.postValue(context.getString(R.string.image_medication_success))
            Log.d(TAG, "success")
        }.addOnFailureListener {
            messageImage.postValue(context.getString(R.string.image_medication_failed))
            Log.d(TAG, it.message ?: "null")
        }
    }

    /**
     * Get the alarms of a medDoc and update in in the viewmodel medicationlist
     * @param medDoc the documentSnapshot
     * @param place The index of the snapshot
     * @param viewModel the viewmodel that needs to be updated
     */
    private fun getAlarms(
        medDoc: DocumentSnapshot,
        place: Int,
        viewModel: OverviewViewModel
    ) {
        medDoc.reference.collection("alarms").get()
            .addOnSuccessListener { res ->
                val alarmListUnwrapped: MutableList<AlarmUnwrapped> = mutableListOf()
                for (alarm in res) {
                    alarmListUnwrapped.add(alarm.toObject())
                }
                if (place < viewModel.medicationList.value?.size!!) {
                    viewModel.medicationList.value?.get(place)?.alarms =
                        DatabaseConverter().wrapAlarmList(alarmListUnwrapped)
                    viewModel.medicationList.postValue(viewModel.medicationList.value)
                    viewModel.getNextIngestions()
                } else {
                    throw fireStoreOutOfBounds
                }
            }
            .addOnFailureListener { exception -> Log.w(TAG, "Error getting alarms.", exception) }
    }

    /**
     * Get the medications of current user from the database and update it in the viewmodel
     * @param viewModel the viewmodel that needs to be updated
     */
    override fun getMedicationList(viewModel: OverviewViewModel) {
        if (userID != null) {
            medDB.get()
                .addOnSuccessListener { result ->
                    viewModel.medicationList.value = ArrayList()
                    for ((i, doc) in result.withIndex()) {
                        if (result.documents[i] != null) {
                            viewModel.medicationList.value?.add(
                                DatabaseConverter().wrapMedication(
                                    doc.toObject()
                                )
                            )
                            getAlarms(result.documents[i], i, viewModel)
                        }
                    }
                    viewModel.loadImages()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting all medication.", exception)
                }
        } else {
            throw userNotLoggedInException // This shouldn't happen
        }
    }

    /**
     * delete a medication from the database given an ID
     * @param medID the id of the medication that needs to be deleted
     */
    override fun deleteMedicationByID(medID: String) {
        Log.d(TAG, "delete medication $medID")
        deleteAndUpdateByID("medication/$medID", null)
    }

    /**
     * delete a medication from the database given an ID
     * @param alarmID the id of the medication that needs to be deleted
     */
    override fun deleteAlarmByID(alarmID: String, medID: String, afterDeleteAction: (() -> Unit)?) {
        Log.d(TAG, "delete alarm $alarmID")
        deleteAndUpdateByID("medication/$medID/alarms/$alarmID", afterDeleteAction)
    }

    /**
     * delete a medication from the database given an ID
     * if you want to update a medication, also call this but afterDeleteAction
     * is a add action
     * @param path the path of the medication or alarm that needs to be deleted
     * @param afterDeleteAction the action to be taken after the medication is deleted from the database
     */
    private fun deleteAndUpdateByID(path: String, afterDeleteAction: (() -> Unit)?) {
        val data = hashMapOf(
            "path" to "/users/$userID/$path"
        )
        Log.d(TAG, "path = ${data["path"]}")
        FirebaseFunctions.getInstance("europe-west3")
            .getHttpsCallable("recursiveDelete").call(data)
            .addOnFailureListener {
                Log.d(TAG, "FAILED")
                Log.d(TAG, "exception: ${it.message}")
            }.addOnSuccessListener {
                Log.d(TAG, "SUCCESS")
                if (afterDeleteAction != null) {
                    afterDeleteAction()
                }
            }
    }

    /**
     * update a medication from the database
     * @param medication the medication containing the new data that needs to be updated in the database
     * @param context the context for toasts
     */
    override fun updateMedication(
        medication: Medication,
        context: Context
    ) { // Will submit given medication with new values to DB
        val afterDeleteAction: () -> Unit = {
            addMedication(medication, medication.id!!, context)
        }
        deleteAndUpdateByID("medication/${medication.id!!}", afterDeleteAction)
    }

    /**
     * This function will (re)schedule all notifications when the device reboots
     */
    override fun scheduleOnBoot(context: Context) {
        val medicationList: MutableList<Medication> = ArrayList()
        if (userID != null) {
            medDB.get()
                .addOnSuccessListener { result ->
                    val maxAmount: Int = result.size()
                    for ((i, doc) in result.withIndex()) {
                        if (result.documents[i] != null) {
                            medicationList.add(
                                DatabaseConverter().wrapMedication(
                                    doc.toObject()
                                )
                            )
                            getAlarmsOnBoot(
                                result.documents[i],
                                i,
                                medicationList,
                                maxAmount,
                                context
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting all medication at boottime.", exception)
                }
        } else {
            throw userNotLoggedInException // This shouldn't happen
        }
    }

    /**
     * This function is called from NotificationReceiver.
     * This function will link the medication with its alarms.
     * This function bears some resemblance to getAlarms(), however:
     * we wanted to prevent using listeners on boot, since it would be difficult to
     * deregister the listeners on completion.
     */
    private fun getAlarmsOnBoot(
        medDoc: DocumentSnapshot,
        place: Int,
        medicationList: MutableList<Medication>,
        maxAmount: Int,
        context: Context
    ) {
        medDoc.reference.collection("alarms").get()
            .addOnSuccessListener { res ->
                val alarmListUnwrapped: MutableList<AlarmUnwrapped> = mutableListOf()
                for (alarm in res) {
                    alarmListUnwrapped.add(alarm.toObject())
                }
                if (place < medicationList.size) {
                    medicationList[place].alarms =
                        DatabaseConverter().wrapAlarmList(alarmListUnwrapped)
                    onBootCompleted++
                    if (onBootCompleted == maxAmount) {
                        NotificationHandler(context).scheduleAllNotifications(medicationList)
                        onBootCompleted = 0 // Resetting this value, I don't believe this is necessary
                    }
                } else {
                    throw fireStoreOutOfBounds
                }
            }
            .addOnFailureListener { exception -> Log.w(TAG, "Error getting alarms.", exception) }
    }

    override fun setUserHasPhoto(b: Boolean) {
        if (userID != null) {
            val data = mapOf("hasPhoto" to b)
            db.collection("users").document(userID).set(data, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "Succesfully updated user hasPhoto to $b") }
                .addOnFailureListener { e ->
                    Log.d(
                        TAG,
                        "Unable to update user hasPhoto. Exception:\n$e"
                    )
                }
        }
    }

    /**
     * Returns the task with the DocumentSnapshot of the current user on completion.
     * Used to check if the user has a custom photo.
     */

    override fun getUserHasPhoto(): Task<DocumentSnapshot> {
        if (userID != null) {
            return db.collection("users").document(userID).get()
        } else {
            throw userNotLoggedInException
        }
    }

    /**
     * Saves if the Medication has been taken for a certain alarm to FireStore
     */
    override fun setAlarmStatistics(alarmString: String, medID: String, status: TakenStatus) {
        val data = mapOf("taken" to status.code)
        medDB.document(medID).collection("statistics").document(alarmString).set(data)
            .addOnSuccessListener { Log.d(TAG, "Set taken for $alarmString to ${status.code}") }
            .addOnFailureListener { e -> Log.d(TAG, "Failed to update taken for $alarmString: $e") }
    }

    /**
     * Checks if the medication has been taken at a specific alarm + date
     */
    override fun getAlarmStatistics(alarmString: String, medID: String, ifNotTakenAction: () -> Unit) {
        medDB.document(medID).collection("statistics").document(alarmString).get()
            .addOnSuccessListener {ds ->
                val code = ds.getLong("taken")?.toInt() // If the medication for the scheduled alarm is already taken
                Log.d(TAG, "Value of taken is:")
                Log.d(TAG, "$code")
                if (code == null) {
                    setAlarmStatistics(alarmString, medID, TakenStatus.NULL) // If the database entry doesn't exist yet, create it
                    ifNotTakenAction() // This will schedule the notification
                } else if (code == TakenStatus.NULL.code) { // Medication not yet taken for this alarm, so schedule this alarm to go off
                    ifNotTakenAction() // This will schedule the notification
                }
            }
    }

    /**
     * const values
     */
    companion object {
        private const val TAG = "MEDICATIONDAO"
        val userNotLoggedInException: FirebaseAuthInvalidCredentialsException =
            FirebaseAuthInvalidCredentialsException(
                "NO_USER",
                "Logged out user trying to access database"
            )
        val fireStoreOutOfBounds = FirebaseFirestoreException(
            "Error getting alarms. (Out of bounds!)",
            FirebaseFirestoreException.Code.OUT_OF_RANGE
        )
    }
}