package com.example.timetapwebapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriesActivities : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        database = FirebaseDatabase.getInstance().getReference("timesheets").child(currentUser!!.uid)

        val calendarIcon: ImageView = findViewById(R.id.calendarIcon)
        calendarIcon.setOnClickListener {
            showDateRangePicker()
        }

        val backImageView: ImageView = findViewById(R.id.imageBack)
        backImageView.setOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CategoriesAdapter()
        recyclerView.adapter = adapter

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timesheetsMap = mutableMapOf<String, CategoryTimesheet>()
                for (timesheetSnapshot in snapshot.children) {
                    val timesheet = timesheetSnapshot.getValue(CategoryTimesheet::class.java)
                    if (timesheet != null) {
                        val category = timesheet.categories
                        if (timesheetsMap.containsKey(category)) {
                            val existingTimesheet = timesheetsMap[category]
                            existingTimesheet?.endTime = existingTimesheet?.endTime + ", " + timesheet.endTime
                        } else {
                            timesheetsMap[category] = timesheet
                        }
                    }
                }
                val aggregatedTimesheets = timesheetsMap.values.toList()
                adapter.submitList(aggregatedTimesheets)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun showDateRangePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second
        }

        dateRangePicker.addOnNegativeButtonClickListener {
            // Handle cancellation
        }

        dateRangePicker.addOnCancelListener {
            // Handle cancel
        }
    }
}
