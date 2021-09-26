package be.ugent.mydigipill.data

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import be.ugent.mydigipill.R
import be.ugent.mydigipill.inflate
import be.ugent.mydigipill.recyclerAdapters.AbstractSpinnerItem

class SnoozeTimeItem(val timeInMinutes: Int, private val string: String) : AbstractSpinnerItem {

    override fun getId(): Long {
        return 1
    }

    /**
     * this function returns a view that the snooze time spinner will display
     * right now, this is a textview with the string of
     * @see SnoozeTimeItem
     */
    override fun getView(convertView: View?, parent: ViewGroup): View {
        val view = parent.inflate(R.layout.textview)
        view.findViewById<TextView>(R.id.textView).text = string
        return view
    }
}