package be.ugent.mydigipill.recyclerAdapters

import android.view.ViewGroup
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.inflate

/**
 * This adapter is used for the recyclerview of the overviewFragment.
 * It is an implementation of the
 * @see AbstractListAdapter but with
 * @see Medication as a Type
 * To keep it interesting we switch the cardviews from right to left every 2.
 * This is why our viewType is modulo 2.
 * @property layouts
 * We use this property for the ease of access of our 2 layout files respectively.
 * @param clickListener
 * This adapter expects an OnclickListener as a parameter that will be used as a callback
 * when the user clicks on an item.
 * @author Arthur Deruytter
 */
class OverviewAdapter(clickListener: (Medication) -> Unit) :
    AbstractListAdapter<Medication>(MedicationDiffCallback(), clickListener) {

    private val layouts = mapOf(
        0 to R.layout.overview_cardview_right,
        1 to R.layout.overview_cardview_left
    )

    /**
     * In this function we only need to inflate the view from the viewType
     * and place it in the ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        return MedicationViewHolder(
            parent.inflate(
                layouts[viewType]
                    ?: throw IllegalStateException(
                        "The viewType should always have a layout file in the layouts map"
                    )
            )
        )
    }

    /**
     * In this function we return the modulus of 2 just so that afterwards we can inflate the right layout.
     */
    override fun getItemViewType(position: Int): Int {
        return position % 2
    }

    companion object {
        private const val TAG = "OVERVIEW ADAPTER"
    }
}