package be.ugent.mydigipill.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.ugent.mydigipill.data.MedicationDAO
import be.ugent.mydigipill.data.MedicationDAOInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class ProfileViewModel : ViewModel() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var name: MutableLiveData<String> = MutableLiveData()
    var email: MutableLiveData<String> = MutableLiveData()
    var password: MutableLiveData<String> = MutableLiveData()
    var profilePicture: MutableLiveData<Uri> = MutableLiveData()

    var wrongPassword: MutableLiveData<Boolean> = MutableLiveData()
    var deleteSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var signOutSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var saveNameSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var saveEmailSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var savePasswordSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var saveProfilePictureSuccess: MutableLiveData<Boolean> = MutableLiveData()

    private val userDAO: MedicationDAOInterface = MedicationDAO()

    private val storageRef = Firebase.storage.reference
    var takePhotoImageUri: Uri? = null

    var startSignOut: Boolean = false
    var startDelete: Boolean = false
    var startSaveEmail: Boolean = false

    var currentName: String = auth.currentUser!!.displayName!!
    var currentEmail: String = auth.currentUser!!.email!!
    val currentPassword: String =
        "*".repeat(Random().nextInt(6) + 6) // val because we don't change it


    /**
     * sign out the current user
     */
    fun signOut() {
        //TODO alarms cancelen ++
        auth.signOut()
        signOutSuccess.postValue(true)
    }

    /**
     * delete the account of the current user if the passed
     * password is correct
     * @param originalPassword password of current user
     */
    fun deleteAccount(originalPassword: String) {
        if (originalPassword.isEmpty()) {
            wrongPassword.postValue(true)
            return
        }
        auth.signInWithEmailAndPassword(auth.currentUser?.email!!, originalPassword)
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnFailureListener {
                        deleteSuccess.postValue(false)
                        Log.d(TAG, it.message ?: "null")
                    }
                    ?.addOnSuccessListener {
                        deleteSuccess.postValue(true)
                    }
            }
            .addOnFailureListener {
                wrongPassword.postValue(true)
            }
    }

    /**
     * update the profile picture of the current user
     * @param uri the uri of the new profile picture
     */
    fun updateProfilePicture(uri: Uri?) {
        if (uri == null) {
            saveProfilePictureSuccess.postValue(false)
        } else {
            val userID = auth.currentUser?.uid

            val profilePictureRef = storageRef.child("${userID}/profile_picture")
            val uploadTask = profilePictureRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                downloadPicture()
                saveProfilePictureSuccess.postValue(true)
                userDAO.setUserHasPhoto(true)
            }.addOnFailureListener {
                saveProfilePictureSuccess.postValue(false)
                Log.d(TAG, it.message ?: "null")
            }
        }
    }

    /**
     * Updates the name of the current user with current data in viewmodel
     */
    fun saveName() {
        name.value?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(it)
                .build()
            auth.currentUser?.updateProfile(profileUpdates)
                ?.addOnFailureListener {
                    saveNameSuccess.postValue(false)
                    Log.d(TAG, it.message ?: "null")
                }
                ?.addOnSuccessListener {
                    this.currentName = name.value!!
                    saveNameSuccess.postValue(true)
                }
        }
    }

    /**
     * Updates the email of the current user with current data in viewmodel
     */
    fun saveEmail(originalPassword: String) {
        email.value?.let { newEmail ->
            auth.currentUser?.email?.let { it1 ->
                auth.signInWithEmailAndPassword(it1, originalPassword)
                    .addOnFailureListener {
                        wrongPassword.postValue(true)
                    }
                    .addOnSuccessListener {
                        auth.currentUser?.updateEmail(newEmail)
                            ?.addOnFailureListener {
                                saveEmailSuccess.postValue(false)
                                Log.d(TAG, it.message ?: "null")
                            }
                            ?.addOnSuccessListener {
                                this.currentEmail = newEmail
                                saveEmailSuccess.postValue(true)
                            }
                    }
            }
        }

    }

    /**
     * Updates the password of the current user with current data in viewmodel
     * only happens when the user fills in his original password
     * @param originalPassword password of current user
     */
    fun savePassword(originalPassword: String) {
        if (originalPassword.isEmpty()) {
            wrongPassword.postValue(true)
            return
        }
        password.value?.let {
            auth.currentUser?.email?.let { it1 ->
                auth.signInWithEmailAndPassword(it1, originalPassword)
                    .addOnFailureListener {
                        wrongPassword.postValue(true)
                    }
                    .addOnSuccessListener {
                        auth.currentUser?.updatePassword(password.value!!)
                            ?.addOnFailureListener {
                                savePasswordSuccess.postValue(false)
                                Log.d(TAG, it.message ?: "null")
                            }
                            ?.addOnSuccessListener {
                                //we don't save the password
                                savePasswordSuccess.postValue(true)
                            }
                    }
            }
        }
    }

    /**
     * if the user has a profile picture
     * load the picture from the database
     * and put it in the viewmodel
     */
    fun downloadPicture() {
        val userID = auth.currentUser?.uid
        userDAO.getUserHasPhoto().addOnSuccessListener { ds ->
            val hasPhoto = ds.getBoolean("hasPhoto")
            if (hasPhoto == true) {
                storageRef.child("${userID}/profile_picture").downloadUrl.addOnSuccessListener {
                    profilePicture.postValue(it)
                }.addOnFailureListener {
                    Log.d(TAG, it.message ?: "null")
                }
            }
        }
    }

    /**
     * const values
     * used to make request
     */
    companion object {
        const val TAG = "PROFILEVIEWMODEL"
        const val TAKE_PHOTO_REQUEST = 0
        const val PICK_IMAGE_REQUEST = 1
        const val REQUEST_CAMERA_ACCESS = 50
    }

}