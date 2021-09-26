package be.ugent.mydigipill.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import be.ugent.mydigipill.R
import be.ugent.mydigipill.viewmodels.AddViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.time.DayOfWeek

class AlarmFragment : DialogFragment() {

    private val viewModel: AddViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_create_alarm, root)
        val builder = AlertDialog.Builder(requireActivity())
        val timePicker = view.findViewById<TimePicker>(R.id.timepicker)
        val weekdays = view.findViewById<LinearLayout>(R.id.selected_weekdays)
        timePicker.setIs24HourView(true)

        weekdays.children.forEach { child ->
            child.setOnClickListener { view ->
                selectDay(view as Button)
            }
        }

        view.findViewById<Button>(R.id.save_button).setOnClickListener {
            saveAlarm(timePicker)
        }

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            /**
             * cancels dialog and gets rid of the popup
             */
            viewModel.clearAddedAlarm()
            dialog?.dismiss()
        }

        viewModel.addedAlarm.observe(this, Observer { added ->

            if (added) {
                /**
                 *get rid of popup when alarm is added
                 */
                viewModel.clearAddedAlarm()
                dialog?.dismiss()
            }
        })

        val saveButton = view.findViewById<Button>(R.id.save_button)
        viewModel.days.observe(this, Observer { days ->
            if (days.isNullOrEmpty()) {
                saveButton.setBackgroundResource(0)
            } else {
                saveButton.setBackgroundResource(R.drawable.buttons_large_green)
            }
            saveButton.isEnabled = !days.isNullOrEmpty()
        })
        saveButton.setBackgroundResource(0)
        saveButton.isEnabled = false


        builder.setView(view)
        return builder.create()
    }


    /**
     * changes layout of day-button if selected
     * @author Wout Van Kets
     * @param button is needed to change the layout of the selected button
     */
    private fun selectDay(button: Button) {
        val index = (button.parent as LinearLayout).indexOfChild(button)
        if (button.isSelected) {
            button.setBackgroundResource(R.drawable.rounded_corner)
            button.setTextColor((0xff000000).toInt())
            viewModel.days.value = viewModel.days.value?.filter { dayOfWeek ->
                dayOfWeek.ordinal != index
            }?.toMutableList()
            viewModel.days.postValue(viewModel.days.value)
        } else {
            button.setBackgroundResource(R.drawable.rounded_corner_selected)
            button.setTextColor((0xffffffff).toInt())
            viewModel.days.value?.add(DayOfWeek.of(index + 1))
            viewModel.days.postValue(viewModel.days.value)
        }
        button.isSelected = !button.isSelected
    }


    /**
     * saves alarm
     * @author Wout Van Kets
     * @param timePicker is needed to get the hour and minute values
     * @see DayOfWeek is an enum of weekdays
     * add all days that are selected to the list
     */
    private fun saveAlarm(timePicker: TimePicker) {
        val hour: Int = timePicker.hour
        val minute: Int = timePicker.minute
        viewModel.addAlarm(hour, minute)
    }
}