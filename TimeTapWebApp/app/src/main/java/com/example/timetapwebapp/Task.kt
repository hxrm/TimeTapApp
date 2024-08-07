package com.example.timetapwebapp

data class Task(
    val startDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val projectName: String = "",
    val taskName: String = "",
    val date: String = ""
) {
    constructor() : this("", "", "", "", "","")
}

