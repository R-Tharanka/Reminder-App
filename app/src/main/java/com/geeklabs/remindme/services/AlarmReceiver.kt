package com.geeklabs.remindme.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// BroadcastReceiver to handle alarm triggers for reminders
class AlarmReceiver : BroadcastReceiver() {

    // Called when the alarm is received (triggered)
    override fun onReceive(context: Context, intent: Intent?) {

        // Extract the reminder ID from the intent
        val reminderId = intent?.getLongExtra("reminderId", 0)
        // Check if the service is already running
        val isServiceRunning = intent?.getBooleanExtra("isServiceRunning", false)

        // Create an intent to start the ReminderService
        val reminderServiceIntent = Intent(context, ReminderService::class.java)
        reminderServiceIntent.putExtra("reminderId", reminderId)

        // Start the service if it is not already running
        if (!isServiceRunning!!) {
            context.startService(reminderServiceIntent)
        }
    }
}
