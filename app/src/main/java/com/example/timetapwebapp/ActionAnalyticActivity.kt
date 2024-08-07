package com.example.timetapwebapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActionAnalyticActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val totalTimesByDate = mutableMapOf<String, Float>()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        barChart = findViewById(R.id.barChart)
        lineChart = findViewById(R.id.lineChart)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        val imageCalendar = findViewById<ImageView>(R.id.imageCalendar)
        imageCalendar.setOnClickListener {
            showDateRangePicker()
        }

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener {
            finish()
        }

        listenForRealtimeUpdates()
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
            val startDate = Date(selection.first!!)
            val endDate = Date(selection.second!! + (24 * 60 * 60 * 1000) - 1)
            fetchDataWithinDateRange(startDate, endDate)
        }
    }

    private fun listenForRealtimeUpdates() {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        fetchDataWithinDateRange(startDate, endDate)
    }

    private fun fetchDataWithinDateRange(startDate: Date, endDate: Date) {
        val finishedTimesRef = database.child("finishedTimes").child(currentUserId!!)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        finishedTimesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val finishedTimes = snapshot.children.mapNotNull {
                    val entry = it.getValue(TimeEntry::class.java)
                    entry?.takeIf { timeEntry ->
                        val entryDate = dateFormat.parse(timeEntry.currentDate)
                        entryDate.after(startDate) && entryDate.before(endDate)
                    }
                }
                updateTotalTimesByDate(finishedTimes)
                displayBarChart()
                displayLineChart()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateTotalTimesByDate(finishedTimes: List<TimeEntry>) {
        totalTimesByDate.clear()

        for (entry in finishedTimes) {
            val date = entry.currentDate
            val elapsedTimeMillis = entry.timeElapsed.toLong()
            val elapsedTimeHours = elapsedTimeMillis / (1000 * 60 * 60).toFloat()
            totalTimesByDate[date] = totalTimesByDate.getOrDefault(date, 0f) + elapsedTimeHours
        }
    }

    private fun displayBarChart() {
        val entries = totalTimesByDate.entries.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.value)
        }

        val dataSet = BarDataSet(entries, "Total Hours")
        val barData = BarData(dataSet)
        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(totalTimesByDate.keys.toList())
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        barChart.invalidate()
    }

    private fun displayLineChart() {
        val entries = totalTimesByDate.entries.mapIndexed { index, data ->
            Entry(index.toFloat(), data.value)
        }

        val dataSet = LineDataSet(entries, "Total Hours")
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(totalTimesByDate.keys.toList())
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.invalidate()
    }
}

data class TimeEntry(
    val currentDate: String = "",
    val timeElapsed: String = ""
)
