package be.ugent.mydigipill.recyclerAdapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * This is a super simple implementation of a
 * @see RecyclerView.ViewHolder
 * We have added a bindTo function so that it can easily be used with the
 * @see AbstractListAdapter
 * @author Arthur Deruytter
 */
abstract class AbstractViewHolder<T> constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    abstract fun bindTo(t: T)
}