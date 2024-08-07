package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProjectDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        val projectTitle = intent.getStringExtra("PROJECT_TITLE")

        // Display the project title in a TextView
        val projectTitleTextView: TextView = findViewById(R.id.projectTitleTextView)
        projectTitleTextView.text = projectTitle

        // Handle the back button click
        val backButton: ImageButton = findViewById(R.id.imageBack)
        backButton.setOnClickListener {
            val intent = Intent(this, ActiveProjectsActivity::class.java)
            startActivity(intent)
        }
    }
}
