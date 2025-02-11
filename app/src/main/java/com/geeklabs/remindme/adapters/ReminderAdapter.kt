package com.geeklabs.remindme.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geeklabs.remindme.R
import com.geeklabs.remindme.models.Reminder
import com.geeklabs.remindme.utils.Util
import com.geeklabs.remindme.activities.TaskDetailActivity // Import statement
import kotlinx.android.synthetic.main.item_reminder.view.*

class ReminderAdapter constructor(private val itemClick: OnItemClickListener) :
    RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    // Holds the list of reminders to be displayed
    var reminderList = mutableListOf<Reminder>()

    // Called when a new ViewHolder is created (i.e., an item view is inflated)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for each reminder item in the RecyclerView
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(inflate)
    }

    // Returns the number of reminders in the list (RecyclerView size)
    override fun getItemCount(): Int {
        return reminderList.size
    }

    // Called to bind data to the ViewHolder for each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind the reminder data to the item view based on its position in the list
        holder.bindItems(reminderList[position], position)
    }

    // ViewHolder class represents the individual list item views
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Binds the data of a specific reminder to the views in the item layout
        @SuppressLint("SetTextI18n") // Suppresses the warning about setting text manually
        fun bindItems(reminder: Reminder, position: Int) {
            // Set the serial number (position + 1) in the list
            itemView.serialTV.text = "${position + 1}."
            // Set the description of the reminder
            itemView.descriptionTV.text = reminder.description

            // Format the reminder date and check if it is in the past
            val reminderDate =
                Util.getFormattedDate(reminder.date + " " + reminder.time, "dd/MM/YYYY HH:mm")

            // Check if the reminder date has passed
            if (reminderDate.time < System.currentTimeMillis()) {
                // If the date is in the past, show normal text without strike-through
                itemView.reminderTV.text = reminder.title
            } else {
                // If the reminder date is in the future, apply strike-through text to all fields
                itemView.reminderTV.text = reminder.title
                itemView.serialTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.reminderTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.descriptionTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.timeTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.dateTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }

            // Set the time and date fields for the reminder
            itemView.timeTV.text = reminder.time
            itemView.dateTV.text = reminder.date

            // Set a click listener for the entire item view
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, TaskDetailActivity::class.java).apply {
                    putExtra("TASK_TITLE", reminder.title)
                    putExtra("TASK_DESCRIPTION", reminder.description)
                }
                context.startActivity(intent)
            }

            // Set a click listener for the "more" button (options menu)
            itemView.more.setOnClickListener {
                // Calls the onItemClick function of the interface with the clicked reminder
                itemClick.onItemClick(reminder, itemView.more, adapterPosition)
            }
        }
    }

    // Interface to handle item clicks (used for more options like update/delete)
    interface OnItemClickListener {
        fun onItemClick(
            reminder: Reminder, // The reminder that was clicked
            view: View,         // The view that was clicked (the "more" button)
            position: Int       // The position of the clicked item in the list
        )
    }
}
