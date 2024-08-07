// ImageViewerActivity.kt
package com.example.timetapwebapp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private var imageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_image_confirm)

        // Retrieve image URIs from intent
        imageUris = intent.getParcelableArrayListExtra("imageUris") ?: mutableListOf()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter(imageUris)
        recyclerView.adapter = imageAdapter
    }
}
