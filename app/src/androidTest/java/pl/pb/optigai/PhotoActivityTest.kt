package pl.pb.optigai.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import pl.pb.optigai.R
import org.hamcrest.CoreMatchers.not

@RunWith(AndroidJUnit4::class)
class PhotoActivityTest {

    private fun launchWithIndex(index: Int): ActivityScenario<PhotoActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), PhotoActivity::class.java)
        intent.putExtra("position", index)
        return ActivityScenario.launch(intent)

    }

    @Test
    fun activity_starts_and_ui_is_visible() {
        ActivityScenario.launch(PhotoActivity::class.java).use {
            onView(withId(R.id.previewImageView)).check(matches(isDisplayed()))
            onView(withId(R.id.leftArrow)).check(matches(isDisplayed()))
            onView(withId(R.id.rightArrow)).check(matches(isDisplayed()))
            onView(withId(R.id.middleButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun loads_images_and_sets_initial_index() {
        launchWithIndex(0).use { scenario ->
            scenario.onActivity { activity ->
                assert(activity.testImages.isNotEmpty())
                assertEquals(0, activity.testCurrentIndex)
            }
        }
    }

    @Test
    fun leftArrow_is_disabled_on_first_image() {
        launchWithIndex(0).use {
            onView(withId(R.id.leftArrow))
                .check(matches(not(isEnabled())))
        }
    }

    @Test
    fun rightArrow_moves_to_next_image() {
        launchWithIndex(0).use { scenario ->
            onView(withId(R.id.rightArrow)).perform(click())

            scenario.onActivity { activity ->
                assertEquals(1, activity.testCurrentIndex)
            }
        }
    }

    @Test
    fun leftArrow_moves_back() {
        launchWithIndex(1).use { scenario ->
            onView(withId(R.id.leftArrow)).perform(click())

            scenario.onActivity { activity ->
                assertEquals(0, activity.testCurrentIndex)
            }
        }
    }

    @Test
    fun middleButton_opens_AnalysisActivity() {
        launchWithIndex(0).use {
            onView(withId(R.id.middleButton)).perform(click())
            Intents.init()
            onView(withId(R.id.middleButton)).perform(click())
            Intents.intended(hasComponent(AnalysisActivity::class.java.name))
            Intents.release()

        }
    }
}