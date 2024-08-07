package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ActivityStartTimer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var database: DatabaseReference
    private lateinit var projectTitleTextView: TextView
    private lateinit var taskTitleTextView: TextView
    private lateinit var startButton: Button
    private lateinit var txtTimeDifference: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_timer)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Set up the drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_drawer)
        navigationView.setNavigationItemSelectedListener(this)

        // Set up the toggle button to open and close the drawer
        val toggleButton: ImageView = findViewById(R.id.toggle)
        toggleButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Initialize TextViews and Button
        projectTitleTextView = findViewById(R.id.projectTitle)
        taskTitleTextView = findViewById(R.id.taskTitle)
        startButton = findViewById(R.id.startButton)
        txtTimeDifference = findViewById(R.id.txtTimeDifference)

        // Retrieve project name and task name from intent
        val projectName = intent.getStringExtra("PROJECT_NAME").orEmpty()
        val taskName = intent.getStringExtra("TASK_NAME").orEmpty()

        // Set project name and task name to TextViews
        projectTitleTextView.text = projectName
        taskTitleTextView.text = taskName

        // Set button click listener
        startButton.setOnClickListener {
            fetchTaskDataFromDatabase(projectName, taskName)
        }
    }

    private fun fetchTaskDataFromDatabase(projectName: String, taskName: String) {
        val query: Query = database.child("tasks")
            .orderByChild("projectName")
            .equalTo(projectName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task?.taskName == taskName) {
                        val startTime = task.startTime
                        val endTime = task.endTime

                        // Calculate time difference
                        val timeDifference = calculateTimeDifference(startTime, endTime)
                        txtTimeDifference.text = timeDifference

                        // Save time difference in Firebase
                        taskSnapshot.ref.child("timeDifference").setValue(timeDifference)
                            .addOnSuccessListener {
                                Log.d("ActivityStartTimer", "Time difference saved successfully")
                            }
                            .addOnFailureListener { error ->
                                Log.e("ActivityStartTimer", "Failed to save time difference", error)
                            }

                        // Pass data to ActivityPauseFinish
                        val intent = Intent(this@ActivityStartTimer, ActivityPauseFinish::class.java).apply {
                            putExtra("PROJECT_NAME", projectName)
                            putExtra("TASK_NAME", taskName)
                            putExtra("TIME_DIFFERENCE", timeDifference)
                        }
                        startActivity(intent)
                        finish()
                        return
                    }
                }
                Log.d("ActivityStartTimer", "Task not found for projectName: $projectName and taskName: $taskName")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivityStartTimer", "Failed to read data from database", error.toException())
            }
        })
    }

    private fun calculateTimeDifference(startTime: String?, endTime: String?): String {
        return try {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = dateFormat.parse(startTime)
            val endDate = dateFormat.parse(endTime)

            if (startDate != null && endDate != null) {
                val differenceInMillis = endDate.time - startDate.time
                val hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis) % 60
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                "Invalid time"
            }
        } catch (e: Exception) {
            "Invalid time"
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_myprofile -> startActivity(Intent(this, ProjectDetailsActivity::class.java))
            R.id.action_category -> startActivity(Intent(this, CategoriesActivities::class.java))
            R.id.action_timesheet_list -> startActivity(Intent(this, ActivityTimeList::class.java))
            R.id.action_analytics -> startActivity(Intent(this, ActionAnalyticActivity::class.java))
            R.id.action_logout -> {
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
