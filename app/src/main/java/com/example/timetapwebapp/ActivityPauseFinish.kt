package com.example.timetapwebapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ActivityPauseFinish : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
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
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var clickSoundPlayer: MediaPlayer
    private lateinit var pointsValue: TextView
    private lateinit var streakCounter: TextView

    private lateinit var updateTimeHandler: Handler
    private lateinit var updateTimeRunnable: Runnable
    private val UPDATE_INTERVAL = 60000L // Update every 60 seconds (1 minute)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pause_finish)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_drawer)
        navigationView.setNavigationItemSelectedListener(this)

        val toggleButton: ImageView = findViewById(R.id.toggle)
        toggleButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        projectTitleTextView = findViewById(R.id.projectTitle)
        taskStatusTextView = findViewById(R.id.taskName)
        timerTextView = findViewById(R.id.timerTextView)
        continueLaterButton = findViewById(R.id.continueLaterButton)
        finishTaskButton = findViewById(R.id.finishTaskButton)
        pauseWorkButton = findViewById(R.id.pauseWorkButton)
        startButton = findViewById(R.id.startButton)
        restartButton = findViewById(R.id.restartButton)
        pointsValue = findViewById(R.id.pointsValue)
        streakCounter = findViewById(R.id.streakValue)

        val projectName = intent.getStringExtra("PROJECT_NAME").orEmpty()
        val taskName = intent.getStringExtra("TASK_NAME").orEmpty()
        timeElapsed = intent.getLongExtra("TIME_ELAPSED", 0L)

        Log.d("ActivityPauseFinish", "Received projectName: $projectName, taskName: $taskName")

        projectTitleTextView.text = projectName
        taskStatusTextView.text = taskName
        updateTimerText()

        timerHandler = Handler(Looper.getMainLooper())
        updateTimerRunnable = object : Runnable {
            override fun run() {
                if (timerRunning) {
                    timeElapsed += 1000
                    updateTimerText()
                    playClickSound()
                    timerHandler.postDelayed(this, 1000)
                }
            }
        }

        updateTimeHandler = Handler(Looper.getMainLooper())
        updateTimeRunnable = object : Runnable {
            override fun run() {
                if (timerRunning) {
                    updateTaskTimeInDatabase(projectName, taskName, timeElapsed)
                    updateTimeHandler.postDelayed(this, UPDATE_INTERVAL)
                }
            }
        }

        startButton.setOnClickListener {
            startTimer()
        }

        pauseWorkButton.setOnClickListener {
            pauseTimer()
        }

        finishTaskButton.setOnClickListener {
            finishTask(projectName, taskName, timeElapsed)
        }

        continueLaterButton.setOnClickListener {
            continueLater(projectName, taskName, timeElapsed)
        }

        restartButton.setOnClickListener {
            restartTimer()
        }

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Initialize click sound player
        clickSoundPlayer = MediaPlayer.create(this, R.raw.timer_sound)

        // Fetch and display points and streaks
        fetchAndDisplayPointsAndStreak()
    }

    private fun startTimer() {
        timerRunning = true
        timerHandler.post(updateTimerRunnable)
        updateTimeHandler.post(updateTimeRunnable) // Start periodic updates
    }

    private fun pauseTimer() {
        timerRunning = false
        timerHandler.removeCallbacks(updateTimerRunnable)
        updateTimeHandler.removeCallbacks(updateTimeRunnable) // Stop periodic updates
    }

    private fun finishTask(projectName: String, taskName: String, timeElapsed: Long) {
        pauseTimer()

        // Get the current date in the desired format
        val currentDate = getCurrentDate()

        // Update Firebase database with finished time
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            val finishedTimesRef = database.child("finishedTimes").child(userUid).push()
            val finishTime = System.currentTimeMillis()
            finishedTimesRef.setValue(
                TimerStats(
                    projectName,
                    taskName,
                    timeElapsed.toString(), // Store timeElapsed as milliseconds
                    finishTime,
                    currentDate
                )
            )
                .addOnSuccessListener {
                    Log.d("ActivityPauseFinish", "Time saved successfully.")
                    Toast.makeText(this, "Time saved successfully.", Toast.LENGTH_SHORT).show()
                    // Navigate to ActivityStartTask after finishing task
                    navigateToStartTask(projectName, taskName)
                }
                .addOnFailureListener {
                    Log.e("ActivityPauseFinish", "Failed to save time: ${it.message}")
                    Toast.makeText(this, "Failed to save time.", Toast.LENGTH_SHORT).show()
                }
        }
        findViewById<TextView>(R.id.congratsTextView).apply {
            text = "Congratulations! Task Completed."
            visibility = TextView.VISIBLE
        }
    }

    private fun navigateToStartTask(projectName: String, taskName: String) {
        val intent = Intent(this, ActivityStartTask::class.java).apply {
            putExtra("PROJECT_NAME", projectName)
            putExtra("TASK_NAME", taskName)
            // Optional: pass the time elapsed if needed
            // putExtra("TIME_ELAPSED", timeElapsed)
        }
        startActivity(intent)
        finish()
    }

    private fun continueLater(projectName: String, taskName: String, timeElapsed: Long) {
        pauseTimer()
        updateTaskTimeInDatabase(projectName, taskName, timeElapsed)

        val intent = Intent(this, ActivityStartTimer::class.java).apply {
            putExtra("PROJECT_NAME", projectName)
            putExtra("TASK_NAME", taskName)
            putExtra("TIME_ELAPSED", timeElapsed) // Pass timeElapsed in milliseconds
        }
        startActivity(intent)
        finish()
    }

    private fun updateTaskTimeInDatabase(projectName: String, taskName: String, timeElapsed: Long) {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            val taskRef = database.child("unfinishedProjects").child(userUid)
                .child("$projectName-$taskName")
            val taskData = mapOf(
                "projectName" to projectName,
                "taskName" to taskName,
                "timeElapsed" to timeElapsed
            )
            taskRef.setValue(taskData).addOnSuccessListener {
                Log.d("ActivityPauseFinish", "Task time updated.")
            }.addOnFailureListener {
                Log.e("ActivityPauseFinish", "Failed to update task time: ${it.message}")
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun formatTime(timeInMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun restartTimer() {
        pauseTimer()
        timeElapsed = 0L
        updateTimerText()
        startTimer()
    }

    private fun updateTimerText() {
        val formattedTime = formatTime(timeElapsed)
        timerTextView.text = formattedTime
    }

    private fun playClickSound() {
        if (clickSoundPlayer.isPlaying) {
            clickSoundPlayer.seekTo(0)
        } else {
            clickSoundPlayer.start()
        }
    }

    private fun fetchAndDisplayPointsAndStreak() {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            val userRef = database.child("users").child(userUid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val points = snapshot.child("points").getValue(Int::class.java) ?: 0
                    val streak = snapshot.child("current_streak").getValue(Int::class.java) ?: 0

                    pointsValue.text = points.toString()
                    streakCounter.text = streak.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ActivityPauseFinish", "Failed to fetch points and streak: ${error.message}")
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (clickSoundPlayer.isPlaying) {
            clickSoundPlayer.stop()
        }
        clickSoundPlayer.release()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_myprofile -> startActivity(Intent(this, ActivityProfileDetails::class.java))
            R.id.action_category -> startActivity(Intent(this, CategoriesActivities::class.java))
            R.id.action_timesheet_list -> startActivity(
                Intent(
                    this,
                    ActivityTimesheetView::class.java
                )
            )

            R.id.action_analytics -> startActivity(Intent(this, ActionAnalyticActivity::class.java))
            R.id.action_logout -> {
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
