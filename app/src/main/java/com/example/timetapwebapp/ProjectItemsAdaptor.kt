package com.example.timetapwebapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProjectItemsAdapter(private val projectList: List<ProjecsItems>) :
    RecyclerView.Adapter<ProjectItemsAdapter.ProjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timesheet_expanded_list, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val projectItem = projectList[position]
        holder.projectName.text = projectItem.projectName
        holder.description.text = projectItem.description
        holder.startDate.text = projectItem.startTime
        //holder.endDate.text = projectItem.endTime
        holder.category.text = projectItem.category
        holder.date.text = projectItem.date
        holder.timeSpent.text = projectItem.timeSpent
        holder.taskName.text = projectItem.taskName


    }

    override fun getItemCount() = projectList.size

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.projectName)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val startDate: TextView = view.findViewById(R.id.txtStartDate)

        // val endDate: TextView = view.findViewById(R.id.txtEndDate)
        val category: TextView = view.findViewById(R.id.tvCategories)
        val date: TextView = view.findViewById(R.id.tvDate)
        val timeSpent: TextView = view.findViewById(R.id.tvTimeSpent)
        val taskName: TextView = view.findViewById(R.id.taskName)
    }
}
