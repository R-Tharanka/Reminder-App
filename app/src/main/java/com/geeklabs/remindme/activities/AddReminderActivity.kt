package com.geeklabs.remindme.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.geeklabs.remindme.R
import com.geeklabs.remindme.database.SharedPreferencesHandler
import com.geeklabs.remindme.models.Reminder
import com.geeklabs.remindme.services.AlarmReceiver
import com.geeklabs.remindme.services.ReminderService
import com.geeklabs.remindme.utils.Util
import kotlinx.android.synthetic.main.activity_add_reminder.*
import java.util.*

// Activity for adding or updating reminders
class AddReminderActivity : AppCompatActivity() {

    // Lateinit properties to be initialized later
    private lateinit var alarmManager: AlarmManager
    private lateinit var reminderPreferences: SharedPreferencesHandler // Handler for managing shared preferences
    private val myCalendar = Calendar.getInstance() // Calendar instance to manage date and time
    private var date: DatePickerDialog.OnDateSetListener? = null // Listener for the date picker
    private var hour: Int = 0 // Selected hour
    private var minute: Int = 0 // Selected minute
    private var reminderSaved = Reminder() // Object to store the reminder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        // Initialize SharedPreferencesHandler to handle reminders
        reminderPreferences = SharedPreferencesHandler(this)

        // Check if the intent contains an existing reminder
        if (intent.hasExtra("reminder")) {
            reminderSaved = intent.getSerializableExtra("reminder") as Reminder
        }

        // Set listener for the date picker dialog
        date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate() // Update the date display
        }

        // If a reminder is being edited (reminderSaved has an ID)
        if (reminderSaved.id != 0L) {
            titleET.setText(reminderSaved.title) // Set the title
            descriptionET.setText(reminderSaved.description) // Set the description
            dateTV.text = reminderSaved.date // Display the saved date
            timeTV.text = reminderSaved.time // Display the saved time

            // Split and parse the saved date and time to set the Calendar object
            val split = reminderSaved.date.split("/")
            val date = split[0]
            val month = split[1]
            val year = split[2]

            val split1 = reminderSaved.time.split(":")
            val hour = split1[0]
            val minute = split1[1]

            myCalendar.set(Calendar.YEAR, year.toInt())
            myCalendar.set(Calendar.MONTH, month.toInt())
            myCalendar.set(Calendar.DAY_OF_MONTH, date.toInt())

            myCalendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
            myCalendar.set(Calendar.MINUTE, minute.toInt())
            myCalendar.set(Calendar.SECOND, 0)

            // Change button text to "Update" when editing an existing reminder
            saveBtn.text = getString(R.string.update)
        } else {
            updateDate() // Update the date for a new reminder
            saveBtn.text = getString(R.string.save) // Set button text to "Save"
        }

        // Set up date selection
        selectDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = myCalendar.timeInMillis // Prevent past dates
            datePickerDialog.show() // Show the date picker dialog
        }

        // Set up time selection
        selectTimeButton.setOnClickListener {
            hour = myCalendar.get(Calendar.HOUR_OF_DAY) // Get current hour
            minute = myCalendar.get(Calendar.MINUTE) // Get current minute

            // Show time picker dialog
            val timePickerDialog =
                TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener(function = { _, hour, minute ->
                        myCalendar.set(Calendar.HOUR_OF_DAY, hour) // Set hour in Calendar
                        myCalendar.set(Calendar.MINUTE, minute) // Set minute in Calendar
                        myCalendar.set(Calendar.SECOND, 0)
                        updateTime(hour, minute) // Update the displayed time
                    }), hour, minute, true
                )

            timePickerDialog.show() // Show the time picker dialog
        }

        // Save button click handler
        saveBtn.setOnClickListener {
            // Validate the title and time inputs
            if (titleET.text?.isEmpty() == true) {
                Util.showToastMessage(this, "Please select title")
            } else if (timeTV.text == getString(R.string.time)) {
                Util.showToastMessage(this, "Please select time")
            } else {
                // Gather reminder details
                val title = titleET.text.toString()
                val description = descriptionET.text.toString()
                val time = timeTV.text.toString()
                val date = dateTV.text.toString()

                // Create a Reminder object
                val reminder = Reminder().apply {
                    this.title = title
                    this.description = description
                    this.time = time
                    this.date = date
                }

                val saveSuccess: Boolean
                // Update reminder if it's an edit, otherwise save new reminder
                if (reminderSaved.id != 0L) {
                    reminder.id = reminderSaved.id
                    saveSuccess = reminderPreferences.updateReminder(reminder)
                } else {
                    saveSuccess = reminderPreferences.saveReminder(reminder)
                }

                // Retrieve the saved reminder ID
                val reminderId = reminder.id

                // Set alarm if saving was successful
                if (saveSuccess && reminderId != 0L) {
                    Log.d("AlarmTime", "Hour: $hour")
                    Log.d("AlarmTime", "Min: $minute")
                    setRemainderAlarm(reminderId) // Schedule the reminder alarm
                } else {
                    Util.showToastMessage(this, "Failed to save reminder")
                }
            }
        }
    }

    // Update the displayed date
    private fun updateDate() {
        val formattedDate = Util.getFormattedDateInString(myCalendar.timeInMillis, "dd/MM/YYYY")
        dateTV.text = formattedDate
    }

    // Update the displayed time
    @SuppressLint("SetTextI18n")
    private fun updateTime(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
        timeTV.text = "$hour:$minute"
    }

    // Set an alarm for the saved reminder
    private fun setRemainderAlarm(savedReminderId: Long) {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderService = ReminderService()
        val reminderReceiverIntent = Intent(this, AlarmReceiver::class.java)

        // Pass reminder details to the receiver
        reminderReceiverIntent.putExtra("reminderId", savedReminderId)
        reminderReceiverIntent.putExtra("isServiceRunning", isServiceRunning(reminderService))
        val pendingIntent =
            PendingIntent.getBroadcast(this, savedReminderId.toInt(), reminderReceiverIntent, 0)
        val formattedDate = Util.getFormattedDateInString(myCalendar.timeInMillis, "dd/MM/YYYY HH:mm")
        Log.d("TimeSetInMillis:", formattedDate)

        // Set the alarm, handling different Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, myCalendar.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, myCalendar.timeInMillis, pendingIntent)
        }

        // Notify the user and finish the activity
        Util.showToastMessage(this, "Alarm is set at : $formattedDate")
        finish()
    }

    // Check if the ReminderService is already running
    @Suppress("DEPRECATION")
    private fun isServiceRunning(reminderService: ReminderService): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (reminderService.javaClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString())
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString())
        return false
    }

}
