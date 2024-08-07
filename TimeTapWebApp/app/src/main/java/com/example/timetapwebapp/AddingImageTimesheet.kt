package com.example.timetapwebapp

data class AddingImageTimesheet(

    val taskName: String,
    val categories: String,
    val description: String,
    val startTime: Long, // assuming these are timestamps
    val endTime: Long,
    val isPhotoRequired: Boolean,
    val imageUrl: String? = null // Image URL is optional
)
