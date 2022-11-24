package com.udacity.project4.locationreminders.reminderslist

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private val title = "Title"
    // Create a reminder and add it into a list
    private val reminderDTO = ReminderDTO(title, "Description", "Location", 1.0, 1.0)
    private val reminderDTOs = mutableListOf(reminderDTO)

    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        // Create a fake data source with the reminders list that contains one reminder
        dataSource = FakeDataSource(reminderDTOs)

        // Stop koin
        stopKoin()

        // Create a koin module that creates a RemindersListViewModel with our fake data source
        val module = module {
            viewModel {
                RemindersListViewModel(getApplicationContext(), dataSource)
            }
        }

        // Start koin with our module
        startKoin {
            modules(module)
        }
    }

    @Test
    fun navigateToAddReminder() {
        // GIVEN - On the reminder list screen with 1 reminder
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - CLick on the add reminder FAB
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        // THEN - Verify that we navigated to the SaveReminderFragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun reminderList_DisplayedInUi() {
        // GIVEN - On the ReminderListFragment with 1 reminder
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        // WHEN - Scroll to the item that has a title which matches our title variable
        // THEN - It succeeds if it finds the item
        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(title))
                )
            )
    }
}