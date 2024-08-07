package com.example.timetapwebapp


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AddPhotosActivity : AppCompatActivity() {

    private lateinit var btnAddCamera: Button
    private var photoUri: Uri? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            // Permission denied, handle appropriately
        }
    }

    private val requestMediaPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true &&
            permissions[Manifest.permission.READ_MEDIA_VIDEO] == true &&
            permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
        ) {
            // Permissions granted, proceed with action
        } else {
            // Permissions denied, handle appropriately
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess: Boolean ->
        if (isSuccess) {
            photoUri?.let { uri ->
                navigateToAddingImageConfirm(uri)
            }
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    navigateToAddingImageConfirm(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photos)

        val backButton: ImageButton = findViewById(R.id.imageBack)
        backButton.setOnClickListener {
            val intent = Intent(this, AddingImagesConfirmActivity::class.java)
            startActivity(intent)
            finish() // Optional: close the current activity
        }

        val btnAddGallery: Button = findViewById(R.id.btnAddGallery)
        btnAddGallery.setOnClickListener {
            openGallery()
        }

        btnAddCamera = findViewById(R.id.btnAddCamera)
        btnAddCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        requestMediaPermissions()
    }

    private fun requestMediaPermissions() {
        requestMediaPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        )
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun openCamera() {
        photoUri = createImageUri()
        photoUri?.let {
            takePictureLauncher.launch(it)
        }
    }

    private fun createImageUri(): Uri? {
        val contentResolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun navigateToAddingImageConfirm(uri: Uri) {
        val intent = Intent(this, AddingImagesConfirmActivity::class.java)
        intent.putExtra("image_uri", uri)
        startActivity(intent)
        finish()
    }

    private fun handleImageUri(uri: Uri) {
        navigateToAddingImageConfirm(uri)
    }
}
