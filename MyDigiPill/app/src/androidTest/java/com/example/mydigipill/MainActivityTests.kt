package com.example.mydigipill

import android.util.Log
import android.view.View
import android.widget.TimePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.ugent.mydigipill.MainActivity
import be.ugent.mydigipill.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class MainActivityTests {

    private val uid = "GGnqBeM8BBMNzHI2qlAitSCWsZh1"

    private val testEmail: String = "Testemail@testemail.com"
    private val testPassword: String = "Testpassword"

    private var reminderName: String = "Paracetamol"
    private var reminderDescription: String = "For my back pain"
    private var reminderIngestion: String = "Orally"
    private var daysList: ArrayList<Int> = ArrayList()
    private var random: Random = Random(1)

    @Before
    fun makeDaysList() {
        daysList.add(R.id.monday_button)
        daysList.add(R.id.tuesday_button)
        daysList.add(R.id.wednesday_button)
        daysList.add(R.id.thursday_button)
        daysList.add(R.id.friday_button)
        daysList.add(R.id.saturday_button)
        daysList.add(R.id.sunday_button)

    }

    @After
    fun clearDB() {
        val data = hashMapOf(
            "path" to "/users/$uid"
        )
        val task = FirebaseFunctions.getInstance("europe-west3")
            .getHttpsCallable("recursiveDelete").call(data)
            .addOnFailureListener {
                Log.d("MAINTEST", "Recursive delete failed")
                Log.d("MAINTEST", "exception: ${it.message}")
            }.addOnSuccessListener {
                Log.d("MAINTEST", "Recursive delete success")
            }
        while (!task.isComplete) {

        }
    }


    @Test
    fun ifNotLoggedInShouldStartLoginActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        ActivityScenario.launch(MainActivity::class.java)
        if (user == null) {
            onView(withId(R.id.signInButton))
                .check(matches(withText(R.string.action_login)))
        } else {
            onView(withId(R.id.add_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun afterAddMedicationShouldAppearInList() {
        ActivityScenario.launch(MainActivity::class.java)
        if (FirebaseAuth.getInstance().currentUser == null) {
            login()
        }
        addMedicationFromOverviewFragmentSaving(1, 0)
        onView(withId(R.id.recycler)).check(
            matches(
                object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
                    override fun describeTo(description: Description?) {
                        description?.appendText(
                            "Checks that the matcher childMatcher matches" +
                                    " with a view having a given id inside a RecyclerView's item (given its position)"
                        )
                    }

                    override fun matchesSafely(item: RecyclerView?): Boolean {
                        val viewHolder = item?.findViewHolderForAdapterPosition(0)
                        val matcher = isDisplayed()
                        return viewHolder != null && matcher.matches(viewHolder.itemView)
                    }
                })
        )
    }

    private fun login() {
        onView(withId(R.id.signInButton)).perform(click())
        onView(withId(R.id.email)).perform(typeText(testEmail))
        onView(withId(R.id.password)).perform(typeText(testPassword))
        pressBack()
        onView(withId(R.id.continueButton)).perform(click())
    }

    @Test
    fun afterAddMedicationALotShouldStillBeAbleToPressAddButton() {
        ActivityScenario.launch(MainActivity::class.java)
        if (FirebaseAuth.getInstance().currentUser == null) {
            login()
        }
        for (i in 0 until 4) {
            addMedicationFromOverviewFragmentSaving(1, 0)
        }
        onView(withId(R.id.add_button))
            .check(matches(isDisplayed()))
    }

    private fun addMedicationFromOverviewFragmentSaving(
        amountAlarms: Int = 5,
        amountMaxSwitchOn: Int = 2
    ) {
        addMedicationFromOverviewFragmentNotSaving(amountAlarms, amountMaxSwitchOn)
        onView(withId(R.id.saveButton)).perform(click())
    }

    private fun addMedicationFromOverviewFragmentNotSaving(
        amountAlarms: Int = 5,
        amountMaxSwitchOn: Int = 2
    ) {
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.editText_name)).perform(typeText(reminderName))
        onView(withId(R.id.editText_description)).perform(typeText(reminderDescription))
        onView(withId(R.id.editText_ingestion)).perform(typeText(reminderIngestion))
        for (i in 0 until amountAlarms) {
            addReminderFromAddFragmentFragmentSaving()
            if (amountMaxSwitchOn == 0) continue
            if (getRandomBoolean(1.0 / amountMaxSwitchOn)) {
                onView(withId(R.id.alarmRecycler)).perform(
                    RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i)
                ).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(i,
                        object : ViewAction {
                            override fun getDescription(): String {
                                return "Perform action on the view whose id is passed in"
                            }

                            override fun getConstraints(): Matcher<View> {
                                return allOf(isDisplayed(), isAssignableFrom(View::class.java))
                            }

                            override fun perform(uiController: UiController?, view: View?) {
                                view?.let {
                                    val child: View = it.findViewById(R.id.switch1)
                                    click().perform(uiController, child)
                                }
                            }
                        }
                    )
                )
            }
        }
    }

    private fun getRandomBoolean(p: Double): Boolean {
        return random.nextDouble() < p
    }

    private fun addReminderFromAddFragmentFragmentSaving(
        hour: Int = random.nextInt(24),
        minutes: Int = random.nextInt(60),
        amountDays: Int = random.nextInt(daysList.size - 1) + 1
    ) {
        addReminderFromAddFragmentFragmentNotSaving(hour, minutes, amountDays)
        onView(withId(R.id.save_button)).perform(click())
    }


    private fun addReminderFromAddFragmentFragmentNotSaving(
        hour: Int = random.nextInt(24),
        minutes: Int = random.nextInt(60),
        amountDays: Int = random.nextInt(daysList.size - 1) + 1
    ) {
        onView(withId(R.id.addButton))
            .perform(click())
        //klikt op een willekeurig aantal dagen om de test zo unbiased mogelijk te maken.
        val clicked = arrayListOf<Int>()
        for (i in 0 until amountDays) {
            val j = random.nextInt(7)
            if (clicked.contains(j)) {
                continue
            }
            onView(withId(daysList[j]))
                .perform(click())
            clicked.add(j)
        }
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name))).perform(
            object : ViewAction {
                override fun getDescription(): String {
                    return "Set the passed time into the TimePicker"
                }

                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(TimePicker::class.java)
                }

                override fun perform(uiController: UiController?, view: View?) {
                    val tp: TimePicker = view as TimePicker
                    tp.hour = hour
                    tp.minute = minutes
                }
            }
        )
    }

}
