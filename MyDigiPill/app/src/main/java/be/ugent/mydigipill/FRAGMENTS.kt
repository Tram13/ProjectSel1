package be.ugent.mydigipill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import be.ugent.mydigipill.fragments.*

/**
 * This enum has been made to have an easy to use common class for all of our fragments
 * that will be used in the main ViewPager2 of our app.
 * @see ViewPager2
 * Every Enum value needs to implement 5 things.
 * @property string
 * This string is the name of the fragment that will be used in the tabLayout of the app.
 * @property getLayout
 * This is the id of the layout file that will be inflated if needed by the PagerAdapter of the ViewPager
 * @see PagerAdapter
 * @property getId
 * This is the id of the fragment used to more easily find the Fragment class which corresponds to this.
 * @see Fragment
 * @property getViewHolder
 * This function is used to get the specific ViewHolder of the fragment.
 * Most of the fragments don't use this because they have their own Fragment class which does everything.
 * @property getSelf
 * This function is just used to simplify the abstractions a little bit.
 * @author Arthur Deruytter
 */
enum class FRAGMENTS {
    /**
     * This is the enum instance of the OverviewFragment class
     * @see OverviewFragment
     */
    OVERVIEW {

        override var string: String = "overview"

        override fun getLayout(): Int {
            return R.layout.overview_nav_host
        }

        override fun getId(): Int {
            return R.id.overview_nav_host
        }

        override fun getViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup?
        ): MyViewHolder {
            viewHolder = MyViewHolder(MyInnerView(layoutInflater, container))
            return viewHolder
        }

    },

    /**
     * This is the enum instance of the OverviewFragment class
     * this fragment is not used at the moment.
     * In a future release we will possibly add this feature.
     * @see StatisticsFragment
     */
    STATISTICS {

        override var string: String = "statistics"

        override fun toString(): String {
            return "statistics"
        }

        override fun getLayout(): Int {
            return R.layout.statistics_nav_host
        }

        override fun getId(): Int {
            return R.id.statistics_nav_host
        }

        override fun getViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup?
        ): MyViewHolder {
            viewHolder = MyViewHolder(MyInnerView(layoutInflater, container))
            return viewHolder
        }

    },

    /**
     * This is the enum instance of the OverviewFragment class
     * @see SettingsFragment
     */
    SETTINGS {

        override var string: String = "settings"

        override fun toString(): String {
            return "settings"
        }

        override fun getLayout(): Int {
            return R.layout.settings_nav_host
        }

        override fun getId(): Int {
            return R.id.settings_nav_host
        }

        override fun getViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup?
        ): MyViewHolder {
            viewHolder = MyViewHolder(MyInnerView(layoutInflater, container))
            return viewHolder
        }

    },

    /**
     * This is the enum instance of the OverviewFragment class
     * @see ProfileFragment
     */
    PROFILE {

        override var string: String = "profile"

        override fun toString(): String {
            return "profile"
        }

        override fun getLayout(): Int {
            return R.layout.profile_nav_host
        }

        override fun getId(): Int {
            return R.id.profile_nav_host
        }

        override fun getViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup?
        ): MyViewHolder {
            viewHolder = MyViewHolder(MyInnerView(layoutInflater, container))
            return viewHolder
        }

    },

    /**
     * This is the enum instance of the OverviewFragment class
     * @see DayFragment
     */
    DAY {

        override var string: String = "day"

        override fun toString(): String {
            return "day"
        }

        override fun getLayout(): Int {
            return R.layout.day_nav_host
        }

        override fun getId(): Int {
            return R.id.day_nav_host
        }

        override fun getViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup?
        ): MyViewHolder {
            viewHolder = MyViewHolder(MyInnerView(layoutInflater, container))
            return viewHolder
        }

    };

    lateinit var viewHolder: MyViewHolder
    abstract var string: String

    override fun toString(): String {
        return string
    }

    abstract fun getLayout(): Int
    abstract fun getId(): Int
    abstract fun getViewHolder(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): MyViewHolder

    inner class MyInnerView internal constructor(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) : MyView(getSelf(), layoutInflater, container)

    fun getSelf(): FRAGMENTS {
        return this
    }
}