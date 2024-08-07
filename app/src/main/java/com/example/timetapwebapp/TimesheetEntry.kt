package com.example.timetapwebapp

data class TimesheetEntry(
    val projectName: String = "",
    val taskName: String = "", // Added taskName field
    val category: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val isPhotoRequired: Boolean = false,
    val entryId: Int = 0,
    val imageUrl: String? = null
) {

    constructor() : this("", "", "", "", "", "", "", false, 0, null)
}
