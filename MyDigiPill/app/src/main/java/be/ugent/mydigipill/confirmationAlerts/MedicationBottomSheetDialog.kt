package be.ugent.mydigipill.confirmationAlerts

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.AlarmEvent
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.data.TakenStatus
import be.ugent.mydigipill.notifications.NotificationHandler
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import com.alamkanak.weekview.WeekView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.medication_detail_view_sheet.*

/**
 * This class is used to display a BottomSheetDialogFragment.
 * In this bottom sheet we display the detailed view of a "medication" in our app.
 * This class is supposed to be called from a Fragment which implements the BottomSheetListener interface.
 * @see Medication
 * @author Arthur Deruytter en Wout van Kets
 */
class MedicationBottomSheetDialog : BottomSheetDialogFragment(), View.OnTouchListener {

    private val overviewViewModel: OverviewViewModel by activityViewModels()
    private lateinit var medication: Medication

    /**
     * In the OnCreateView we initialize all the onClick listeners and viewModel observers
     * We also make sure the BottomSheet is set as open in the viewmodel.
     * @see viewModel
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.medication_detail_view_sheet, container, false)

        /**
         * now to make sure the scrolling of the bottomsheet doesn't overrule the scrolling of the weekview
         * by example when you scroll down, you close the bottomsheet instead of going up in the weekview
         * this line makes sure that doesn't happen
         */
        view.findViewById<WeekView<AlarmEvent>>(R.id.weekView).setOnTouchListener(this)

        val args: MedicationBottomSheetDialogArgs by navArgs()
        val medId = args.medId
        medication = overviewViewModel.findMedicationById(medId)
            ?: throw IllegalStateException("the argument should be a valid medication")
        medication.infoLeaflet?.let {
            view.findViewById<Button>(R.id.prescriptionButton).setOnClickListener {
                val action =
                    MedicationBottomSheetDialogDirections.actionBottomSheetDialogToPrescriptionButtons(
                        medId
                    )
                findNavController().navigate(action)
            }
        } ?: let {
            //Als er geen prescription is dan tonen we ook geen knop er voor.
            view.findViewById<Button>(R.id.prescriptionButton).visibility = View.GONE
        }


        view.findViewById<Button>(R.id.delete).setOnClickListener {
            val action =
                MedicationBottomSheetDialogDirections.actionBottomSheetDialogToConfirmDelete(
                    medId
                )
            findNavController().navigate(action)
        }
        view.findViewById<Button>(R.id.edit).setOnClickListener {
            val action =
                MedicationBottomSheetDialogDirections.actionBottomSheetDialogToEditFragment(
                    medId
                )
            findNavController().navigate(action)
        }
        view.findViewById<Button>(R.id.took_pill).setOnClickListener {
            showTookPill()
        }

        overviewViewModel.alarmEvents.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                view.findViewById<WeekView<AlarmEvent>>(R.id.weekView)
                    .submit(it)
            }
        })

        /**
         * update almarmEvents of
         * @see OverviewViewModel
         */
        overviewViewModel.fetchEvents(
            medication.alarms ?: throw IllegalStateException("The alarms should be present.")
        )
        //overviewViewModel.detailSheetOpen = true
        medication.image?.let {
            Glide.with(context)
                .load(it)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_notification_dark)
                        .error(R.drawable.ic_notification_dark)
                )
                .into(view.findViewById(R.id.image_bottomsheet))
        }
            ?: view.findViewById<ImageView>(R.id.image_bottomsheet)
                .setImageResource(R.drawable.ic_notification_dark)

        val button = view.findViewById<Button>(R.id.took_pill)
        //if no medication enabled, you shouldn't be able to press the took_pill button
        if (medication.alarms?.any { alarm ->
                alarm.enabled?.value
                    ?: throw IllegalStateException("Enabled of an alarm can't be null")
            } ?: throw IllegalStateException("alarms of a medication can't be null")) {
            button.visibility = View.VISIBLE
            button.isEnabled = true
        } else {
            button.visibility = View.GONE
            button.isEnabled = false
        }

        return view
    }

    /**
     * Will show the dialog where the user can fill in if they took their pill or not
     */

    private fun showTookPill() {
        // If openend from notification, the default will be Skipped
        if (!overviewViewModel.selectedFromNotificationID.isNullOrEmpty()) {
            overviewViewModel.setAlarmStatistics(medication, TakenStatus.SKIPPED)
        }
        // If necessary, the Overview will reschedule if the "pill taken"-dialog is about a pill in the past
        NotificationHandler(requireContext()).cancelNotification(medication)
        medication.id?.let {
            val action =
                MedicationBottomSheetDialogDirections.actionBottomSheetDialogToTookPillDialog(it)
            findNavController().navigate(action)
        }
    }

    /**
     * In the onViewCreated we request the medication which we should display from the viewmodel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindValues(medication)

        // The app was launched by tapping a notification
        if (!overviewViewModel.selectedFromNotificationID.isNullOrBlank()) {
            Handler().post { showTookPill() }
        }
    }

    /**
     * In the bindValues funciton we will actually bind all of our values to the right textViews.
     */
    private fun bindValues(medication: Medication) {
        nameTextView.text = medication.name
        nextTextView.text = medication.timeToNext
        descriptionTextView.text = medication.note
        intakeTextView.text = medication.intake
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.parent?.requestDisallowInterceptTouchEvent(event?.action == MotionEvent.ACTION_MOVE)
        return false
    }
}
