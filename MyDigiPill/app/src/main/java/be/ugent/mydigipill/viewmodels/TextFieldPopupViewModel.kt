package be.ugent.mydigipill.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * This ViewModel just holds the string that the user typed into the TextField.
 * We do this so that we can update this live.
 */
class TextFieldPopupViewModel : ViewModel() {

    var inputString = MutableLiveData<String>()

}
