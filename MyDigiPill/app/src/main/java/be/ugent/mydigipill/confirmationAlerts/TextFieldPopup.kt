package be.ugent.mydigipill.confirmationAlerts

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import be.ugent.mydigipill.R
import be.ugent.mydigipill.viewmodels.TextFieldPopupViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * This Dialog class is used to show a dialog with 1 TextField on it
 * and 2 buttons, a positive and a negative button.
 * This is used to ask the user to confirm his/her password
 * or to ask what we will search in the api.
 */
abstract class TextFieldPopup : DialogFragment() {

    val viewModel: TextFieldPopupViewModel by activityViewModels()

    lateinit var editTextPassword: EditText
    lateinit var myView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return myView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        myView =
            requireActivity().layoutInflater.inflate(R.layout.confirm_password_popup, root)

        editTextPassword = myView.findViewById(R.id.textField)

        editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.inputString.postValue(s.toString())
            }
        })

        myView.findViewById<View>(R.id.buttonSkip)
            .setOnClickListener {
                pressedSkip()
            }
        myView.findViewById<View>(R.id.buttonNext)
            .setOnClickListener {
                pressedNext()
            }

        return builder.setView(myView).create()
    }

    /**
     * The default implementation of these functions is empty just so that
     * in the default way the buttons don't do anything
     */
    open fun pressedSkip() {}

    /**
     * The default implementation of these functions is empty just so that
     * in the default way the buttons don't do anything
     */
    open fun pressedNext() {}

}

