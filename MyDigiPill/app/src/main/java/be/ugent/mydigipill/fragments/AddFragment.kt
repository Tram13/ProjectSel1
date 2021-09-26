package be.ugent.mydigipill.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import be.ugent.mydigipill.FRAGMENTS
import be.ugent.mydigipill.R
import kotlinx.android.synthetic.main.fragment_add.*

/**
 * This class is an implementation of the abstract AbstractEdiAddFragment class.
 * @see AbstractEditAddFragment
 * This class does everything that needs to be done to add new medication.
 * @author Arthur Deruytter
 */
class AddFragment : AbstractEditAddFragment() {

    companion object {
        fun newInstance() = AddFragment()
    }

    /**
     * Here the top title gets set to the right string value.
     * We also set the new string for the tab layout of the app.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        super.adapter.setOnButtonClickListener { viewModel.deleteAlarm(it, null) }
        view?.findViewById<TextView>(R.id.textView_reminder)?.text =
            getString(R.string.Reminder_fragment_title_add)
        FRAGMENTS.OVERVIEW.string = "add medication"
        mainActivity.editTabLayoutMediator()

        view?.findViewById<Button>(R.id.leafletButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_AddFragment_to_textFieldToSearch)
        }

        return view
    }

    /**
     * Here we add the OnClickListeners to the buttons.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cancelButton.setOnClickListener {
            //cancel the updates
            viewModel.cancelChange(null)

            //make the viewmodel empty for next time
            viewModel.makeEmpty()

            //navigate to overview
            findNavController().navigate(R.id.action_AddFragment_to_overviewFragment)
        }

        saveButton.setOnClickListener {
            //save medication
            val statusCode =
                viewModel.saveMedication(
                    overviewViewModel.medicationList,
                    requireActivity()
                ).orElse(-1)
            map.getOrDefault(statusCode, map.get(-1)).invoke()
        }

    }

    /**
     * If we have successfully saved the medication we need to navigate back to the overviewFragment.
     */
    override fun savedSuccess() {
        super.savedSuccess()
        findNavController().navigate(R.id.action_AddFragment_to_overviewFragment)
    }

}
