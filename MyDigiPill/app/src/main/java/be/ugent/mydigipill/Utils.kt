package be.ugent.mydigipill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import be.ugent.mydigipill.Utils.fragmentsList

/**
 * This file has a lot of small classes and functions that are made for the ease of use in other places.
 * @author Arthur Deruytter
 */

/**
 * This class is used just to hold the views of the fragments from the FRAGMENTS enum.
 * @see FRAGMENTS
 * This makes it easier because it is a common super class for all the fragments.
 */
abstract class MyView(
    val fragment: FRAGMENTS,
    layoutInflater: LayoutInflater,
    container: ViewGroup?
) {
    var view: View = layoutInflater.inflate(fragment.getLayout(), container, false)
}

/**
 * This class is used as a viewHolder for the fragments in the FRAGMENTS enum.
 * @see FRAGMENTS
 * This makes it easier because it is a common super class for all the fragments.
 */
open class MyViewHolder internal constructor(myView: MyView) :
    RecyclerView.ViewHolder(myView.view) {
    /**
     * The default implementation of the bind function is just empty.
     */
    internal open fun bind() {}
}

/**
 * This object has some utility variables that are static and used in multiple places.
 * @property fragmentsList
 * This is a list of the fragments which will be shown by the PagerAdapter
 * @see PagerAdapter
 * The order of the fragments here will also reflect the order of the fragments in the app.
 */
object Utils {
    val fragmentsList = mutableListOf(
        FRAGMENTS.PROFILE,
        FRAGMENTS.OVERVIEW,
        FRAGMENTS.DAY,
        FRAGMENTS.SETTINGS
    )
}
