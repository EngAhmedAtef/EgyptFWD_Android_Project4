package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @Before
    fun initRepository() {
        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Unconfined)
    }

    @After
    fun closeDb() {
        database.close()
    }

    /**
     * Saves 2 reminders in the db. Returns a reminder by ID. Returns all reminders.
     */
    @Test
    fun saveReminder_getReminderById_getReminders() = runTest {
        /// GIVEN - Two reminders
        val reminderDTO1 = ReminderDTO("Title", "Description", "Location", 1.0, 1.0)
        val reminderDTO2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0)

        // WHEN - Save both reminders in the database
        database.reminderDao().saveReminder(reminderDTO1)
        database.reminderDao().saveReminder(reminderDTO2)

        // THEN - Return reminderDTO1 by ID and assert that it's not null and actually returned
        val loaded = database.reminderDao().getReminderById(reminderDTO1.id)
        assertThat(loaded, `is`(notNullValue()))
        assertThat(loaded?.id, `is`(reminderDTO1.id))

        // Return all reminders in the database and assert that the size of the list returned is 2
        val reminderDTOs = database.reminderDao().getReminders()
        assertThat(reminderDTOs.size, `is`(2))
    }

    /**
     * Saves a reminder in the db then deletes all reminders.
     * Returns all reminders from the db and checks if the returned list's size is 0.
     */
    @Test
    fun saveReminder_deleteAllReminders() = runTest {
        // GIVEN - A database with 1 reminder.
        database.reminderDao().saveReminder(
            ReminderDTO("Title", "Description", "Location", 1.0, 1.0)
        )

        // WHEN - Delete all reminders
        database.reminderDao().deleteAllReminders()

        // THEN - Retrieve all reminders and check if the size is 0
        val loaded = database.reminderDao().getReminders()
        assertThat(loaded.size, `is`(0))
    }

}