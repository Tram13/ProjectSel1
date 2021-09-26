package be.ugent.mydigipill.recyclerAdapters

import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Alarm

/**
 * This is an implementation of an
 * @see AbstractViewHolder
 * This class holds all the TextViews and switch of an AlarmCardView
 * @see R.layout.alarm_cardview_fragment
 * @author Arthur Deruytter
 */
class AlarmViewHolder constructor(
    itemView: View,
    private val buttonListener: ((item: Alarm) -> Unit)?
) : AbstractViewHolder<Alarm>(itemView) {
    private var switch: Switch = itemView.findViewById(R.id.switch1)
    private var time: TextView = itemView.findViewById(R.id.textView3)
    private var dayOfWeek: TextView = itemView.findViewById(R.id.textView4)
    private var deleteButton: Button = itemView.findViewById(R.id.deleteAlarmButton)

    /**
     * binds the data to the alarm
     */
    override fun bindTo(t: Alarm) {
        // set switch
        switch.isChecked = t.enabled?.value!!
        switch.setOnCheckedChangeListener { _, isChecked -> t.enabled.value = isChecked }

        // set time
        var tijd = ""
        if (t.hour?.value.toString().length == 1) {
            tijd += 0
        }
        tijd += t.hour?.value.toString() + ":"
        if (t.minute?.value.toString().length == 1) {
            tijd += 0
        }
        tijd += t.minute?.value.toString()
        time.text = tijd

        // set day of the week
        dayOfWeek.text = t.weekData?.value.toString()

        //set the listener on the for the deletebutton
        deleteButton.setOnClickListener { buttonListener?.invoke(t) }
    }


}