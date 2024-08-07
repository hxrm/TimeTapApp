package com.example.timetapwebapp

data class TimesheetItem(
    val projectName: String = "",
    val timeElapsed: String = "",
    var formattedTime: String = "",
    val date: String = ""
) {


    fun getTimeElapsedAsMillis(): Long {
        return timeElapsed.toLongOrNull() ?: 0L
    }
}
