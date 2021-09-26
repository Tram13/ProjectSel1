package be.ugent.mydigipill.confirmationAlerts

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * This dialog fragment is used to display all the possible buttons from the prescription.
 * This considering if the prescription exists and/or if the content of the description exists.
 */
class ButtonsPopup : DialogFragment() {

    private lateinit var mView: View
    val viewModel: OverviewViewModel by activityViewModels()
    private lateinit var medication: Medication

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val args: ButtonsPopupArgs by navArgs()
        medication = viewModel.findMedicationById(args.medId)
            ?: throw IllegalStateException("The medication with the given Id should exist!")

        medication.infoLeaflet?.let {
            val disclaimerButton = mView.findViewById<Button>(R.id.disclaimerButton)
            val brandButton = mView.findViewById<Button>(R.id.BrandButton)
            val descriptionButton = mView.findViewById<Button>(R.id.DescriptionButton)
            val routeButton = mView.findViewById<Button>(R.id.RouteButton)
            val warningsButton = mView.findViewById<Button>(R.id.WarningsButton)
            val warningsSmallButton = mView.findViewById<Button>(R.id.WarningsSmallButton)
            val pediatricUseButton = mView.findViewById<Button>(R.id.PediatricUseButton)
            val dosageButton = mView.findViewById<Button>(R.id.DosageButton)
            val reactionsButton = mView.findViewById<Button>(R.id.ReactionsButton)
            val pregnancyButton = mView.findViewById<Button>(R.id.PregnancyButton)
            val overdosageButton = mView.findViewById<Button>(R.id.Overdosage_button)
            setButtonRight(it.brand, brandButton)
            setButtonRight(it.disclaimer, disclaimerButton)
            setButtonRight(it.description, descriptionButton)
            setButtonRight(it.route, routeButton)
            setButtonRight(it.warnings, warningsButton)
            setButtonRight(it.warningsSmall, warningsSmallButton)
            setButtonRight(it.pediatricUse, pediatricUseButton)
            setButtonRight(it.dosage, dosageButton)
            setButtonRight(it.reactions, reactionsButton)
            setButtonRight(it.pregnancy, pregnancyButton)
            setButtonRight(it.overdosage, overdosageButton)
        }

        return mView
    }

    /**
     * This little help function adds an onclick listener to the button if the string is present.
     * otherwise the button should not be visible and we set it invisible here.
     */
    private fun setButtonRight(str: String?, button: Button) {
        str?.let { string ->
            button.setOnClickListener {
                medication.id?.let {
                    val action =
                        ButtonsPopupDirections.actionPrescriptionButtonsToPrescriptionText(
                            string,
                            it
                        )
                    findNavController().navigate(action)
                } ?: throw IllegalStateException("The medication id should never be null!")
            }
        } ?: let {
            button.visibility = View.GONE
            button.isEnabled = false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mView = requireActivity().layoutInflater.inflate(
            R.layout.buttons_popup,
            root
        )
        return AlertDialog.Builder(requireActivity()).setView(mView).create()
    }

}