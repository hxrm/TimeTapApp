package com.example.timetapwebapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ProjectAdapter(
    private val context: Context,
    private val projects: List<ProjectData>
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    private var filteredProjects: List<ProjectData> = projects

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.projectName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = filteredProjects[position]
        holder.projectName.text = project.projectName

        // Set an OnClickListener on the itemView to handle item clicks
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProjectDetailsActivity::class.java).apply {
                putExtra("PROJECT_TITLE", project.projectName)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = filteredProjects.size

    // Function to filter the list based on search query
    fun filter(text: String) {
        filteredProjects = if (text.isEmpty()) {
            projects
        } else {
            val filterPattern = text.toLowerCase(Locale.getDefault()).trim()
            projects.filter { it.projectName.toLowerCase(Locale.getDefault()).contains(filterPattern) }
        }
        notifyDataSetChanged()
    }
}
