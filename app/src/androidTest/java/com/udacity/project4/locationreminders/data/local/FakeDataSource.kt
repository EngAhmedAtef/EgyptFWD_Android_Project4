package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    var reminderDTOs: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {
    private var shouldReturnError = false

    /**
     * Gets all the reminders in the list.
     * @return returns a Success object that includes a list of all the reminders.
     * Returns a "Reminders not found" error if shouldReturnError is true
     */
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError)
            Result.Error("Reminders not found")
        else
            Result.Success(ArrayList(reminderDTOs))
    }

    /**
     * Saves a reminder into the list.
     * @param reminder A ReminderDTO object to save into the list.
     */
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOs.add(reminder)
    }

    /**
     * Given an ID, it returns a Success object that holds a reminder that has an ID which matches the given ID.
     * @param id The reminder id used to search
     * @return Success object if the reminder is found.
     * Error object with the message "Reminder not found" if the reminder is not found.
     */
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        for (reminderDTO in reminderDTOs) {
            if (reminderDTO.id == id)
                return Result.Success(reminderDTO)
        }
        return Result.Error("Reminder not found")
    }

    /**
     * Deletes all reminders from the list.
     */
    override suspend fun deleteAllReminders() {
        reminderDTOs.clear()
    }
}