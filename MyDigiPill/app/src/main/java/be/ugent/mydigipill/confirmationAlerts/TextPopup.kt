package be.ugent.mydigipill.confirmationAlerts

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * This Dialog is used to display 1 string in a TextField.
 * The string should be given through the navigation arguments.
 */
class TextPopup : DialogFragment() {

    lateinit var mView: View
    val viewModel: OverviewViewModel by activityViewModels()
    private lateinit var medication: Medication

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val args: TextPopupArgs by navArgs()
        medication = viewModel.findMedicationById(args.medId)
            ?: throw IllegalStateException("The medication with the given ID sould exist!")
        mView.findViewById<TextView>(R.id.text).text = args.prescriptionString
        return mView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mView = requireActivity().layoutInflater.inflate(R.layout.text_popup, root)
        return AlertDialog.Builder(requireActivity()).setView(mView).create()
    }

}
