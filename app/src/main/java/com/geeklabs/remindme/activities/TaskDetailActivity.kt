package com.geeklabs.remindme.activities

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geeklabs.remindme.R

class TaskDetailActivity : AppCompatActivity() {

    // UI elements for task details and stopwatch functionality
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var timerTextView: TextView

    // Variables for handling stopwatch state and time
    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private val handler = Handler()

    // Runnable to update the timer text every second
    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                timerTextView.text = formatTime(elapsedTime)
                handler.postDelayed(this, 1000) // Update every second
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        // Initialize UI elements
        titleTextView = findViewById(R.id.task_title)
        descriptionTextView = findViewById(R.id.task_description)
        startButton = findViewById(R.id.start_button)
        pauseButton = findViewById(R.id.pause_button)
        stopButton = findViewById(R.id.stop_button)
        timerTextView = findViewById(R.id.timer_text)

        // Get task details passed via Intent
        val title = intent.getStringExtra("TASK_TITLE")
        val description = intent.getStringExtra("TASK_DESCRIPTION")

        // Set task title and description in the UI
        titleTextView.text = title
        descriptionTextView.text = description

        // Set up listeners for stopwatch buttons
        startButton.setOnClickListener {
            startTimer()  // Start the timer
        }

        pauseButton.setOnClickListener {
            pauseTimer()  // Pause the timer
        }

        stopButton.setOnClickListener {
            stopTimer()  // Stop and reset the timer
        }

        // Initialize button states: pause and stop buttons are disabled initially
        pauseButton.isEnabled = false
        stopButton.isEnabled = false
    }

    // Start the stopwatch
    private fun startTimer() {
        isRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        stopButton.isEnabled = true
        startTime = System.currentTimeMillis() - elapsedTime  // Resume from where it was paused
        handler.post(runnable)  // Start updating the timer
    }

    // Pause the stopwatch
    private fun pauseTimer() {
        isRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
    }

    // Stop and reset the stopwatch
    private fun stopTimer() {
        isRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        stopButton.isEnabled = false
        elapsedTime = 0L  // Reset the elapsed time
        timerTextView.text = "00:00"  // Reset the displayed time
    }

    // Format the elapsed time into minutes and seconds (MM:SS)
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
