package be.ugent.mydigipill.recyclerAdapters

import android.view.ViewGroup
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Alarm
import be.ugent.mydigipill.inflate

/**
 * This adapter is a really simple implementation of the
 * @see AbstractListAdapter with
 * @see Alarm as a Type
 * @author Arthur Deruytter
 */
class AddRecyclerAdapter(clickListener: (Alarm) -> Unit) :
    AbstractListAdapter<Alarm>(AlarmDiffCallback(), clickListener) {

    /**
     * the stored listener for the delete button
     */
    private var buttonListener: ((item: Alarm) -> Unit)? = null

    /**
     * sets the listener for the delete button
     */
    fun setOnButtonClickListener(listener: (item: Alarm) -> Unit) {
        this.buttonListener = listener
    }

    /**
     * In this fucntion we only inflate the view and give it to the ViewHolder.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlarmViewHolder {
        return AlarmViewHolder(
            parent.inflate(R.layout.alarm_cardview_fragment, false),
            buttonListener
        )
    }

}