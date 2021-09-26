package be.ugent.mydigipill.recyclerAdapters

import android.view.View
import android.view.ViewGroup

interface AbstractSpinnerItem {
    fun getId(): Long
    fun getView(convertView: View?, parent: ViewGroup): View

}