package com.geeklabs.remindme.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

object Util {

    // Show a short Toast message
    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Convert a timestamp (in milliseconds) into a formatted date string
    fun getFormattedDateInString(timeInMillis: Long, format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(timeInMillis)
    }

    // Convert a date string into a Date object
    fun getFormattedDate(timeInString: String, format: String): Date {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.parse(timeInString)
    }
}
