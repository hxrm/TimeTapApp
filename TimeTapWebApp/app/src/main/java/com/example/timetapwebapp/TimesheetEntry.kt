package com.example.timetapwebapp

data class TimesheetEntry(
    val projectName: String = "",
    val category: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val isPhotoRequired: Boolean = false
) {
    // Default constructor required by Firebase
    constructor() : this("", "", "", "", "", "", false)
}
