package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivities : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        val calendarIcon: ImageView = findViewById(R.id.calendarIcon)
        calendarIcon.setOnClickListener {
            val intent = Intent(this, ActivityDatePicker::class.java)
            startActivity(intent)
        }
        val backImageView: ImageView = findViewById(R.id.imageBack)
        backImageView.setOnClickListener {
            finish()
        }
    }
}
