package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityTimesheetView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimesheetAdapter
    private val timesheetList = mutableListOf<TimesheetItem>()
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_sheet_view)

        recyclerView = findViewById(R.id.recycler_view2)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TimesheetAdapter(timesheetList) { timesheetItem ->
            val intent = Intent(this, TimesheetActivity::class.java)
            intent.putExtra("PROJECT_NAME", timesheetItem.projectName)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        fetchTimesheetData()

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener {
            finish()
        }

        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        calendarButton.setOnClickListener {
            showDateRangePicker()
        }

        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTimesheetData(searchView.query.toString(), newText)
                return true
            }
        })
    }

    private fun showDateRangePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
            val startDate = formatDate(dateRange.first)
            val endDate = formatDate(dateRange.second)
            val dateRangeString = "$startDate - $endDate"
            searchView.setQuery(dateRangeString, true)
        }

        dateRangePicker.show(supportFragmentManager, "date_range_picker")
    }

    private fun formatDate(timeInMillis: Long?): String {
        timeInMillis ?: return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    private fun filterTimesheetData(dateRange: String?, query: String?) {
        var filteredList = timesheetList

        if (dateRange != null && dateRange.isNotEmpty()) {
            val dates = dateRange.split(" - ")
            if (dates.size == 2) {
                val startDate = dates[0]
                val endDate = dates[1]
                filteredList = filteredList.filter {
                    it.date in startDate..endDate
                }.toMutableList()
            }
        }

        if (!query.isNullOrBlank()) {
            filteredList = filteredList.filter {
                it.projectName.contains(query, ignoreCase = true)
            }.toMutableList()
        }

        adapter.updateData(filteredList)
    }

    private fun fetchTimesheetData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val database = FirebaseDatabase.getInstance().getReference("finishedTimes")

            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    timesheetList.clear()
                    for (timesheetSnapshot in snapshot.children) {
                        val timesheetItem = timesheetSnapshot.getValue(TimesheetItem::class.java)
                        timesheetItem?.let {
                            val timeInMillis = it.getTimeElapsedAsMillis()
                            val formattedTime = formatTime(timeInMillis)
                            it.formattedTime = formattedTime
                            timesheetList.add(it)
                        }
                    }
                    filterTimesheetData(searchView.query.toString(), null)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds / (1000 * 60)) % 60).toInt()
        val seconds = (milliseconds / 1000 % 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
