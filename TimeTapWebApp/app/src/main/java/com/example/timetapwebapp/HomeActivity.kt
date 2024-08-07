package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val plusButton = findViewById<ImageView>(R.id.plus)
        // Set an OnClickListener to navigate to NewProjectActivity
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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_myprofile -> {

                startActivity(
                    Intent(
                        this@HomeActivity,
                        ActivityProfileDetails::class.java
                    )
                )
            }

            R.id.action_category -> {

                startActivity(
                    Intent(
                        this@HomeActivity,
                        CategoriesActivities::class.java
                    )
                )
            }

            R.id.action_timesheet_list -> {

                startActivity(
                    Intent(
                        this@HomeActivity,
                        ActivityTimeList::class.java
                    )
                )
            }

            R.id.action_analytics -> {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        ActionAnalyticActivity::class.java
                    )
                )
            }

            R.id.action_logout -> {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        SignInActivity::class.java
                    )
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
