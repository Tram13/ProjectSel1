package be.ugent.mydigipill.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.SnoozeTimeItem
import be.ugent.mydigipill.recyclerAdapters.CustomSpinnerAdapter
import be.ugent.mydigipill.viewmodels.SettingsViewModel

class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private val viewModel: SettingsViewModel by activityViewModels()

    //items that will be put in the snooze-time-spinner
    private lateinit var list: List<SnoozeTimeItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)

        val spinner: Spinner = view.findViewById(R.id.snooze_time_dropdown)
        list = listOf(
            SnoozeTimeItem(1, getString(R.string.one_minute)),
            SnoozeTimeItem(60, getString(R.string.one_hour)),
            SnoozeTimeItem(3600, getString(R.string.one_day))
        )
        spinner.adapter = CustomSpinnerAdapter(list)

        /**
         * load all preferences of the settings and put those values into the view model
         */
        val prefs = requireActivity().getSharedPreferences(
            getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        viewModel.snooze.postValue(prefs.getBoolean(getString(R.string.snooze), false))


        val snoozeTime = prefs.getInt(getString(R.string.snooze_time), 1)
        list.forEachIndexed { index, item ->
            //search for last selected item of the spinner so that you can display this in the view
            if (item.timeInMinutes == snoozeTime) {
                viewModel.indexOfSpinner.postValue(index)
                viewModel.snoozeTime.postValue(item.timeInMinutes)
            }
        }

        //update view
        viewModel.snooze.observe(viewLifecycleOwner, Observer {
            view.findViewById<Switch>(R.id.snooze_switch).isChecked = it
        })
        viewModel.indexOfSpinner.observe(viewLifecycleOwner, Observer {
            view.findViewById<Spinner>(R.id.snooze_time_dropdown).setSelection(it)
        })

        //add listeners to switches and spinner
        view.findViewById<Switch>(R.id.snooze_switch).setOnCheckedChangeListener {_, isChecked ->
            viewModel.snooze.postValue(isChecked)
            with(prefs.edit()) {
                putBoolean(getString(R.string.snooze), isChecked)
                apply()
            }
        }
        view.findViewById<Spinner>(R.id.snooze_time_dropdown).onItemSelectedListener = this

        return view
    }


    /**
     * functions overriden from AdapterView.OnItemSelectedListener
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {
        viewModel.snoozeTime.postValue(null)
        viewModel.indexOfSpinner.postValue(null)
    }

    /**
     * change value of viewmodel and preference when
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem: SnoozeTimeItem = parent?.getItemAtPosition(position) as SnoozeTimeItem
        val minutes = selectedItem.timeInMinutes
        viewModel.snoozeTime.postValue(minutes)
        viewModel.indexOfSpinner.postValue(position)

        val prefs = requireActivity().getSharedPreferences(
            getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        with(prefs.edit()) {
            putInt(getString(R.string.snooze_time), minutes)
            apply()
        }
    }
}