package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminderDTOs: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {
    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(shouldReturnError)
            Result.Error("Reminders not found")
        else
            Result.Success(ArrayList(reminderDTOs))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOs.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        for (reminderDTO in reminderDTOs) {
            if (reminderDTO.id == id)
                return Result.Success(reminderDTO)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminderDTOs.clear()
    }
}