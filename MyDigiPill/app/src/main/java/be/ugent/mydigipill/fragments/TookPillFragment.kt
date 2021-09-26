package be.ugent.mydigipill.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import be.ugent.mydigipill.R
import be.ugent.mydigipill.confirmationAlerts.MedicationBottomSheetDialogArgs
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.data.TakenStatus
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import kotlinx.android.synthetic.main.activity_main.*

class TookPillFragment : DialogFragment() {

    val viewmodel: OverviewViewModel by activityViewModels()
    private lateinit var medication: Medication

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_took_pill, root)
        val builder = AlertDialog.Builder(requireActivity())
        val args: MedicationBottomSheetDialogArgs by navArgs()
        val medId = args.medId
        medication = viewmodel.findMedicationById(medId)
            ?: throw IllegalStateException("the argument should be a valid medication")

        view.findViewById<Button>(R.id.no_button).setOnClickListener {
            viewmodel.setAlarmStatistics(medication, TakenStatus.SKIPPED)
            dismiss()
        }
        view.findViewById<Button>(R.id.yes_button).setOnClickListener {
            viewmodel.setAlarmStatistics(medication, TakenStatus.TAKEN)
            dismiss()
        }
        builder.setView(view)
        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewmodel.selectedFromNotificationAlarmString = null
        viewmodel.selectedFromNotificationID = null
    }
}