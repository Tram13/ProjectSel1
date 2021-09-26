package be.ugent.mydigipill.viewmodels

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.ugent.mydigipill.data.MedicationDAO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class LoginViewModel : ViewModel() {

    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val screen = MutableLiveData(Screens.SignUp)

    val authSuccessful = MutableLiveData<Boolean>()
    val verificationMailSuccess = MutableLiveData<Boolean>()
    val usernameSetSuccess = MutableLiveData<Boolean>()
    val passwordMail = MutableLiveData<Boolean>()

    val emailError = MutableLiveData<Boolean>()
    val passwordError = MutableLiveData<Boolean>()
    val usernameError = MutableLiveData<Boolean>()


    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * This function switches the screens in the screen variable
     * so that all the Views know which Screen should be in the middle
     * @author Arthur Deruytter
     */
    fun switch() {
        if (screen.value == Screens.SignIn) {
            screen.postValue(Screens.SignUp)
        } else {
            screen.postValue(Screens.SignIn)
        }
    }

    /**
     * This function handles the request to the database while logging in.
     * It also adds OnSuccessListeners and OnFailureListeners to each task
     * which will post the right value to the right LiveData so that the Views get updated.
     * @author Arthur Deruytter
     */
    fun continuePressed() {
        val emailBool = validateEmail()
        val passwordBool = validatePassword()
        val usernameBool = (screen.value == Screens.SignIn || validateUserName())
        if (!(emailBool && passwordBool && usernameBool)) {
            authSuccessful.postValue(false)
            return
        }

        if (screen.value == Screens.SignIn) {
            /* The user pressed on the continue button while on the Sign In screen so we will try to sign in. */
            auth.signInWithEmailAndPassword(email.value!!, password.value!!)
                .addOnSuccessListener {
                    authSuccessful.postValue(true)
                }.addOnFailureListener {
                    authSuccessful.postValue(false)
                }
        } else {
            /* The user pressed on the continue button while on the Sign Up screen so we will try to sign up. */
            auth.createUserWithEmailAndPassword(email.value!!, password.value!!)
                .addOnSuccessListener {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username.value)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnSuccessListener {
                            usernameSetSuccess.postValue(true)
                        }?.addOnFailureListener {
                            usernameSetSuccess.postValue(false)
                        }
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            verificationMailSuccess.postValue(true)
                        }?.addOnFailureListener {
                            verificationMailSuccess.postValue(false)
                        }
                    MedicationDAO().setUserHasPhoto(false)
                }.addOnFailureListener {
                    authSuccessful.postValue(false)
                }
        }
    }

    /**
     * This function will send the password reset email
     * if the email field has a valid email address.
     * @author Arthur Deruytter
     */
    fun forgotPassword() {
        if (!validateEmail()) return
        auth.sendPasswordResetEmail(email.value!!)
            .addOnSuccessListener {
                passwordMail.postValue(true)
            }.addOnFailureListener {
                passwordMail.postValue(false)
            }
    }

    /**
     * This function validates if the email field has the right value
     * and posts to the LiveData if it has an error or not.
     * It also return a boolean if it is valid or not.
     * @author Arthur Deruytter
     */
    private fun validateEmail(): Boolean {
        if (TextUtils.isEmpty(email.value)) {
            emailError.postValue(true)
            return false
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
            ) {
                emailError.postValue(true)
                return false
            }
        }
        emailError.postValue(false)
        return true
    }

    /**
     * This function validates if the password field has the right value
     * and posts to the LiveData if it has an error or not.
     * It also return a boolean if it is valid or not.
     * @author Arthur Deruytter
     */
    private fun validatePassword(): Boolean {
        if (TextUtils.isEmpty(password.value)) {
            passwordError.postValue(true)
            return false
        } else {
            if (password.value?.length!! < 6) {
                passwordError.postValue(true)
                return false
            }
        }
        passwordError.postValue(false)
        return true
    }

    private fun validateUserName(): Boolean {
        return if (TextUtils.isEmpty(username.value)) {
            usernameError.postValue(true)
            false
        } else {
            usernameError.postValue(false)
            true
        }
    }


}

/**
 * This is a simple enum class to hold all the
 * different screen we should be able to switch in between
 * @author Arthur Deruytter
 */
enum class Screens {
    SignIn,
    SignUp
}
