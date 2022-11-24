package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var reminderDTOs: MutableList<ReminderDTO>

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @After
    fun closeKoin() {
        stopKoin()
    }

    @Before
    fun setupViewModel() {
        val reminderDTO1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0)
        val reminderDTO2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0)
        val reminderDTO3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0)
        reminderDTOs = mutableListOf(reminderDTO1, reminderDTO2, reminderDTO3)
        dataSource = FakeDataSource(reminderDTOs)

        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    /**
     * Checks if the ViewModel successfully loads all the reminders
     */
    @Test
    fun loadReminders_remindersListDataUpdated() {
        // GIVEN - A datasource with 3 reminders

        // WHEN - Loading all reminders
        remindersListViewModel.loadReminders()

        // THEN - The loaded reminders list's size is 3
        assertThat(remindersListViewModel.remindersList.value?.size, `is`(reminderDTOs.size))
    }

    /**
     * Checks if the ViewModel successfully shows that it's loading
     */
    @Test
    fun loadReminders_showLoading() {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.value, `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.value, `is`(false))
    }

    /**
     * Checks if the ViewModel successfully shows/returns an error
     */
    @Test
    fun loadReminders_showError() {
        dataSource.setShouldReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.value, `is`(notNullValue()))
    }

    /**
     * Checks if ShowNoData is true if the remindersList is empty
     */
    @Test
    fun invalidateShowNoData_remindersListValueIsEmpty_ShowNoDataIsTrue() = runTest {
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    /**
     * Checks if ShowNoData is true if the remindersList is null
     */
    @Test
    fun invalidateShowNoData_remindersListValueIsNull_ShowNoDataIsTrue() {
        remindersListViewModel.remindersList.value = null
        remindersListViewModel.invalidateShowNoData()
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }
}