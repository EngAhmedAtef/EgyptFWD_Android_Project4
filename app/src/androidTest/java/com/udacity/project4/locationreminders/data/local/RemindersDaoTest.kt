package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    /**
     * Saves a reminder in the database then attempts to retrieve it by ID.
     */
    @Test
    fun saveReminderAndGetById() = runTest {
        // GIVEN - A db with 1 reminder
        val reminderDTO = ReminderDTO("Title", "Description", "Location", 1.0, 1.0)
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - Get the reminder by id from the db
        val loaded = database.reminderDao().getReminderById(reminderDTO.id)

        // THEN - the loaded data contains expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.title, `is`(reminderDTO.title))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
    }

    /**
     * Saves 3 reminders into the database and attempts to retrieve the reminders. Checks if the retrieved list's size is the same
     * as the list saved into the database.
     */
    @Test
    fun saveRemindersAndGetReminders() = runTest {
        // GIVEN - A db with 3 reminders
        val reminderDTO1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0)
        val reminderDTO2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0)
        val reminderDTO3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0)

        val reminderDTOs = listOf(reminderDTO1, reminderDTO2, reminderDTO3)

        database.reminderDao().saveReminder(reminderDTO1)
        database.reminderDao().saveReminder(reminderDTO2)
        database.reminderDao().saveReminder(reminderDTO3)

        // WHEN - Load all the reminders in the db
        val loaded = database.reminderDao().getReminders()

        // THEN - The retrieved list's size is the same as the input list.
        assertThat(loaded, notNullValue())
        assertThat(loaded.size, `is`(reminderDTOs.size))
    }

    /**
     * Saves 2 reminders into the db and then retrieves all the reminders from the db.
     * Succeeds if the retrieved list's size is 0.
     */
    @Test
    fun saveRemindersAndDeleteReminders() = runTest {
        // GIVEN - 2 reminders saved in a db.
        val reminderDTO1 = ReminderDTO("Title", "Description", "Location", 1.0, 1.0)
        val reminderDTO2 = ReminderDTO("Title", "Description", "Location", 1.0, 1.0)
        database.reminderDao().saveReminder(reminderDTO1)
        database.reminderDao().saveReminder(reminderDTO2)

        // WHEN - Delete all reminders
        database.reminderDao().deleteAllReminders()
        val loaded = database.reminderDao().getReminders()

        // THEN - retrieved reminders list's size is 0
        assertThat(loaded, notNullValue())
        assertThat(loaded.size, `is`(0))
    }
}