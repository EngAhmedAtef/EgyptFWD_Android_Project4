package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var reminderDataItem: ReminderDataItem

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
        reminderDataItem = ReminderDataItem("Title1", "Description1", "Location1", 1.0, 1.0)
        val reminderDTOs = mutableListOf<ReminderDTO>()
        dataSource = FakeDataSource(reminderDTOs)

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    /**
     * Checks if showSnackbarInt's value is not null when the title is empty
     */
    @Test
    fun validateEnteredData_titleIsEmpty_showSnackbarIntValueIsNotNull() {
        // GIVEN - reminder data item's title is empty
        reminderDataItem.title = ""

        // WHEN - Calling validateEnteredData
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - Assert that the snackbar has a value
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(notNullValue()))
    }

    /**
     * Checks if the showSnackbarInt's value is not null when the title is null
     */
    @Test
    fun validateEnteredData_titleIsNull_showSnackbarIntValueIsNotNull() {
        // GIVEN - reminder data item's title is null
        reminderDataItem.title = null

        // WHEN - Calling validateAndSaveReminder
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - Assert that the snackbar has a value
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(notNullValue()))
    }

    /**
     * Checks if the showSnackbarInt's value is not null when the location is null
     */
    @Test
    fun validateEnteredData_locationIsNull_showSnackbarIntValueIsNotNull() {
        // GIVEN - reminder data item's location is null
        reminderDataItem.location = null

        // WHEN - Calling validateEnteredData
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - Assert that the snackbar has a value
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(notNullValue()))
    }

    /**
     * Checks if the showSnackbarInt's value is not null when the location is empty
     */
    @Test
    fun validateEnteredData_locationIsEmpty_showSnackbarIntValueIsNotNull() {
        // GIVEN - reminder data item's location is empty
        reminderDataItem.location = ""

        // WHEN - Calling validateEnteredData
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - Assert that the snackbar has a value
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(notNullValue()))
    }

    /**
     * Checks if the showSnackbarInt's value is null when the location and title are not null/empty
     */
    @Test
    fun validateEnteredData_locationAndTitleNotNullOrEmpty_showSnackbarIntValueIsNull() {
        // GIVEN - A reminderDataItem with all the information

        // WHEN - Calling validateEnteredData
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - Assert that the snackbar's value is null
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(nullValue()))
    }

    /**
     * Checks if the remindersList's size is 1 after saving 1 reminder
     */
    @Test
    fun saveReminder_remindersListIsEmpty_remindersListSizeIsOne() = runTest {
        // GIVEN - A reminder list with exactly 1 reminder
        saveReminderViewModel.saveReminder(reminderDataItem)

        // WHEN - Getting all the reminders casting it to Result.Success as we assume it will work
        val loaded = dataSource.getReminders() as Result.Success

        // THEN - Check if it was actually a Success object with actual data of size 1
        assertThat(loaded.data.size, `is`(1))
    }

    /**
     * Checks if the showLoading is true before and false after
     */
    @Test
    fun saveReminder_showLoading() {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderDataItem)
        assertThat(saveReminderViewModel.showLoading.value, `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.value, `is`(false))
    }

    /**
     * Checks that showToast's value is not null after saving
     */
    @Test
    fun saveReminder_showToast() {
        // GIVEN - an empty reminderList

        // WHEN - Saving a reminder
        saveReminderViewModel.saveReminder(reminderDataItem)

        // THEN - Asserting that the showToast's value is not null
        assertThat(saveReminderViewModel.showToast.value, `is`(notNullValue()))
    }

    /**
     * Checks that navigationCommand's value is not null after saving
     */
    @Test
    fun saveReminder_navigationCommand() {
        // GIVEN - An empty reminderList

        // WHEN - Saving a reminder
        saveReminderViewModel.saveReminder(reminderDataItem)

        // THEN - Asserting that navigationCommand's value is not null
        assertThat(saveReminderViewModel.navigationCommand.value, `is`(notNullValue()))
    }
}