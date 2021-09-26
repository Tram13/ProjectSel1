package be.ugent.mydigipill

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import be.ugent.mydigipill.notifications.NotificationHandler
import be.ugent.mydigipill.receivers.NotificationReceiver
import be.ugent.mydigipill.recyclerAdapters.PagerAdapter
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import be.ugent.mydigipill.viewmodels.OverviewViewModelFactory
import com.androidnetworking.AndroidNetworking
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jacksonandroidnetworking.JacksonParserFactory
import net.danlew.android.joda.JodaTimeAndroid

/**
 * This is our Main activity it has all of our view logic except for the login and sign up.
 * We use a viewPager2 to display all of our fragments in this view.
 * @property viewPager2
 * This is also the Activity that gets launched from a notification.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mediator: TabLayoutMediator
    private lateinit var tabLayout: TabLayout
    private lateinit var overviewViewModel: OverviewViewModel
    private lateinit var viewPager2: ViewPager2
    private lateinit var notificationHandler: NotificationHandler

    private var user: FirebaseUser? =
        null //dit moet nullable zijn want get current user kan null terug geven.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this) // Creates the necessary resources for the JodeTime Library
        setContentView(R.layout.activity_main)

        /**
         * Initialize the AndroidNetworking library to the right values.
         */
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())

        /**
         * Hier wordt nagekeken of de user geAuthenticeerd is.
         * Indien niet wordt de user naar de LoginActivity gestuurd
         */
        user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            //Er is nog geen user signed in.
            startActivity(Intent(this, LoginActivity::class.java))
            //hier wordt de user ingelogd/ geregistreerd.
            finish()
            return
        }

        /**
         * create the overviewViewmodel with correct strings
         */
        overviewViewModel =
            ViewModelProvider(
                this,
                OverviewViewModelFactory(
                    getString(R.string.next_ingestion_message),
                    getString(R.string.No_alarms_enabled)
                )
            ).get(OverviewViewModel::class.java)

        // Will tell the overviewViewModel that the app was launched by clicking a notification
        processIntent()
        /**
         * Alles om de notificaties te schedulen en te kunnen laten afgaan.
         */
        overviewViewModel.medicationList.observe(this, Observer {
            if (overviewViewModel.selectedFromNotificationID.isNullOrBlank()) { // Voorkomt dat een notificatie opnieuw wordt gestuurd indien deze wordt geopenend binnen één minuut.
                notificationHandler.scheduleAllNotifications(it)
            }
        })
        notificationHandler = NotificationHandler(this)


        overviewViewModel.searchQuery.observe(this, Observer {
            // Force update de list naar zichzelf, dan triggert de Listener die de view aanpast met de juiste zoekstring
            overviewViewModel.medicationList.postValue(overviewViewModel.medicationList.value)
        })


        viewPager2 = findViewById(R.id.view_pager)
        viewPager2.adapter = PagerAdapter()
        viewPager2.setCurrentItem(Utils.fragmentsList.indexOf(FRAGMENTS.OVERVIEW), false)

        tabLayout = findViewById(R.id.tabs)

        mediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = Utils.fragmentsList[position].toString()
        }
        mediator.attach()

        overviewViewModel.setStart()
    }

    /**
     * This is a hulp function that we can call from our other fragments
     * to update the text in the tablayout on the bottom of the screen.
     */
    fun editTabLayoutMediator() {
        mediator.detach()
        mediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = Utils.fragmentsList[position].toString()
        }
        mediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overviewViewModel.isInitialized) {
            overviewViewModel.cancelTimer()
        }
    }

    /**
     * implementation for searching in the medication overview
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.overview_search, menu)

        val searchItem = menu?.findItem(R.id.overview_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //beide returnen false voor default behaviour
            override fun onQueryTextSubmit(query: String?): Boolean {
                overviewViewModel.searchQuery.postValue(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                overviewViewModel.searchQuery.postValue(newText)
                return false
            }
        })
        return true
    }

    /**
     * Will tell the overviewViewModel that the app was launched by clicking a notification
     */
    private fun processIntent() {
        overviewViewModel.selectedFromNotificationID =
            intent.getStringExtra(NotificationReceiver.NOTIFICATION_STRINGID)
        overviewViewModel.selectedFromNotificationAlarmString =
            intent.getStringExtra(NotificationReceiver.NOTIFICATION_ALARMSTRING)
    }

}
