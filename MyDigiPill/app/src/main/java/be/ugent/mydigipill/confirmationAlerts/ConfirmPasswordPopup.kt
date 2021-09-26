package be.ugent.mydigipill.confirmationAlerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.ugent.mydigipill.R
import be.ugent.mydigipill.viewmodels.ProfileViewModel
import kotlin.properties.Delegates

/**
 * This class is an extension of the TextFieldPopup
 * It implements what the buttons should do
 * and what the title should be.
 */
class ConfirmPasswordPopup : TextFieldPopup() {

    var type by Delegates.notNull<Int>()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = super.onCreateView(inflater, container, savedInstanceState)
        myView.findViewById<TextView>(R.id.title).text = context?.getString(R.string.confirm_pw)
        return res
    }

    override fun pressedSkip() {
        super.pressedSkip()
        this.dismiss()
    }

    override fun pressedNext() {
        super.pressedNext()
        when {
            profileViewModel.startDelete -> {
                //delete User
                profileViewModel.deleteAccount(viewModel.inputString.value.toString())
            }
            profileViewModel.startSaveEmail -> {
                profileViewModel.saveEmail(viewModel.inputString.value.toString())
            }
            else -> {
                // change password
                profileViewModel.savePassword(viewModel.inputString.value.toString())
            }
        }
        findNavController().navigate(R.id.action_confirmPassword_to_ProfileFragment)
    }
}
