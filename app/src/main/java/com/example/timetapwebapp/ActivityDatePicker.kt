package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker

class ActivityDatePicker : AppCompatActivity() {
    private var dateRangePicker: MaterialDatePicker<Pair<Long, Long>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_range)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        showDateRangePicker()


        val closeButton = findViewById<ImageButton>(R.id.imageBack)


        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun showDateRangePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
        val builder: MaterialDatePicker.Builder<Pair<Long, Long>> =
            MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Select Date Range")
        builder.setCalendarConstraints(constraintsBuilder.build())

        dateRangePicker = builder.build()
        dateRangePicker?.show(supportFragmentManager, "DATE_RANGE_PICKER")

        dateRangePicker?.addOnPositiveButtonClickListener { selectedDates ->
            val startDate = selectedDates.first
            val endDate = selectedDates.second
            // Use the selected start and end dates
            Log.d("ActivityDatePicker", "Date range selected: $startDate to $endDate")
        }

        dateRangePicker?.addOnCancelListener {
            navigateBack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateBack()
        return true
    }

    private fun navigateBack() {
        // Get the source activity from intent extra
        val fromActivity = intent.getStringExtra("FROM_ACTIVITY")

        // Navigate back to the source activity
        val intent = when (fromActivity) {
            "ActivityProfileDetails" -> Intent(this, ActivityEditProfile::class.java)
            "CategoriesActivity" -> Intent(this, CategoriesActivities::class.java)
            else -> null
        }

        intent?.let {
            startActivity(it)
            finish()
        } ?: finish()
    }
}
