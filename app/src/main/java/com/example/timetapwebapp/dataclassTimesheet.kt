package com.example.timetapwebapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timesheets")
data class Timesheet(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val category: String,
    val startTime: Long // Store time in milliseconds
)
