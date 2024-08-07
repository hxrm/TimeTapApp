package com.example.timetapwebapp

data class Timesheets(
    var categories: String = "",
    var startDate: String = "",
    var description: String = "",
    var endTime: String = "",
    var imageUrl: String = "",
    var photoRequired: Boolean = false,
    var projectName: String = "",
    var startTime: String = "",
    var taskName: String = "",
    var timeSpent: String = ""
)
