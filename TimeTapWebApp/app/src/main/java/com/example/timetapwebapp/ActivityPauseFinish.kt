package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class ActivityPauseFinish : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var database: DatabaseReference
    private lateinit var projectTitleTextView: TextView
    private lateinit var taskStatusTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var continueLaterButton: Button
    private lateinit var finishTaskButton: Button
    private lateinit var pauseWorkButton: Button
    private lateinit var startButton: Button
    private lateinit var restartButton: Button
    private var timerRunning = false
    private var timeElapsed: Long = 0L
    private lateinit var timerHandler: Handler
    private lateinit var updateTimerRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pause_finish)

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

        // Initialize TextViews and Buttons
        projectTitleTextView = findViewById(R.id.projectTitle)
        taskStatusTextView = findViewById(R.id.taskStatus)
        timerTextView = findViewById(R.id.timerTextView)
        continueLaterButton = findViewById(R.id.continueLaterButton)
        finishTaskButton = findViewById(R.id.finishTaskButton)
        pauseWorkButton = findViewById(R.id.pauseWorkButton)
        startButton = findViewById(R.id.startButton)
        restartButton = findViewById(R.id.restartButton)

        // Get data from intent
        val projectName = intent.getStringExtra("PROJECT_NAME").orEmpty()
        val taskName = intent.getStringExtra("TASK_NAME").orEmpty()
        val timeDifference = intent.getStringExtra("TIME_DIFFERENCE").orEmpty()

        // Set data to TextViews
        projectTitleTextView.text = projectName
        taskStatusTextView.text = taskName
        timeElapsed = parseTimeDifference(timeDifference)
        updateTimerText()

        // Timer handler and runnable
        timerHandler = Handler(Looper.getMainLooper())
        updateTimerRunnable = object : Runnable {
            override fun run() {
                if (timerRunning) {
                    timeElapsed -= 1000
                    if (timeElapsed <= 0) {
                        timeElapsed = 0
                        pauseTimer()
                        // Handle timer finish logic here, e.g., notify user
                    }
                    updateTimerText()
                    timerHandler.postDelayed(this, 1000)
                }
            }
        }

        // Set up button click listeners
        startButton.setOnClickListener {
            startTimer()
        }

        pauseWorkButton.setOnClickListener {
            pauseTimer()
        }

        finishTaskButton.setOnClickListener {
            finishTask()
        }

        continueLaterButton.setOnClickListener {
            continueLater(projectName, taskName)
        }

        restartButton.setOnClickListener {
            restartTimer()
        }
    }

    private fun startTimer() {
        timerRunning = true
        timerHandler.post(updateTimerRunnable)
    }

    private fun pauseTimer() {
        timerRunning = false
        timerHandler.removeCallbacks(updateTimerRunnable)
    }

    private fun finishTask() {
        pauseTimer()
        // Handle finishing task logic here
        // e.g., show a congratulations message, update task status, etc.
        val congratsTextView: TextView = findViewById(R.id.congratsTextView)
        congratsTextView.text = "Congratulations! Task Completed."
        congratsTextView.visibility = TextView.VISIBLE

        // Update task status in Firebase
        val taskRef = database.child("tasks").orderByChild("projectName").equalTo(projectTitleTextView.text.toString())
        taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task?.taskName == taskStatusTextView.text.toString()) {
                        taskSnapshot.ref.child("status").setValue("Completed")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivityPauseFinish", "Failed to update task status", error.toException())
            }
        })
    }

    private fun continueLater(projectName: String, taskName: String) {
        pauseTimer()
        val intent = Intent(this, ActivityStartTimer::class.java).apply {
            putExtra("PROJECT_NAME", projectName)
            putExtra("TASK_NAME", taskName)
            putExtra("TIME_DIFFERENCE", formatTimeDifference(timeElapsed))
        }
        startActivity(intent)
        finish()
    }

    private fun restartTimer() {
        timeElapsed = 0L
        updateTimerText()
        startTimer()
    }

    private fun updateTimerText() {
        val hours = TimeUnit.MILLISECONDS.toHours(timeElapsed)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60
        timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun parseTimeDifference(timeDifference: String): Long {
        val parts = timeDifference.split(":")
        return if (parts.size == 3) {
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            val seconds = parts[2].toLong()
            TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds)
        } else {
            0L
        }
    }

    private fun formatTimeDifference(timeDifference: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
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
