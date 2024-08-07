package com.example.timetapwebapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityStartTask : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var pointsCounter: TextView
    private lateinit var pointsValue: TextView
    private lateinit var streakCounter: TextView
    private lateinit var streakValue: TextView
    private lateinit var projectTitle: TextView
    private lateinit var taskTitleTextView: TextView
    private lateinit var rightArrow: ImageView
    private lateinit var customToggle: ImageView
    private lateinit var plusButton: ImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_task)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_drawer)
        navigationView.setNavigationItemSelectedListener(this)

        pointsCounter = findViewById(R.id.pointsCounter)
        pointsValue = findViewById(R.id.pointsValue)
        streakCounter = findViewById(R.id.streakCounter)
        streakValue = findViewById(R.id.streakValue)
        projectTitle = findViewById(R.id.projectTitle)
        taskTitleTextView = findViewById(R.id.taskTitle)
        rightArrow = findViewById(R.id.rightArrow)
        customToggle = findViewById(R.id.toggle)
        plusButton = findViewById(R.id.plus)

        pointsCounter.text = "Points"
        pointsValue.text = "0"
        streakCounter.text = "Streak"
        streakValue.text = "0"

        val sharedPreferences =
            getSharedPreferences("com.example.timetapwebapp.PREFERENCES", Context.MODE_PRIVATE)
        val projectName = sharedPreferences.getString("PROJECT_TITLE", null)
        projectTitle.text = projectName ?: "No project"

        val taskName = intent.getStringExtra("TASK_NAME")
        Log.d("ActivityStartTask", "Received taskName: $taskName")
        taskTitleTextView.text = taskName ?: "No task"

        val startNewTimesheetButton: Button = findViewById(R.id.startTimesheet)
        startNewTimesheetButton.setOnClickListener {
            val intent = Intent(this, AddingImagesConfirmActivity::class.java)
            intent.putExtra("PROJECT_TITLE", projectName)
            startActivity(intent)
        }

        rightArrow.setOnClickListener {
            val intent = Intent(this, ActiveProjectsActivity::class.java)
            startActivity(intent)
        }

        customToggle.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        plusButton.setOnClickListener {
            val intent = Intent(this, NewProjectActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch and display streak and points values
        fetchAndDisplayStreakAndPoints()

        // Get current user ID
        userId = mAuth.currentUser?.uid ?: ""

        // Setup real-time listeners for streak and points
        setupStreakAndPointsListeners(userId)
    }

    private fun fetchAndDisplayStreakAndPoints() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val currentStreak =
                            snapshot.child("current_streak").getValue(Int::class.java) ?: 0
                        val points = snapshot.child("points").getValue(Int::class.java) ?: 0

                        streakValue.text = currentStreak.toString()
                        pointsValue.text = points.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching streak and points", error.toException())
                }
            })
        }
    }

    private fun setupStreakAndPointsListeners(userId: String) {
        // Setup listener for current streak
        val currentStreakRef = database.getReference("users").child(userId).child("current_streak")
        currentStreakRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val streak = snapshot.getValue(Int::class.java)
                if (streak != null) {
                    streakValue.text = streak.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching current streak", error.toException())
            }
        })

        // Setup listener for points
        val pointsRef = database.getReference("users").child(userId).child("points")
        pointsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userPoints = snapshot.getValue(Int::class.java)
                if (userPoints != null) {
                    pointsValue.text = userPoints.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching points", error.toException())
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_myprofile -> {
                startActivityForResult(
                    Intent(this, ActivityProfileDetails::class.java), 1001
                )
            }

            R.id.action_category -> {
                startActivityForResult(
                    Intent(this, CategoriesActivities::class.java), 1002
                )
            }

            R.id.action_timesheet_list -> {
                startActivityForResult(
                    Intent(this, ActivityTimesheetView::class.java), 1003
                )
            }

            R.id.action_analytics -> {
                startActivityForResult(
                    Intent(this, ActionAnalyticActivity::class.java), 1004
                )
            }

            R.id.action_logout -> {
                startActivityForResult(
                    Intent(this, SignInActivity::class.java), 1005
                )
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
