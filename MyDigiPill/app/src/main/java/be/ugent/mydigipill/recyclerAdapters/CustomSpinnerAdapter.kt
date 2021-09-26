package be.ugent.mydigipill.recyclerAdapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * This adapter is used for the recyclerview of the snooze time in the settingsFragment
 * It is an implementation of the
 * @see BaseAdapter ubt with
 * @see AbstractSpinnerItem as a Type
 * @param list is the list of all the options that can be put in the adapter
 */
class CustomSpinnerAdapter<T : AbstractSpinnerItem>(private val list: List<T>) : BaseAdapter() {

    override fun getItem(position: Int): T {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return list[position].getId()
    }

    /**
     * in this function you get the view of the item you need of the list
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return list[position].getView(
            convertView,
            parent ?: throw IllegalStateException("parent can't be null.")
        )
    }

    override fun getCount(): Int {
        return list.size
    }
}