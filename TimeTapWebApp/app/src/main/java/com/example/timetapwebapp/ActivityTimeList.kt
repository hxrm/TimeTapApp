package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ActivityTimeList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.time_list_activity)

        val btnCalendar = findViewById<Button>(R.id.btnCalendar)

        // Set OnClickListener for btnCalendar to navigate to NewProjectActivity
        btnCalendar.setOnClickListener {
            val intent = Intent(this, ActivityDatePicker::class.java)
            startActivity(intent)
        }

        val imageBack = findViewById<ImageButton>(R.id.imageBack)

        imageBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        }
    }