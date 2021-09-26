package be.ugent.mydigipill.fragments

import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import be.ugent.mydigipill.FRAGMENTS
import be.ugent.mydigipill.R
import be.ugent.mydigipill.Utils
import be.ugent.mydigipill.data.AlarmEvent
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.viewmodels.DayViewModel
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import com.alamkanak.weekview.OnEventClickListener
import com.alamkanak.weekview.WeekView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_day.*

class DayFragment : Fragment(), OnEventClickListener<AlarmEvent> {

    val viewmodel: OverviewViewModel by activityViewModels()
    val dayViewModel: DayViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_day, container, false)

        val weekview: WeekView<AlarmEvent> = view.findViewById(R.id.weekView)
        weekview.setOnEventClickListener { data: AlarmEvent, eventRect: RectF ->
            this.onEventClick(data, eventRect)
        }
        viewmodel.medicationList.observe(viewLifecycleOwner, Observer {
            //every time medicatiolist is updated, you also need to update alarmevents
            dayViewModel.fetchEvents(it)
        })

        dayViewModel.alarmEvents.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                weekView_placeholder.visibility = View.GONE
                weekview.visibility = View.VISIBLE
                weekview.submit(it)
            } else {
                weekview.visibility = View.GONE
                weekView_placeholder.visibility = View.VISIBLE
            }
        })
        weekview.visibility = View.GONE
        dayViewModel.fetchEvents(
            viewmodel.medicationList.value
                ?: throw IllegalStateException("Medicationlist can't be null.")
        )

        return view
    }

    //In a cafe in Lima that is called parfait brownie that is my favorite thing in the world is like Greek yogurt granola peanut butter vegan brownie fruit

    override fun onEventClick(data: AlarmEvent, eventRect: RectF) {
        val medication: Medication = dayViewModel.mapLongToStringKey[data.id]?.let {
            viewmodel.findMedicationById(it)
        } ?: throw IllegalStateException("A medication should always have an id.")
        requireActivity().view_pager?.currentItem = Utils.fragmentsList.indexOf(FRAGMENTS.OVERVIEW)
        viewmodel.showBottomsheetDialog.postValue(medication.id)
        /*
        medication.id?.let {
            val action = OverviewFragmentDirections.actionOverviewFragmentToBottomSheetDialog(it)
            findNavController().navigate(action)
        }
         */
    }
}