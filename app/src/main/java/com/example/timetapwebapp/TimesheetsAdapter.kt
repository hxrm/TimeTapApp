package com.example.timetapwebapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class TimesheetsAdapter(private val timesheetList: List<Timesheets>) : RecyclerView.Adapter<TimesheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_expanded_list, parent, false)
        return TimesheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimesheetViewHolder, position: Int) {
        val timesheet = timesheetList[position]
        holder.tvProjectName.text = timesheet.projectName
        holder.tvTaskName.text = timesheet.taskName
        holder.tvCategories.text = timesheet.categories
        holder.tvDescription.text = timesheet.description
        holder.tvDate.text = timesheet.startDate
        holder.tvStartTime.text = timesheet.startTime
        holder.tvEndTime.text = timesheet.endTime
        holder.tvTimeSpent.text = timesheet.timeSpent

        if (timesheet.imageUrl.isNotEmpty()) {
            Picasso.get().load(timesheet.imageUrl).into(holder.ivImage)
        } else {
            holder.ivImage.setImageDrawable(null) // Ensures no image is displayed when URL is empty
        }
    }

    override fun getItemCount(): Int {
        return timesheetList.size
    }
}
