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
import be.ugent.mydigipill.R
import kotlinx.android.synthetic.main.activity_main.*

/**
 * This abstract class is used as a common super class for ConfirmationFab and ConfirmationText
 * @see ConfirmationFab
 * @see ConfirmationText
 * This class has all the common variables
 * @property myView
 * This class expects 1 parameter which is the layout id that it needs to inflate.
 * @param layout
 * The given layout needs to at least have:
 * @see R.id.buttonSkip
 * this should be the button which has the dismiss behavior.
 * @see R.id.buttonNext
 * this should be the button which has the continue/next behavior.
 * The next 2 should respectively suppose to contain the title and the content of the view.
 * These 2 need to be instances of TextView.
 * @see R.id.title
 * @see R.id.content
 * @see TextView
 * @author Arthur Deruytter
 */
abstract class AbstractConfirmation(
    private val layout: Int
) : DialogFragment() {

    lateinit var myView: View

    /**
     * Here in the onCreateView we make the background transparent for smooth corners.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return myView
    }

    /**
     * Here in the onCreateDialog we actually make the dialog by inflating our view.
     * We also add the onClickListeners to the buttons.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myView = requireActivity().layoutInflater.inflate(layout, root)
        myView.findViewById<View>(R.id.buttonSkip)
            .setOnClickListener {
                onNegativePressed()
            }
        myView.findViewById<View>(R.id.buttonNext)
            .setOnClickListener {
                onPositivePressed()
            }
        return AlertDialog.Builder(requireActivity()).setView(myView).create()
    }

    /**
     * The default implementation is nothing so we don't have to override the function
     */
    open fun onPositivePressed() {

    }

    /**
     * The default implementation is nothing so we don't have to override the function
     */
    open fun onNegativePressed() {

    }

}
