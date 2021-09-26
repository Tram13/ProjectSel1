package be.ugent.mydigipill.confirmationAlerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.notifications.NotificationHandler
import be.ugent.mydigipill.viewmodels.OverviewViewModel

/**
 * This class is an extension of the ConfirmationText
 * It implements what the buttons should do.
 * It also sets the strings for the title, content and the buttons correct.
 */
class ConfirmationTextDeleteUser : ConfirmationText() {

    lateinit var medication: Medication
    private val overviewViewModel: OverviewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = super.onCreateView(inflater, container, savedInstanceState)
        myView.findViewById<TextView>(R.id.title).text =
            getString(R.string.confirmation_dialog_delete_account_title)
        myView.findViewById<TextView>(R.id.content).text =
            getString(R.string.confirmation_dialog_delete_account_content)
        myView.findViewById<Button>(R.id.buttonSkip).text =
            getString(R.string.confirmation_dialog_delete_cancel)
        myView.findViewById<Button>(R.id.buttonNext).text =
            getString(R.string.confirmation_dialog_delete_continue)
        return res
    }

    override fun onPositivePressed() {
        super.onPositivePressed()
        for (med in overviewViewModel.medicationList.value!!) {
            NotificationHandler(requireContext()).cancelNotification(med)
        }
        findNavController().navigate(R.id.action_confirmDelete_to_confirmPassword)
    }

    override fun onNegativePressed() {
        super.onNegativePressed()
        this.dismiss()
    }
}