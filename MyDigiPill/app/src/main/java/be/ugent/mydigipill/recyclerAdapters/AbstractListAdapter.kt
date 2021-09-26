package be.ugent.mydigipill.recyclerAdapters

import androidx.recyclerview.widget.ListAdapter
import be.ugent.mydigipill.data.AbstractDataClass

/**
 * This is an abstract implementation of the
 * @see ListAdapter
 * This class is typed for the ease of use when implementing it.
 * The type of this class represents the item that will be visualised by the adapter.
 * this class expects 2 arguments
 * @param diffCallback
 * This will be used as a diffCallback by the ListAdapter when a new list is submitted.
 * @param clickListener
 * This is a function that will be called when the user clicks on an item of the adapter.
 * @author Arthur Deruytter
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractListAdapter<T : AbstractDataClass>(
    diffCallback: AbstractDiffCallback<T>,
    val clickListener: (T) -> Unit
) : ListAdapter<T, AbstractViewHolder<T>>(diffCallback) {

    /**
     * In this function we bind the item to the ViewHolder and add the OnClickListener to the itemView.
     */
    override fun onBindViewHolder(holder: AbstractViewHolder<T>, position: Int) {
        val item = getItem(position)
        holder.bindTo(item)
        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }

    /**
     * Here we override the submitList function of the ListAdapter.
     * We do this for one reason:
     * In the default implementation if you would submit the same List object twice it wouldn't
     * do anything because it does not have 2 different lists to compare.
     * Therefore we override this and make a copy of the list to submit.
     * This way we do not have the "same object" problem.
     * Extra info can be found here.
     * https://stackoverflow.com/questions/49726385/listadapter-not-updating-item-in-recyclerview
     */
    override fun submitList(list: MutableList<T>?) {
        super.submitList(list?.let {
            val newList = ArrayList<T>()
            it.forEach { newList.add(it.getCopy() as T) }
            newList
        })
    }
}
