package be.ugent.mydigipill.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import be.ugent.mydigipill.FRAGMENTS
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import kotlinx.android.synthetic.main.fragment_add.*

/**
 * This class is an implementation of the abstract AbstractEdiAddFragment class.
 * @see AbstractEditAddFragment
 * This class does everything that needs to be done to edit an already existing medication.
 * @property medication, this is the medication we are editing right now.
 * @author Arthur Deruytter
 */
class EditFragment : AbstractEditAddFragment() {

    companion object {
        fun newInstance() = EditFragment()
    }

    private lateinit var medication: Medication

    /**
     * Here the top title gets set to the right string value.
     * We search the medication id we got passed as an argument from the navArgs in the viewModel.
     * We also set the new string for the tab layout of the app
     * and initialize all the fields to the right values.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<TextView>(R.id.textView_reminder)?.text =
            getString(R.string.Reminder_fragment_title_edit)
        val args: EditFragmentArgs by navArgs()
        val medication = overviewViewModel.findMedicationById(args.medicationId)
            ?: throw IllegalStateException("The EditFragment may not be called on a non existing medication.")

        FRAGMENTS.OVERVIEW.string = "edit medication"
        mainActivity.editTabLayoutMediator()
        this.medication = medication
        initializeFromMedication(view, medication)
        super.adapter.setOnButtonClickListener { viewModel.deleteAlarm(it, this.medication) }

        view?.findViewById<Button>(R.id.leafletButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_EditFragment_to_textFieldToSearch)
        }

        return view
    }

    /**
     * initializes the viewmodel with the current medication
     */
    private fun initializeFromMedication(view: View?, medication: Medication) {
        view?.findViewById<EditText>(R.id.editText_name)?.setText(medication.name)
        viewModel.name.postValue(medication.name)
        view?.findViewById<EditText>(R.id.editText_description)?.setText(medication.note)
        viewModel.note.postValue(medication.note)
        view?.findViewById<EditText>(R.id.editText_ingestion)?.setText(medication.intake)
        viewModel.intake.postValue(medication.intake)

        /**
         * only when viewmodel original alarms is empty/null, does it need to get the alarms of
         * @see medication
         */
        if (viewModel.originalAlarms.value.isNullOrEmpty()) {
            viewModel.alarms.postValue(medication.alarms)
            viewModel.updateOriginalAlarms(medication.alarms)
        }

        /**
         * get the correct image
         */
        viewModel.takePhotoImageUri?.let {
            view?.findViewById<ImageView>(R.id.placeholder)?.setImageURI(it)
        } ?: viewModel.image.value?.let {
            view?.findViewById<ImageView>(R.id.placeholder)?.setImageURI(it)
        } ?: viewModel.image.postValue(medication.image)

    }

    /**
     * Here we add the OnClickListeners to the buttons.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancelButton.setOnClickListener {
            //cancel the updates
            viewModel.cancelChange(medication)

            //make the viewmodel empty for next time
            viewModel.makeEmpty()

            //navigate to overview
            findNavController().navigate(R.id.action_EditFragment_to_overviewFragment)
        }

        saveButton.setOnClickListener {
            //save updated medication
            val statusCode =
                viewModel.updateMedication(medication, requireActivity()).orElse(-1)
            overviewViewModel.medicationList.postValue(overviewViewModel.medicationList.value)
            map.getOrDefault(statusCode, map[-1]).invoke()
        }

    }

    /**
     * Creates a toast with
     * @param message as the message
     */
    private fun createToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * If we have successfully saved the medication we need to navigate back to the overviewFragment.
     */
    override fun savedSuccess() {
        super.savedSuccess()
        findNavController().navigate(R.id.action_EditFragment_to_overviewFragment)
    }
}