package com.example.timetapwebapp


data class TimerStats(
    val projectName: String = "",
    val taskName: String = "",
    val timeElapsed: String = "5",
    val finishTime: Long = System.currentTimeMillis(),
    val currentDate: String
)
