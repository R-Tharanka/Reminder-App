package com.geeklabs.remindme.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request to hide the status bar (no title)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Set the window to full screen mode
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Set the content view to the splash screen layout
        setContentView(com.geeklabs.remindme.R.layout.activity_splash)

        // Delay for 2 seconds (2000 milliseconds) before transitioning to the MainActivity
        Handler().postDelayed({
            // Create an intent to navigate to MainActivity
            val splashIntent = Intent(this, MainActivity::class.java)
            startActivity(splashIntent) // Start MainActivity
            finish() // Close SplashActivity so the user can't return to it
        }, 2000) // 2-second delay before switching to the main screen
    }
}
