package be.ugent.mydigipill.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.ugent.mydigipill.fragments.SettingsFragment

/**
 * this ViewModel is used so that the
 * @see SettingsFragment don't always have to read from the preferences
 */
class SettingsViewModel : ViewModel() {

    //values
    var snooze: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    var snoozeTime: MutableLiveData<Int> = MutableLiveData<Int>(1)
    var indexOfSpinner: MutableLiveData<Int> = MutableLiveData<Int>(0)
}