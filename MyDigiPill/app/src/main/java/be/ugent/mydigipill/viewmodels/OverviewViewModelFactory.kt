package be.ugent.mydigipill.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * create the overviewViewmodel with correct strings
 * @param string1 next ingestion message if there is a next alarm
 * @param string2 next ingestion message if no alarms set
 */
class OverviewViewModelFactory(val string1: String, val string2: String) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OverviewViewModel(string1, string2) as T
    }
}