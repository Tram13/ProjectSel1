package be.ugent.mydigipill.recyclerAdapters

import be.ugent.mydigipill.data.Alarm

/**
 * This class is used to implement the
 * @see AbstractDiffCallback with
 * @see Alarm as a type
 * @author Arthur Deruytter
 */
class AlarmDiffCallback : AbstractDiffCallback<Alarm>() {

    override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem.id == newItem.id
    }
}