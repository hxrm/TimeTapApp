package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityStartTimer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var projectTitleTextView: TextView
    private lateinit var taskNameTextView: TextView
    private lateinit var startButton: Button
    private lateinit var pointsValue: TextView
    private lateinit var streakCounter: TextView

    private lateinit var profileLauncher: ActivityResultLauncher<Intent>
    private lateinit var categoryLauncher: ActivityResultLauncher<Intent>
    private lateinit var timesheetLauncher: ActivityResultLauncher<Intent>
    private lateinit var analyticsLauncher: ActivityResultLauncher<Intent>
    private lateinit var logoutLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_timer)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

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
        taskNameTextView = findViewById(R.id.taskName)

        val projectName = intent.getStringExtra("PROJECT_NAME").orEmpty()
        val taskName = intent.getStringExtra("TASK_NAME").orEmpty()

        projectTitleTextView.text = projectName
        taskNameTextView.text = taskName

        startButton = findViewById(R.id.startButton)

        pointsValue = findViewById(R.id.pointsValue)
        streakCounter = findViewById(R.id.streakValue)

        fetchAndDisplayPointsAndStreak()

        startButton.setOnClickListener {
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                val taskRef = database.child("unfinishedProjects").child(userUid)
                    .orderByChild("taskName").equalTo(taskName)
                taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var timeElapsedMillis = 0L
                            for (taskSnapshot in snapshot.children) {
                                timeElapsedMillis = taskSnapshot.child("timeElapsed").getValue(Long::class.java) ?: 0L
                                break
                            }
                            val intent = Intent(this@ActivityStartTimer, ActivityPauseFinish::class.java).apply {
                                putExtra("PROJECT_NAME", projectName)
                                putExtra("TASK_NAME", taskName)
                                putExtra("TIME_ELAPSED", timeElapsedMillis)
                            }
                            startActivity(intent)
                        } else {
                            // Start new task
                            val intent = Intent(this@ActivityStartTimer, ActivityPauseFinish::class.java).apply {
                                putExtra("PROJECT_NAME", projectName)
                                putExtra("TASK_NAME", taskName)
                                putExtra("TIME_ELAPSED", 0L)
                            }
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ActivityStartTimer", "Failed to retrieve time: ${error.message}")
                    }
                })
            } else {
                Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            }
        }

        val rightArrow2: ImageView = findViewById(R.id.rightArrow2)
        rightArrow2.setOnClickListener {
            val intent = Intent(this, ActiveProjectsActivity::class.java)
            startActivity(intent)
        }

        // Initialize the ActivityResultLauncher instances
        profileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle result if needed
        }
        categoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle result if needed
        }
        timesheetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle result if needed
        }
        analyticsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle result if needed
        }
        logoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle result if needed
            finish() // Finish the current activity on logout
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_myprofile -> {
                profileLauncher.launch(Intent(this, ActivityProfileDetails::class.java))
            }
            R.id.action_category -> {
                categoryLauncher.launch(Intent(this, CategoriesActivities::class.java))
            }
            R.id.action_timesheet_list -> {
                timesheetLauncher.launch(Intent(this, ActivityTimeList::class.java))
            }
            R.id.action_analytics -> {
                analyticsLauncher.launch(Intent(this, ActionAnalyticActivity::class.java))
            }
            R.id.action_logout -> {
                logoutLauncher.launch(Intent(this, SignInActivity::class.java))
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

    private fun fetchAndDisplayPointsAndStreak() {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            val userRef = database.child("users").child(userUid)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val points = snapshot.child("points").getValue(Int::class.java) ?: 0
                    val streak = snapshot.child("current_streak").getValue(Int::class.java) ?: 0

                    pointsValue.text = " $points"
                    streakCounter.text = "$streak"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ActivityStartTimer", "Failed to retrieve points and streak: ${error.message}")
                }
            })
        }
    }

    companion object {
        private const val REQUEST_CODE_PROFILE = 1
        private const val REQUEST_CODE_CATEGORY = 2
        private const val REQUEST_CODE_TIMESHEET = 3
        private const val REQUEST_CODE_ANALYTICS = 4
        private const val REQUEST_CODE_LOGOUT = 5
    }
}
