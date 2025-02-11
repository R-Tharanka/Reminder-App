package com.geeklabs.remindme.database

import android.content.Context
import android.content.SharedPreferences
import com.geeklabs.remindme.models.Reminder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.atomic.AtomicLong

class SharedPreferencesHandler(context: Context) {

    // Initialize SharedPreferences and Gson for storing/removing reminders as JSON
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("RemindMePrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    companion object {
        // Keys for storing reminders and ID counter
        private const val REMINDER_KEY = "ReminderList"
        private const val ID_COUNTER = "ReminderIdCounter"
    }

    // AtomicLong for generating unique reminder IDs
    private val idCounter: AtomicLong by lazy {
        AtomicLong(sharedPreferences.getLong(ID_COUNTER, 0))
    }

    // Generates a unique ID for new reminders
    private fun generateId(): Long {
        val newId = idCounter.incrementAndGet()
        editor.putLong(ID_COUNTER, newId).apply()
        return newId
    }

    // Saves a reminder to SharedPreferences, generates ID if the reminder is new
    fun saveReminder(reminder: Reminder): Boolean {
        val reminderList = getAllReminders().toMutableList()

        // Assign ID to the reminder if it's new
        if (reminder.id == 0L) {
            reminder.id = generateId()
        }

        // Add the reminder and save the updated list
        reminderList.add(reminder)
        return saveReminderList(reminderList)
    }

    // Retrieves a reminder by its ID
    fun getReminderById(id: Long): Reminder? {
        val reminderList = getAllReminders()
        return reminderList.find { it.id == id }
    }

    // Updates an existing reminder in SharedPreferences
    fun updateReminder(updatedReminder: Reminder): Boolean {
        val reminderList = getAllReminders().toMutableList()
        val index = reminderList.indexOfFirst { it.id == updatedReminder.id }

        // Replace the existing reminder with the updated one
        if (index != -1) {
            reminderList[index] = updatedReminder
            return saveReminderList(reminderList)
        }
        return false
    }

    // Deletes a reminder by its ID
    fun deleteReminderById(id: Long): Boolean {
        val reminderList = getAllReminders().toMutableList()
        // Filter out the reminder to delete
        val updatedList = reminderList.filter { it.id != id }
        return saveReminderList(updatedList)
    }

    // Retrieves all reminders from SharedPreferences
    fun getAllReminders(): List<Reminder> {
        val jsonString = sharedPreferences.getString(REMINDER_KEY, null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<Reminder>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    // Saves the list of reminders as a JSON string in SharedPreferences
    private fun saveReminderList(reminderList: List<Reminder>): Boolean {
        val jsonString = gson.toJson(reminderList)
        editor.putString(REMINDER_KEY, jsonString)
        return editor.commit() // Commit the changes to persist data
    }

    // Clears all reminders and resets the ID counter
    fun clearAllReminders(): Boolean {
        editor.remove(REMINDER_KEY)
        editor.putLong(ID_COUNTER, 0L)
        return editor.commit()
    }
}
