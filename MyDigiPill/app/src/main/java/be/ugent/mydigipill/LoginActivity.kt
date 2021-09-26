package be.ugent.mydigipill

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import be.ugent.mydigipill.fragments.SignInFragment
import be.ugent.mydigipill.fragments.SignUpFragment
import be.ugent.mydigipill.viewmodels.LoginViewModel
import be.ugent.mydigipill.viewmodels.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.internal.common.CommonUtils
import kotlinx.android.synthetic.main.activity_login.*

/**
 * This is our login activity, this activity handles everything for logging in
 * and signing up the user.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var verificationMail: String
    private lateinit var verificationMailFailed: String
    private lateinit var toastString: String
    private lateinit var welcome: String
    private lateinit var failedAuth: String


    /**
     * When this activity gets created we need to make a new ViewModel.
     * We also need to link all the observers to the LiveData from the ViewModel.
     * @see LiveData
     * @see LoginViewModel
     * @author Arthur Deruytter
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        /**
         *  This observer checks the current screen
         *  and makes sure all the visible fields are set correctly
         *  @author Arthur Deruytter
         */
        viewModel.screen.observe(this, Observer {
            if (it == Screens.SignIn) {
                signInAttached()
            } else {
                signUpAttached()
            }
        })

        /**
         *  This observer checks if the authentication was successful
         *  and displays a toast explaining the successful or failed state.
         *  @author Arthur Deruytter
         */
        viewModel.authSuccessful.observe(this, Observer {
            if (it) {
                val user = auth.currentUser
                Toast.makeText(
                    this, "$welcome, ${user!!.displayName}",
                    Toast.LENGTH_SHORT
                ).show()
                authSuccess()
            } else {
                Toast.makeText(
                    this, failedAuth,
                    Toast.LENGTH_SHORT
                ).show()
                authFailed()
            }
        })

        /**
         *  This observer checks if the verification mail was sent successfully
         *  and displays a toast explaining the successful or failed state.
         *  @author Arthur Deruytter
         */
        viewModel.verificationMailSuccess.observe(this, Observer {
            if (it) {
                Toast.makeText(
                    this,
                    "$verificationMail ${auth.currentUser?.email} ",
                    Toast.LENGTH_LONG
                ).show()
                authSuccess()
            } else {
                Toast.makeText(
                    this, verificationMailFailed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        /**
         *  This observer checks if the username was set successfully
         *  and displays a toast explaining the successful or failed state.
         *  @author Arthur Deruytter
         */
        viewModel.usernameSetSuccess.observe(this, Observer {
            if (it) {
                val user = auth.currentUser
                Toast.makeText(
                    this, "$welcome, ${user?.displayName}",
                    Toast.LENGTH_SHORT
                ).show()
                authSuccess()
            } else {
                Toast.makeText(
                    this, toastString,
                    Toast.LENGTH_SHORT
                ).show()
                authFailed()
            }
        })

        verificationMail = getString(R.string.verification_mail)
        verificationMailFailed = getString(R.string.failed_verification_mail)
        toastString = getString(R.string.failed_set_username)
        welcome = getString(R.string.welcome)
        failedAuth = getString(R.string.failed_authentication)
    }

    /**
     * When this activity starts we will register the
     * @see Button.setOnClickListener
     * for all the buttons we have.
     * @see LoginViewModel
     * @see LoginViewModel.switch
     * @see LoginViewModel.continuePressed
     * @author Arthur Deruytter
     */
    override fun onStart() {
        super.onStart()
        /* If the user presses the signUp or signIn Button then the ViewModel should know */
        signUpButton.setOnClickListener {
            viewModel.switch()
        }
        signInButton.setOnClickListener {
            viewModel.switch()
        }
        continueButton.setOnClickListener {
            //set buttons correct
            continueButton.visibility = View.GONE
            signInButton.visibility = View.GONE
            signUpButton.visibility = View.GONE
            loadingAnimation.visibility = View.VISIBLE

            //hide keyboard
            val newFocus = currentFocus
            val newContext = baseContext
            if (newFocus != null && newContext != null) {
                CommonUtils.hideKeyboard(newContext, newFocus)
            }

            viewModel.continuePressed()
        }
        loadingAnimation.visibility = View.GONE
    }

    /**
     * This function gets called when the Sign In fragment is now the center.
     * It makes sure that everything the activity needs to handle gets done.
     * @see SignInFragment
     * @author Arthur Deruytter
     */
    private fun signInAttached() {
        signInButton.visibility = View.GONE
        signUpButton.visibility = View.VISIBLE
        text.text = getString(R.string.action_sign_in_short)
    }

    /**
     * This function gets called when the Sign Up fragment is now the center.
     * It makes sure that everything the activity needs to handle gets done.
     * @see SignUpFragment
     * @author Arthur Deruytter
     */
    private fun signUpAttached() {
        signInButton.visibility = View.VISIBLE
        signUpButton.visibility = View.GONE
        text.text = getString(R.string.action_register)
    }

    /**
     * This function gets called when the authentication of the user has failed.
     * It makes sure that the user can retry by making the buttons visible or enabling them.
     * @author Arthur Deruytter
     */
    private fun authFailed() {
        loadingAnimation.visibility = View.GONE
        continueButton.visibility = View.VISIBLE
        if (viewModel.screen.value == Screens.SignUp) {
            signInButton.visibility = View.VISIBLE
        } else {
            signUpButton.visibility = View.VISIBLE
        }
    }

    /**
     * This function gets called when the authentication of the user has succeeded.
     * It makes sure that the user gets sent to the MainActivity to start using the app.
     * @see MainActivity
     * @author Arthur Deruytter
     */
    private fun authSuccess() {
        if (
            (
                    viewModel.screen.value == Screens.SignIn &&
                            viewModel.authSuccessful.value != null &&
                            viewModel.authSuccessful.value!!
                    ) || (
                    viewModel.screen.value == Screens.SignUp &&
                            viewModel.verificationMailSuccess.value != null &&
                            viewModel.verificationMailSuccess.value!! &&
                            viewModel.usernameSetSuccess.value != null &&
                            viewModel.usernameSetSuccess.value!!
                    )
        ) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}