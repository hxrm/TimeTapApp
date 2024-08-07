package com.example.timetapwebapp

data class AddingImageTimesheet(

    val taskName: String,
    val categories: String,
    val description: String,
    val startTime: String, // assuming these are timestamps
    val endTime: String,
    val selectedEndTime: String,
    val isPhotoRequired: Boolean,
    val imageUrl: String? = null // Image URL is optional){}
)
