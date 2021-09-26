package be.ugent.mydigipill.recyclerAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import be.ugent.mydigipill.MyViewHolder
import be.ugent.mydigipill.Utils

/**
 * This is a simple adapter implementation that uses the FRAGMENTS enum class
 * and the utility viewHolder class to inflate and show fragments.
 * @property getItemViewType
 * We use this function to set the viewType to the position in the list so that afterwards in
 * @property onCreateViewHolder
 * we can use this to get the correct fragment from the Utils fragments list.
 * @see Utils.fragmentsList
 * @author Arthur Deruytter
 */
class PagerAdapter : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return Utils.fragmentsList[viewType].getViewHolder(
            LayoutInflater.from(parent.context),
            parent
        )
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    /**
     * This is a trivial implementation
     */
    override fun getItemCount(): Int {
        return Utils.fragmentsList.size
    }

    /**
     * We actually call the bind function here although none of the fragments implement the function.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind()
    }
}