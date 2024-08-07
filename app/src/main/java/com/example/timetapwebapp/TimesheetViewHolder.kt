package com.example.timetapwebapp

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimesheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
    val tvTaskName: TextView = itemView.findViewById(R.id.TaskName)
    val tvCategories: TextView = itemView.findViewById(R.id.CategoriesName)
    val tvDescription: TextView = itemView.findViewById(R.id.Descriptions)
    val tvDate: TextView = itemView.findViewById(R.id.Date)
    val tvStartTime: TextView = itemView.findViewById(R.id.startTime)
    val tvEndTime: TextView = itemView.findViewById(R.id.EndTime)
    val tvTimeSpent: TextView = itemView.findViewById(R.id.TimeSpent)
    val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
}
