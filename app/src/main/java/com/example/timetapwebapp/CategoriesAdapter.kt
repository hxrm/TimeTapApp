package com.example.timetapwebapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter : ListAdapter<CategoryTimesheet, CategoriesAdapter.TimesheetViewHolder>(TimesheetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.categories_items, parent, false)
        return TimesheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimesheetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TimesheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.lblCategoryName)
        private val timesheetHours: TextView = itemView.findViewById(R.id.lblTimeSheetHours)

        fun bind(timesheet: CategoryTimesheet) {
            categoryName.text = timesheet.categories
            timesheetHours.text = timesheet.endTime
        }
    }
}

class TimesheetDiffCallback : DiffUtil.ItemCallback<CategoryTimesheet>() {
    override fun areItemsTheSame(oldItem: CategoryTimesheet, newItem: CategoryTimesheet): Boolean {
        return oldItem.categories== newItem.categories
    }

    override fun areContentsTheSame(oldItem: CategoryTimesheet, newItem: CategoryTimesheet): Boolean {
        return oldItem == newItem
    }
}
