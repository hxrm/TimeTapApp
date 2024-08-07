package com.example.timetapwebapp

data class ProjectData(
    val projectName: String = "",
    val clientName: String = "",
    val deadline: String = "",
    val projectId: Long = 0
) {
    constructor() : this("", "", "", 0)
}
