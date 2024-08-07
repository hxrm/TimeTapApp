package com.example.timetapwebapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var streakValueTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserProfile"

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String

    companion object {
        const val REQUEST_CODE_PROFILE_DETAILS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val plusButton = findViewById<ImageView>(R.id.plus)
        plusButton.setOnClickListener {
            val intent = Intent(this, NewProjectActivity::class.java)
            startActivity(intent)
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_drawer)
        navigationView.setNavigationItemSelectedListener(this)

        val toggleButton = findViewById<ImageView>(R.id.toggle)
        toggleButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Initialize streakValueTextView
        streakValueTextView = findViewById(R.id.streakValue)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch and display streak value
        fetchAndDisplayStreak()

        // Get current user ID
        userId = mAuth.currentUser?.uid ?: ""

        // Setup real-time listener for streak
        setupStreakListener(userId)
    }

    private fun fetchAndDisplayStreak() {
        // Load current streak from SharedPreferences as an Integer
        val currentStreak = sharedPreferences.getInt("current_streak", 0)
        streakValueTextView.text = currentStreak.toString()
    }

    private fun setupStreakListener(userId: String) {
        // Setup listener for current streak
        val currentStreakRef = database.getReference("users").child(userId).child("current_streak")
        currentStreakRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val streak = snapshot.getValue(Int::class.java)
                if (streak != null) {
                    streakValueTextView.text = streak.toString()

                    // Update SharedPreferences with new streak value as an Integer
                    sharedPreferences.edit().putInt("current_streak", streak).apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching current streak", error.toException())
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_myprofile -> {
                val intent = Intent(this, ActivityProfileDetails::class.java)
                startActivityForResult(intent, REQUEST_CODE_PROFILE_DETAILS)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PROFILE_DETAILS && resultCode == RESULT_OK) {
            data?.let {
                val streakValue = it.getStringExtra("STREAK_VALUE") ?: "0"
                streakValueTextView.text = streakValue
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
