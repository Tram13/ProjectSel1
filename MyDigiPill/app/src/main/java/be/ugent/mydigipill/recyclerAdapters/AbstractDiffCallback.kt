package be.ugent.mydigipill.recyclerAdapters

import androidx.recyclerview.widget.DiffUtil
import org.jetbrains.annotations.NotNull

/**
 * This is a super simple implementation of a
 * @see DiffUtil.ItemCallback
 * @author Arthur Deruytter
 */
abstract class AbstractDiffCallback<T> : DiffUtil.ItemCallback<T>() {
    /**
     * This function just checks if the objects are the same.
     */
    override fun areItemsTheSame(@NotNull oldItem: T, @NotNull newItem: T): Boolean {
        return oldItem == newItem
    }
}