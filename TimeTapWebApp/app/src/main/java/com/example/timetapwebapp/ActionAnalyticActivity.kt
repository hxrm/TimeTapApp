package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class ActionAnalyticActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val imageCalendar = findViewById<ImageView>(R.id.imageCalendar)

        imageCalendar.setOnClickListener {
            val intent = Intent(this, ActivityDatePicker::class.java)
            startActivity(intent)
        }

        val imageBack = findViewById<ImageView>(R.id.imageBack)

        imageBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
