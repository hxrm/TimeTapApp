package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class ActivityRestartFinish : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pause_finish)


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_drawer)

        // Set up the toggle button to open and close the drawer
        val toggleButton: ImageView = findViewById(R.id.toggle)
        toggleButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set navigation item click listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_myprofile -> {
                    startActivity(
                        Intent(
                            this@ActivityRestartFinish,
                            ProjectDetailsActivity::class.java
                        )
                    )
                }

                R.id.action_category -> {
                    startActivity(
                        Intent(
                            this@ActivityRestartFinish,
                            CategoriesActivities::class.java
                        )
                    )
                }

                R.id.action_timesheet_list -> {
                    startActivity(Intent(this@ActivityRestartFinish, ActivityTimeList::class.java))
                }

                R.id.action_analytics -> {
                    startActivity(
                        Intent(
                            this@ActivityRestartFinish,
                            ActionAnalyticActivity::class.java
                        )
                    )
                }

                R.id.action_logout -> {
                    startActivity(Intent(this@ActivityRestartFinish, SignInActivity::class.java))

                    finish()
                }
            }
            // Close the drawer after an item is selected
            drawerLayout.closeDrawer(GravityCompat.START)
            true // Return true to indicate the item is selected
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
