package be.ugent.mydigipill.confirmationAlerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.ugent.mydigipill.R
import be.ugent.mydigipill.notifications.NotificationHandler
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import be.ugent.mydigipill.viewmodels.ProfileViewModel

/**
 * This class is an extension of the ConfirmationFab
 * It implements what the buttons should do,
 * what the title should be and what the content should be.
 */
class ConfirmationTextSignOut : ConfirmationFab() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val overviewViewModel: OverviewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = super.onCreateView(inflater, container, savedInstanceState)
        myView.findViewById<TextView>(R.id.title).text =
            getString(R.string.confirmation_dialog_sign_out_title)
        myView.findViewById<TextView>(R.id.content).text =
            getString(R.string.confirmation_dialog_sign_out_content)
        return res
    }

    override fun onPositivePressed() {
        super.onPositivePressed()
        for (med in overviewViewModel.medicationList.value!!) {
            NotificationHandler(requireContext()).cancelNotification(med)
        }
        profileViewModel.signOut()
        findNavController()
            .navigate(R.id.action_confirmSignOut_to_ProfileFragment)
    }

    override fun onNegativePressed() {
        super.onNegativePressed()
        this.dismiss()
    }
}
