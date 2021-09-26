package be.ugent.mydigipill.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import be.ugent.mydigipill.R

/**
 * We have decided not to use this fragment but left it here for future implementations.
 */
class StatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_statistics, container, false)
        //Als men dit in een merge tegenkomt, ALTIJD DE ANDERE NEMEN dit is een placeholder
        return view
    }
}