package com.geeklabs.remindme.models

import java.io.Serializable

// Data class representing a Reminder entity
data class Reminder(
    var id: Long = 0,                  // Unique ID for each reminder
    var title: String = "",            // Title of the reminder
    var description: String = "",      // Detailed description of the reminder
    var time: String = "",             // Time of the reminder (HH:mm format, for example)
    var date: String = "",             // Date of the reminder (yyyy-MM-dd format, for example)
    var createdTime: Long = System.currentTimeMillis(),  // Timestamp when the reminder is created
    var modifiedTime: Long = System.currentTimeMillis()  // Timestamp when the reminder is last modified
) : Serializable  // Serializable interface allows this object to be passed between activities or saved
