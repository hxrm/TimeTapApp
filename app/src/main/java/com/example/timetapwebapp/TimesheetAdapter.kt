package com.example.timetapwebapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimesheetAdapter(
    private var timesheetList: List<TimesheetItem>,
    private val onItemClick: (TimesheetItem) -> Unit
) : RecyclerView.Adapter<TimesheetAdapter.TimesheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_items, parent, false)
        return TimesheetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimesheetViewHolder, position: Int) {
        val currentItem = timesheetList[position]
        holder.projectName.text = currentItem.projectName
        holder.timeElapsed.text = currentItem.formattedTime

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = timesheetList.size

    fun updateData(newList: List<TimesheetItem>) {
        timesheetList = newList
        notifyDataSetChanged()
    }

    class TimesheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.projectName)
        val timeElapsed: TextView = itemView.findViewById(R.id.timeElapsed)
    }
}
