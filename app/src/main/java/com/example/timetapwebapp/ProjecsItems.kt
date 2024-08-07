package com.example.timetapwebapp

data class ProjecsItems(
    val category: String = "",
    val date: String = "",
    val description: String = "",
    val endTime: String = "",
    val entryId: Int = 0,
    val photoRequired: Boolean = false,
    val projectName: String = "",
    val startTime: String = "",
    val timeSpent: String = "",
    val photoUrl: String? = null,
    val taskName: String = ""
)
