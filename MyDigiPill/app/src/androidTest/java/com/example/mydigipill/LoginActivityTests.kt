package com.example.mydigipill

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.ugent.mydigipill.LoginActivity
import be.ugent.mydigipill.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTests {

    private val testUser: String = "TestUser"
    private val testEmail: String = "Testemail@testemail.com"
    private val testPassword: String = "Testpassword"

    @After
    fun logoutSoThatWeCanLoginAgain() {
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun afterRegisterUserThenUserShouldBeLoggedIn() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.email)).perform(typeText(testEmail))
        onView(withId(R.id.password)).perform(typeText(testPassword))
        onView(withId(R.id.username)).perform(typeText(testUser))
        pressBack()
        onView(withId(R.id.continueButton)).perform(click())
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        assert(user != null)
        assert(user?.displayName == testUser)
        assert(user?.email == testEmail)
    }

    @Test
    fun ifUserTriesToRegisterWithNotAValidEmailThenRegistrationShouldFail() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.email)).perform(typeText("testemail.com"))
        onView(withId(R.id.password)).perform(typeText(testPassword))
        onView(withId(R.id.username)).perform(typeText(testUser))
        pressBack()
        onView(withId(R.id.continueButton)).perform(click())
        assert(FirebaseAuth.getInstance().currentUser == null)
    }

    @Test
    fun ifLoginWithWrongPasswordShouldNotBeAbleToLoginWithWrongPassword() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.signInButton)).perform(click())
        onView(withId(R.id.email)).perform(typeText(testEmail))
        onView(withId(R.id.password)).perform(typeText("wrongpassword"))
        pressBack()
        onView(withId(R.id.continueButton)).perform(click())
        assert(FirebaseAuth.getInstance().currentUser == null)
    }

    @Test
    fun ifLoggingInThenUserShouldBeLoggedIn() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.signInButton)).perform(click())
        onView(withId(R.id.email)).perform(typeText(testEmail))
        onView(withId(R.id.password)).perform(typeText(testPassword))
        pressBack()
        onView(withId(R.id.continueButton)).perform(click())
        val user = FirebaseAuth.getInstance().currentUser
        assert(user != null)
        assert(user?.displayName == testUser)
        assert(user?.email == testEmail)
    }

}