package be.ugent.mydigipill.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import be.ugent.mydigipill.R
import be.ugent.mydigipill.viewmodels.LoginViewModel
import be.ugent.mydigipill.viewmodels.Screens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginViewModel by activityViewModels()

    private lateinit var passwordErrorString: String
    private lateinit var emailErrorString: String
    private lateinit var usernameErrorString: String

    private val screen: Screens = Screens.SignUp

    /**
     * When the view is created the ViewModel gets instantiated.
     * The other Observers get also added to the fields of the ViewModel.
     * @author Arthur Deruytter
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        /**
         * When the screen in the ViewModel has changed we also need to change accordingly
         * @author Arthur Deruytter
         */
        viewModel.screen.observe(viewLifecycleOwner, Observer {
            if (it != screen) {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }
        })

        /**
         * When the ViewModel has changed and the password has an error
         * we need to propagate it to the user by adding an error to the TextField.
         * @author Arthur Deruytter
         */
        viewModel.passwordError.observe(viewLifecycleOwner, Observer {
            if (it) {
                password.error = passwordErrorString
            } else {
                password.error = null
            }
        })

        /**
         * When the ViewModel has changed and the email has an error
         * we need to propagate it to the user by adding an error to the TextField.
         * @author Arthur Deruytter
         */
        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it) {
                email.error = emailErrorString
            } else {
                email.error = null
            }
        })

        /**
         * When the ViewModel has changed and the username has an error
         * we need to propagate it to the user by adding an error to the TextField.
         */
        viewModel.usernameError.observe(viewLifecycleOwner, Observer {
            if (it) {
                username.error = usernameErrorString
            } else {
                username.error = null
            }
        })

        passwordErrorString = getString(R.string.invalid_password)
        emailErrorString = getString(R.string.invalid_email)
        usernameErrorString = getString(R.string.username_error)

        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }


    /**
     * When the activity is created we add 3 Observers to the text fields.
     * These observers make sure that the ViewModel always has the right value.
     * @author Arthur Deruytter
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.password.postValue(s.toString())
            }
        })
        email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.email.postValue(s.toString())
            }
        })
        username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.username.postValue(s.toString())
            }
        })
    }

}