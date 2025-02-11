package com.geeklabs.remindme.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.geeklabs.remindme.R
import com.geeklabs.remindme.adapters.ReminderAdapter
import com.geeklabs.remindme.database.SharedPreferencesHandler
import com.geeklabs.remindme.models.Reminder
import com.geeklabs.remindme.services.AlarmReceiver
import com.geeklabs.remindme.services.ReminderService
import com.geeklabs.remindme.utils.Util
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), ReminderAdapter.OnItemClickListener {

    // SharedPreferencesHandler to manage reminders
    private lateinit var reminderPreferences: SharedPreferencesHandler
    // Adapter to handle displaying reminders in a RecyclerView
    private lateinit var adapter: ReminderAdapter
    // Mutable list to store all reminders
    private var reminderList = mutableListOf<Reminder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAds() // Initialize ads using MobileAds SDK
        reminderPreferences = SharedPreferencesHandler(this) // Initialize the SharedPreferences handler
        adapter = ReminderAdapter(this) // Set up the adapter for the RecyclerView
        recycler_view_reminder.adapter = adapter // Attach the adapter to the RecyclerView

        // Retrieve all reminders from the database (SharedPreferences) and update the UI
        getAllRemindersFromDB()

        // When the "add reminder" button is clicked, navigate to the AddReminderActivity
        addReminderButton.setOnClickListener {
            val reminderIntent = Intent(this, AddReminderActivity::class.java)
            startActivity(reminderIntent)
        }

        // Handle notifications that may have triggered the activity
        val from = intent.getStringExtra("from")
        if (from == "Notification") {
            val reminderId = intent.getLongExtra("reminderId", 0)
            reminderPreferences.getReminderById(reminderId)?.let {
                showReminderAlert(it) // Show an alert if a reminder is found
            }
        }

        // Set up search functionality for filtering reminders
        searchET.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterData(newText) // Filter reminders based on the search query
                return false
            }
        })
    }

    // Initialize MobileAds (AdMob)
    private fun initAds() {
        MobileAds.initialize(this) { }
    }

    // Filter the reminder list based on a search query
    private fun filterData(query: String) {
        // Filter list based on title or description containing the search query (case-insensitive)
        val finalList = if (query.isEmpty()) reminderList else reminderList.filter {
            it.title.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())) ||
                    it.description.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))
        }
        updateList(finalList.toMutableList()) // Update the list with filtered data
    }

    // Update the RecyclerView list and manage visibility for "no data" views
    private fun updateList(finalList: MutableList<Reminder>) {
        adapter.reminderList = finalList // Update adapter data
        adapter.notifyDataSetChanged() // Notify the adapter that data has changed

        // Show or hide the RecyclerView and "no data" text based on the presence of reminders
        if (finalList.isNotEmpty()) {
            recycler_view_reminder.visibility = View.VISIBLE
            noData.visibility = View.GONE
        } else {
            recycler_view_reminder.visibility = View.GONE
            noData.visibility = View.VISIBLE
        }
    }

    // Fetch all reminders from SharedPreferences and update the UI
    private fun getAllRemindersFromDB() {
        reminderList = reminderPreferences.getAllReminders().toMutableList() // Retrieve reminders
        updateList(reminderList) // Update the list
    }

    override fun onResume() {
        super.onResume()
        getAllRemindersFromDB() // Refresh the reminders when the activity resumes
    }

    // Show an alert dialog for a selected reminder
    private fun showReminderAlert(reminder: Reminder) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(reminder.title) // Set the title of the reminder in the dialog
        builder.setMessage(reminder.description) // Show the description of the reminder

        // Set up the "STOP ALARM" button
        builder.setPositiveButton("STOP ALARM") { dialog, _ ->
            Util.showToastMessage(this, "Your alarm has been stopped") // Notify the user
            dialog.dismiss()
            stopAlarm() // Stop the alarm
            stopReminderService() // Stop the reminder service
        }

        val alertDialog = builder.create() // Create and show the dialog
        alertDialog.show()
    }

    // Stop the active alarm
    private fun stopAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0) // Create a pending intent for the alarm
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender) // Cancel the alarm
    }

    // Stop the ReminderService that may be running
    private fun stopReminderService() {
        val reminderService = Intent(this, ReminderService::class.java)
        stopService(reminderService) // Stop the service
    }

    // Handle item click events for reminders
    override fun onItemClick(reminder: Reminder, view: View, position: Int) {
        // Create a popup menu when a reminder is clicked
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        // Handle menu item clicks (update or delete reminder)
        popupMenu.setOnMenuItemClickListener {
            when (it.title) {
                getString(R.string.update) -> {
                    // Navigate to AddReminderActivity to update the reminder
                    startActivity(
                        Intent(this, AddReminderActivity::class.java)
                            .putExtra("reminder", reminder)
                    )
                }
                getString(R.string.delete) -> {
                    // Delete the reminder from SharedPreferences and update the list
                    reminderPreferences.deleteReminderById(reminder.id)
                    getAllRemindersFromDB() // Refresh the list
                }
            }
            true
        }
        popupMenu.show() // Show the popup menu
    }
}
