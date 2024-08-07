package com.example.timetapwebapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityProfileDetails : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserProfile"

    private lateinit var lblName: TextView
    private lateinit var lblEmail: TextView
    private lateinit var lblUsername: TextView
    private lateinit var lblTotalProjects: TextView
    private lateinit var lblCurrentStreak: TextView
    private lateinit var lblAvgWeeklyHours: TextView
    private lateinit var lblPoints: TextView
    private lateinit var lblTotalHours: TextView
    private lateinit var lblMinHoursGoal: TextView
    private lateinit var lblMaxHoursGoal: TextView
    private lateinit var buttonEditProfile: Button
    private lateinit var backImageView: ImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Keys for SharedPreferences
    private val KEY_NAME = "name"
    private val KEY_EMAIL = "email"
    private val KEY_USERNAME = "username"
    private val KEY_TOTAL_PROJECTS = "total_projects"
    private val KEY_CURRENT_STREAK = "current_streak"
    private val KEY_AVG_WEEKLY_HOURS = "avg_weekly_hours"
    private val KEY_POINTS = "points"
    private val KEY_TOTAL_HOURS = "total_hours"
    private val KEY_MIN_HOURS_GOAL = "min_hours_goal"
    private val KEY_MAX_HOURS_GOAL = "max_hours_goal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_details)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize UI elements
        lblName = findViewById(R.id.lblname)
        lblEmail = findViewById(R.id.lblEmail)
        lblUsername = findViewById(R.id.lblUsername)
        lblTotalProjects = findViewById(R.id.lblTotalProjects)
        lblCurrentStreak = findViewById(R.id.lblCurrentStreak)
        lblAvgWeeklyHours = findViewById(R.id.lblAvgWeeklyHours)
        lblPoints = findViewById(R.id.lblPoints)
        lblTotalHours = findViewById(R.id.lblTotalHours)
        lblMinHoursGoal = findViewById(R.id.lblMinHoursGoal)
        lblMaxHoursGoal = findViewById(R.id.lblMaxHoursGoal)
        buttonEditProfile = findViewById(R.id.buttonEditProfile)
        backImageView = findViewById(R.id.imageBackProfile)

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listener for the Edit Profile button
        buttonEditProfile.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)
        }

        // Set click listener for the back button
        backImageView.setOnClickListener {
            finish() // This will close the current activity and return to the previous one
        }

        // Load data from SharedPreferences
        loadData()

        // Check if the user is signed in
        if (mAuth.currentUser != null) {
            // Fetch user details from Firebase
            fetchUserDetails()
        }
    }

    private fun loadData() {
        lblName.text = sharedPreferences.getString(KEY_NAME, "Joe Black")
        lblEmail.text = sharedPreferences.getString(KEY_EMAIL, "joeblack@email.com")
        lblUsername.text = sharedPreferences.getString(KEY_USERNAME, "Pablo")
        lblTotalProjects.text = sharedPreferences.getString(KEY_TOTAL_PROJECTS, "7")
        lblCurrentStreak.text = sharedPreferences.getString(KEY_CURRENT_STREAK, "8")
        lblAvgWeeklyHours.text = sharedPreferences.getString(KEY_AVG_WEEKLY_HOURS, "12.5 Hrs")
        lblPoints.text = sharedPreferences.getString(KEY_POINTS, "1087")
        lblTotalHours.text = sharedPreferences.getString(KEY_TOTAL_HOURS, "400 Hrs")
        lblMinHoursGoal.text = sharedPreferences.getString(KEY_MIN_HOURS_GOAL, "2 Hrs")
        lblMaxHoursGoal.text = sharedPreferences.getString(KEY_MAX_HOURS_GOAL, "6 Hrs")
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, lblName.text.toString())
        editor.putString(KEY_EMAIL, lblEmail.text.toString())
        editor.putString(KEY_USERNAME, lblUsername.text.toString())
        editor.putString(KEY_TOTAL_PROJECTS, lblTotalProjects.text.toString())
        editor.putString(KEY_CURRENT_STREAK, lblCurrentStreak.text.toString())
        editor.putString(KEY_AVG_WEEKLY_HOURS, lblAvgWeeklyHours.text.toString())
        editor.putString(KEY_POINTS, lblPoints.text.toString())
        editor.putString(KEY_TOTAL_HOURS, lblTotalHours.text.toString())
        editor.putString(KEY_MIN_HOURS_GOAL, lblMinHoursGoal.text.toString())
        editor.putString(KEY_MAX_HOURS_GOAL, lblMaxHoursGoal.text.toString())
        editor.apply()
    }

    private fun fetchUserDetails() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        val email = document.getString("email")
                        val username = document.getString("username")
                        val totalProjects = document.getString("total_projects")
                        val currentStreak = document.getString("current_streak")
                        val avgWeeklyHours = document.getString("avg_weekly_hours")
                        val points = document.getString("points")
                        val totalHours = document.getString("total_hours")
                        val minHoursGoal = document.getString("min_hours_goal")
                        val maxHoursGoal = document.getString("max_hours_goal")

                        lblName.text = name
                        lblEmail.text = email
                        lblUsername.text = username
                        lblTotalProjects.text = totalProjects
                        lblCurrentStreak.text = currentStreak
                        lblAvgWeeklyHours.text = avgWeeklyHours
                        lblPoints.text = points
                        lblTotalHours.text = totalHours
                        lblMinHoursGoal.text = minHoursGoal
                        lblMaxHoursGoal.text = maxHoursGoal

                        // Save fetched data to SharedPreferences
                        saveData()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failures
                }
        }
    }

    override fun onPause() {
        super.onPause()
        // Save data when activity is paused
        saveData()
    }
}
