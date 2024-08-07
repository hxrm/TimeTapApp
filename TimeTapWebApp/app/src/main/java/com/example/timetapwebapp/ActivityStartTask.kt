package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView

class ActivityStartTask : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var pointsCounter: TextView
    private lateinit var pointsValue: TextView
    private lateinit var streakCounter: TextView
    private lateinit var streakValue: TextView
    private lateinit var projectTitle: TextView
    private lateinit var rightArrow: ImageView
    private lateinit var customToggle: ImageView
    private lateinit var plusButton: ImageView

    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_task)

        // Initialize the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_drawer)

        // Set up ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Show the hamburger icon to open and close the drawer
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the NavigationItemSelectedListener
        navigationView.setNavigationItemSelectedListener(this)

        // Initialize UI elements
        pointsCounter = findViewById(R.id.pointsCounter)
        pointsValue = findViewById(R.id.pointsValue)
        streakCounter = findViewById(R.id.streakCounter)
        streakValue = findViewById(R.id.streakValue)
        projectTitle = findViewById(R.id.projectTitle)
        rightArrow = findViewById(R.id.rightArrow)
        customToggle = findViewById(R.id.toggle)
        plusButton = findViewById(R.id.plus)

        // Observe ViewModel data
        projectViewModel.projectTitle.observe(this, Observer { title ->
            projectTitle.text = title
        })

        // Retrieve the project title from the intent and set it in the ViewModel
        intent.getStringExtra("PROJECT_TITLE")?.let {
            projectViewModel.setProjectTitle(it)
        }

        // Set initial values
        pointsCounter.text = "Points"
        streakCounter.text = "Streak"

        // Set up button click listener to start a new timesheet
        findViewById<Button>(R.id.startTimesheet).setOnClickListener {
            val timeManagerIntent = Intent(this, AddingImagesConfirmActivity::class.java)
            // Pass the project title to the next activity
            val projectTitle = projectViewModel.projectTitle.value
            timeManagerIntent.putExtra("PROJECT_TITLE", projectTitle)
            startActivity(timeManagerIntent)
        }

        // Set up right arrow click listener
        rightArrow.setOnClickListener {
            val intent = Intent(this, ActiveProjectsActivity::class.java)
            startActivity(intent)
        }

        // Set up custom toggle click listener
        customToggle.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set up plus button click listener
        plusButton.setOnClickListener {
            val intent = Intent(this, NewProjectActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Handle navigation view item clicks here
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
