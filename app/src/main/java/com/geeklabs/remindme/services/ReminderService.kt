package com.geeklabs.remindme.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.geeklabs.remindme.R
import com.geeklabs.remindme.activities.MainActivity
import com.geeklabs.remindme.database.SharedPreferencesHandler
import com.geeklabs.remindme.models.Reminder
import java.util.*

class ReminderService : Service() {

    private var mediaPlayer: MediaPlayer? = null // Optional media player for custom alarm sounds
    private var tts: TextToSpeech? = null // Text-to-Speech engine

    // Called when the service is created
    override fun onCreate() {
        super.onCreate()
        Log.d("ReminderService", "onCreate called")

        // Create and start the MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_ringtone)
        mediaPlayer?.start()

        // Stop the MediaPlayer after 4 seconds
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }, 4000) // 4000 milliseconds = 4 seconds
    }


    // This service doesn't allow binding, so return null
    override fun onBind(intent: Intent?): IBinder? {
        Log.d("ReminderService", "onBind called")
        return null
    }

    // Called when the service is started (triggered by the alarm)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ReminderService", "onStartCommand called")
        val reminderId = intent?.getLongExtra("reminderId", 0)
        val reminderPreferences = SharedPreferencesHandler(this)
        val reminder = reminderPreferences.getReminderById(reminderId ?: 0)

        if (reminder != null) {
            showAlarmNotification(reminder) // Show a notification for the reminder

            // Speak the reminder details using Text-to-Speech
            val speakText = reminder.title + " " + reminder.description
            tts = TextToSpeech(this,
                TextToSpeech.OnInitListener {
                    if (it != TextToSpeech.ERROR) {
                        tts?.language = Locale.US
                        tts?.speak(speakText, TextToSpeech.QUEUE_ADD, null, null)
                    } else {
                        Log.d("ReminderService", "Error: $it")
                    }
                })
            Log.d("ReminderService", speakText)
        } else {
            Log.d("ReminderService", "Reminder not found")
        }
        return START_STICKY // Restart service if it gets terminated by the system
    }

    // Show a notification with reminder details
    private fun showAlarmNotification(reminder: Reminder) {
        Log.d("ReminderService", "showAlarmNotification called")

        createNotificationChannel(reminder.id.toInt()) // Create a notification channel for Android O+

        // Build the notification
        val builder = NotificationCompat.Builder(this, reminder.id.toString())
            .setSmallIcon(R.drawable.app_logo) // Notification icon
            .setContentTitle(reminder.title) // Notification title
            .setContentText(reminder.description) // Notification message
            .setAutoCancel(true) // Auto-dismiss notification on click
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the notification priority

        // Set up the notification's click behavior to open the MainActivity
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.putExtra("reminderId", reminder.id)
        notificationIntent.putExtra("from", "Notification")

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)
        val notification = builder.build()

        // Show the notification
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminder.id.toInt(), notification)
    }

    // Create a notification channel for Android versions O and above
    private fun createNotificationChannel(id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                id.toString(),
                "Reminder Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    // Called when the service is destroyed
    override fun onDestroy() {
        Log.d("ReminderService", "onDestroy called")
        super.onDestroy()

        // Stop and release media player resources if playing
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        // Stop and release Text-to-Speech engine
        tts?.stop()
        tts?.shutdown()
    }
}
