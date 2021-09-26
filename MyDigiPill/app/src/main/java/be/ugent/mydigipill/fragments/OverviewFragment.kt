package be.ugent.mydigipill.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.ugent.mydigipill.FRAGMENTS
import be.ugent.mydigipill.MainActivity
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import be.ugent.mydigipill.notifications.NotificationHandler
import be.ugent.mydigipill.recyclerAdapters.OverviewAdapter
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_overview.*

class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by activityViewModels()
    private lateinit var adapter: OverviewAdapter
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_overview, container, false)

        adapter = OverviewAdapter { medication: Medication ->
            handleItemInRecyclerViewClicked(medication)
        }

        /**
         * het keyboard moet automatisch weggaan als je terug op de overview komt
         * we gebruiken hier een deel code can commonutils
         * Let wel op dat deze niet checkt op null!
         */
        val newFocus = requireActivity().currentFocus
        val newContext = requireActivity().baseContext
        if (newFocus != null && newContext != null) {
            hideKeyboard(newContext, newFocus)
        }

        viewModel.showBottomsheetDialog.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                val action =
                    OverviewFragmentDirections.actionOverviewFragmentToBottomSheetDialog(it)
                findNavController().navigate(action)
                viewModel.showBottomsheetDialog.value = null
            }
        })

        /**
         * set the display items for the adapter
         * this also asks for an update of ingestion timers
         * this will also check if the user is searching and
         * adapt accordingly
         * finally this will sort the medications based on time left to next alarm
         */
        viewModel.medicationList.observe(viewLifecycleOwner, Observer {
            // In case of opening the app by notification, this will be the ID of the Medication the notification is referring to
            handleIfOpenedByNotificaction() // Will open detailed view if the app was launched by a notification
            viewModel.getNextIngestions() // Update the ingestion-timers

            val displayItems: MutableList<Medication> =
                if (!viewModel.searchQuery.value.isNullOrEmpty()) { // If searching: filter list
                    filterDisplayItems(it)
                } else { // Else: return the full list
                    it
                }
            displayItems.sort() // Sort by ingestion
            adapter.submitList(displayItems)
        })

        mainActivity = requireActivity() as MainActivity

        FRAGMENTS.OVERVIEW.string = "overview"
        mainActivity.editTabLayoutMediator()

        val width = resources.configuration.screenWidthDp
        var spanCount = width / 300
        if (spanCount == 0) spanCount = 1
        view.findViewById<RecyclerView>(R.id.recycler).layoutManager =
            GridLayoutManager(context, spanCount)
        view.findViewById<RecyclerView>(R.id.recycler).adapter = adapter

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        mainActivity.setSupportActionBar(toolbar)
        mainActivity.supportActionBar?.title =
            getString(R.string.app_name)

        return view
    }

    /**
     * opens the a medication bottomsheet
     */
    private fun handleItemInRecyclerViewClicked(medication: Medication) {
        medication.id?.let {
            val action = OverviewFragmentDirections.actionOverviewFragmentToBottomSheetDialog(it)
            findNavController().navigate(action)
        } ?: throw IllegalStateException("All medication should have an id.")
    }

    /**
     * set a listener for the addbutton
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_button.setOnClickListener {
            findNavController().navigate(R.id.action_overviewFragment_to_AddFragment)
        }
    }

    /**
     * Will open detailed view if the app was launched by a notification
     */
    private fun handleIfOpenedByNotificaction() {
        val notID = viewModel.selectedFromNotificationID
        if (notID != null) { // If opened by clicking on a notification
            val med = viewModel.findMedicationById(notID)
            if (med != null) { // If the database has already downloaded the required Medication object
                handleItemInRecyclerViewClicked(med)
            }
        }
    }

    /**
     * this filters the items to create a displaylist
     * when the user is searching
     * @see viewModel to get the search query
     */
    private fun filterDisplayItems(list: MutableList<Medication>): MutableList<Medication> {
        //kijk voor elk item of de query in de naam zit
        return viewModel.filterList(list)
    }

    /**
     * const values
     */
    private companion object {
        private const val TAG = "OVERVIEWFRAGMENT"
    }

    /**
     * implementation for the search option
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overview_search, menu)

        val searchItem = menu.findItem(R.id.overview_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //beide returnen false voor default behaviour
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQuery.postValue(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery.postValue(newText)
                return false
            }
        })
    }

}
