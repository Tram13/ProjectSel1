package be.ugent.mydigipill.recyclerAdapters

import be.ugent.mydigipill.data.Medication
import org.jetbrains.annotations.NotNull

/**
 * This class is used to implement the
 * @see AbstractDiffCallback with
 * @see Medication as a type
 * @author Arthur Deruytter
 */
class MedicationDiffCallback : AbstractDiffCallback<Medication>() {

    /**
     * Here we check every single visible thing if they are the same.
     * Once there is 1 different we return false.
     */
    override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
        return (oldItem.timeToNext == newItem.timeToNext &&
                oldItem.intake == newItem.intake &&
                oldItem.name == newItem.name &&
                oldItem.note == newItem.note &&
                oldItem.image == newItem.image)
    }

    /**
     * We override this function because we want it to check the id's of medications in stead of the objects.
     */
    override fun areItemsTheSame(
        @NotNull oldItem: Medication,
        @NotNull newItem: Medication
    ): Boolean {
        return oldItem.id == newItem.id
    }

}