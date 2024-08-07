package com.example.timetapwebapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

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
    private val KEY_LAST_OPEN_TIMESTAMP = "last_open_timestamp"

    private var lastOpenTimestamp: Long = 0
    private var currentStreak: Int = 0
    private var points: Int = 0 // Added points variable

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
        database = FirebaseDatabase.getInstance()
        firestore = Firebase.firestore

        // Set click listener for the Edit Profile button
        buttonEditProfile.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            intent.putExtra(KEY_CURRENT_STREAK, currentStreak)
            intent.putExtra(KEY_POINTS, points)
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
            // Fetch user details from Firebase Auth
            fetchAuthUserDetails()
            // Fetch user details from Firestore
            fetchFirestoreUserDetails()
            // Fetch total projects count
            fetchTotalProjects()
            // Fetch total hours
            fetchTotalHours()
            // Fetch min and max hours goals
            fetchMinMaxHoursGoals()
            // Calculate streaks
            calculateStreak(mAuth.currentUser!!.uid)
            // Setup real-time listeners for current streak and points
            setupRealTimeListeners(mAuth.currentUser!!.uid)
        }
    }

    private fun loadData() {
        lblName.text = sharedPreferences.getString(KEY_NAME, "Joe Black")
        lblEmail.text = sharedPreferences.getString(KEY_EMAIL, "joeblack@email.com")
        lblUsername.text = sharedPreferences.getString(KEY_USERNAME, "Pablo")
        lblTotalProjects.text = sharedPreferences.getString(KEY_TOTAL_PROJECTS, "0")

        lblCurrentStreak.text = try {
            sharedPreferences.getInt(KEY_CURRENT_STREAK, 8).toString()
        } catch (e: ClassCastException) {
            sharedPreferences.getString(KEY_CURRENT_STREAK, "8")!!.toInt().toString()
        }
        lblAvgWeeklyHours.text = sharedPreferences.getString(KEY_AVG_WEEKLY_HOURS, "12.5 Hrs")

        lblPoints.text = try {
            sharedPreferences.getInt(KEY_POINTS, 1087).toString()
        } catch (e: ClassCastException) {
            sharedPreferences.getString(KEY_POINTS, "1087")!!.toInt().toString()
        }
        lblTotalHours.text = sharedPreferences.getString(KEY_TOTAL_HOURS, "400 Hrs")
        lblMinHoursGoal.text = sharedPreferences.getString(KEY_MIN_HOURS_GOAL, "0 Hrs")
        lblMaxHoursGoal.text = sharedPreferences.getString(KEY_MAX_HOURS_GOAL, "0 Hrs")
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, lblName.text.toString())
        editor.putString(KEY_EMAIL, lblEmail.text.toString())
        editor.putString(KEY_USERNAME, lblUsername.text.toString())
        editor.putString(KEY_TOTAL_PROJECTS, lblTotalProjects.text.toString())
        editor.putInt(KEY_CURRENT_STREAK, lblCurrentStreak.text.toString().toInt()) // Store streak as int
        editor.putString(KEY_AVG_WEEKLY_HOURS, lblAvgWeeklyHours.text.toString())
        editor.putInt(KEY_POINTS, points) // Store points as int
        editor.putString(KEY_TOTAL_HOURS, lblTotalHours.text.toString())
        editor.putString(KEY_MIN_HOURS_GOAL, lblMinHoursGoal.text.toString())
        editor.putString(KEY_MAX_HOURS_GOAL, lblMaxHoursGoal.text.toString())
        editor.putLong(KEY_LAST_OPEN_TIMESTAMP, lastOpenTimestamp)
        editor.apply()
    }

    private fun fetchAuthUserDetails() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            lblName.text = currentUser.displayName ?: "No Name"
            lblEmail.text = currentUser.email ?: "No Email"

            val userId = currentUser.uid
            val userRef = database.getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java) ?: "No Username"
                        lblUsername.text = username
                    } else {
                        lblUsername.text = "No Username"
                    }
                    saveData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching user details", error.toException())
                }
            })
        }
    }

    private fun fetchFirestoreUserDetails() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = firestore.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: "No First Name"
                    val lastName = document.getString("lastName") ?: "No Last Name"
                    val username = document.getString("username") ?: "No Username"

                    lblName.text = "$firstName $lastName"
                    lblUsername.text = username
                } else {
                    lblName.text = "No Name"
                    lblUsername.text = "No Username"
                }
                saveData()
            }.addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }
        }
    }

    private fun fetchTotalProjects() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userProjectsRef = database.getReference("Projects").child(userId)

            userProjectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalProjects = 0
                    if (snapshot.exists()) {
                        for (projectSnapshot in snapshot.children) {
                            if (projectSnapshot.child("projectName").exists()) {
                                totalProjects++
                            }
                        }
                    }
                    lblTotalProjects.text = totalProjects.toString()
                    saveData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching total projects", error.toException())
                }
            })
        }
    }

    private fun fetchTotalHours() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val finishedTimesRef = database.getReference("finishedTimes").child(userId)

            finishedTimesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalMilliseconds: Long = 0
                    if (snapshot.exists()) {
                        for (projectSnapshot in snapshot.children) {
                            for (timeSnapshot in projectSnapshot.children) {
                                val timeElapsed = timeSnapshot.child("timeElapsed").getValue(String::class.java)?.toLongOrNull()
                                if (timeElapsed != null) {
                                    totalMilliseconds += timeElapsed
                                }
                            }
                        }
                    }
                    val totalHours = totalMilliseconds / (1000.0 * 60.0 * 60.0)
                    val formattedTotalHours = String.format("%.4f", totalHours)
                    lblTotalHours.text = "$formattedTotalHours Hrs"
                    saveData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching total hours", error.toException())
                }
            })
        }
    }

    private fun fetchMinMaxHoursGoals() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val minHoursGoal = snapshot.child("min_hours_goal").getValue(Int::class.java) ?: 0
                    val maxHoursGoal = snapshot.child("max_hours_goal").getValue(Int::class.java) ?: 0
                    lblMinHoursGoal.text = "$minHoursGoal Hrs"
                    lblMaxHoursGoal.text = "$maxHoursGoal Hrs"
                    saveData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching min/max hours goal", error.toException())
                }
            })
        }
    }

    private fun calculateStreak(userId: String) {
        val userRef = database.getReference("finishedTimes").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var streak = 0
                if (snapshot.exists()) {
                    for (projectSnapshot in snapshot.children) {
                        if (projectSnapshot.child("projectName").exists()) {
                            streak++
                        }
                    }
                }

                currentStreak = streak
                points = currentStreak * 10 // Calculate points based on streak

                lblCurrentStreak.text = currentStreak.toString()
                lblPoints.text = points.toString()

                saveData()

                val userStreakRef = database.getReference("users").child(userId).child(KEY_CURRENT_STREAK)
                userStreakRef.setValue(currentStreak).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseSuccess", "Current streak updated successfully.")
                    } else {
                        Log.e("FirebaseError", "Error updating current streak", task.exception)
                    }
                }

                val userPointsRef = database.getReference("users").child(userId).child(KEY_POINTS)
                userPointsRef.setValue(points).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseSuccess", "Points updated successfully.")
                    } else {
                        Log.e("FirebaseError", "Error updating points", task.exception)
                    }
                }

                lastOpenTimestamp = System.currentTimeMillis()
                val userTimestampRef = database.getReference("users").child(userId).child(KEY_LAST_OPEN_TIMESTAMP)
                userTimestampRef.setValue(lastOpenTimestamp)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error calculating streak", error.toException())
            }
        })
    }

    private fun setupRealTimeListeners(userId: String) {
        val userRef = database.getReference("users").child(userId)

        userRef.child(KEY_CURRENT_STREAK).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentStreak = snapshot.getValue(Int::class.java) ?: 0
                lblCurrentStreak.text = currentStreak.toString()
                points = currentStreak * 10
                lblPoints.text = points.toString()
                saveData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching current streak", error.toException())
            }
        })

        userRef.child(KEY_POINTS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                points = snapshot.getValue(Int::class.java) ?: 0
                lblPoints.text = points.toString()
                saveData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching points", error.toException())
            }
        })
    }
}
