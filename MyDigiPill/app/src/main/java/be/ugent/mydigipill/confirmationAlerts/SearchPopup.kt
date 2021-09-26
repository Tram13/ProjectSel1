package be.ugent.mydigipill.confirmationAlerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import be.ugent.mydigipill.R
import be.ugent.mydigipill.viewmodels.AddViewModel

/**
 * This is an extended class from TextFieldPopup.
 * This class implements what the 2 buttons should
 * do and says what the title text should be.
 */
class SearchPopup : TextFieldPopup() {

    private val addViewModel: AddViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = super.onCreateView(inflater, container, savedInstanceState)

        myView.findViewById<TextView>(R.id.title).text =
            context?.getString(R.string.search_infoleaflet_question)

        return res
    }

    override fun pressedSkip() {
        super.pressedSkip()
        this.dismiss()
    }

    override fun pressedNext() {
        super.pressedNext()
        addViewModel.searchInformationLeaflet(viewModel.inputString.value.toString())
        this.dismiss()
    }
}
