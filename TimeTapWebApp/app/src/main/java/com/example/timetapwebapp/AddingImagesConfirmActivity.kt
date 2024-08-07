package com.example.timetapwebapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddingImagesConfirmActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val imageUris = mutableListOf<Uri>()

    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage

    private lateinit var txtStartDate: EditText
    private lateinit var txtStartTime: EditText
    private lateinit var txtEndTime: EditText
    private lateinit var txtProjectName: EditText
    private lateinit var txtTaskName: EditText
    private lateinit var btnContinue: Button

    private val calendar = Calendar.getInstance()

    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_image_confirm)

        // Initialize Firebase App Check
        val appCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(appCheckProviderFactory)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        // Setup RecyclerView and Adapter
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter(imageUris)
        recyclerView.adapter = imageAdapter

        // Handle image URI from Intent
        intent.getParcelableExtra<Uri>("image_uri")?.let { uri ->
            imageUris.add(uri)
            imageAdapter.notifyItemInserted(imageUris.size - 1)
            uploadImageToFirebase(uri)
        }

        // Initialize UI components
        txtStartDate = findViewById(R.id.txtStartDate)
        txtStartTime = findViewById(R.id.txtStartTime)
        txtEndTime = findViewById(R.id.txtEndTime)
        txtProjectName = findViewById(R.id.txtProjectName)
        txtTaskName = findViewById(R.id.txtTaskName)
        btnContinue = findViewById(R.id.btnContinue)

        // Retrieve the project title from the intent
        val projectTitle = intent.getStringExtra("PROJECT_TITLE")
        if (projectTitle != null) {
            projectViewModel.setProjectTitle(projectTitle)
        }

        // Observe ViewModel data
        projectViewModel.projectTitle.observe(this) { title ->
            txtProjectName.setText(title)
        }

        // Set click listeners for date and time pickers
        txtStartDate.setOnClickListener { showDatePickerDialog(txtStartDate) }
        txtStartTime.setOnClickListener { showTimePickerDialog(txtStartTime) }
        txtEndTime.setOnClickListener { showTimePickerDialog(txtEndTime) }

        // Continue button click listener
        btnContinue.setOnClickListener {
            if (checkAllFields()) {
                saveDataToDatabase()
            } else {
                showErrorToast("Please fill all required fields")
            }
        }

        // Back button click listener
        findViewById<ImageButton>(R.id.imageBack).setOnClickListener {
            navigateBack()
        }

        // Photograph switch listener
        findViewById<Switch>(R.id.photograph).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                navigateToAddPhotosActivity()
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            updateEditText(editText, "yyyy-MM-dd")
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateEditText(editText, "HH:mm")
        }
        TimePickerDialog(
            this,
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateEditText(editText: EditText, format: String) {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        editText.setText(sdf.format(calendar.time))
    }

    private fun checkAllFields(): Boolean {
        return txtStartDate.text.isNotEmpty() && txtStartTime.text.isNotEmpty() && txtEndTime.text.isNotEmpty() && txtProjectName.text.isNotEmpty() && txtTaskName.text.isNotEmpty()
    }

    private fun saveDataToDatabase() {
        val startDate = txtStartDate.text.toString()
        val startTime = txtStartTime.text.toString()
        val endTime = txtEndTime.text.toString()
        val projectName = txtProjectName.text.toString()
        val taskName = txtTaskName.text.toString()

        // Check if any field is empty and show error toast if true
        if (startDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || projectName.isEmpty() || taskName.isEmpty()) {
            showErrorToast("Please fill all required fields")
            return
        }

        val task = Task(startDate, startTime, endTime, projectName, taskName)
        database.child("tasks").push().setValue(task).addOnSuccessListener {
            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show()
            navigateToActivityStartTimer(projectName, task.taskName) // Pass project and task names
        }.addOnFailureListener {
            showErrorToast("Failed to save task")
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageRef = storage.reference.child("images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(uri).addOnSuccessListener {
            Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            showErrorToast("Failed to upload image")
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateBack() {
        val intent = Intent(this, ActivityStartTask::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun navigateToAddPhotosActivity() {
        val intent = Intent(this, AddPhotosActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToActivityStartTimer(projectName: String, taskName: String) {
        val intent = Intent(this, ActivityStartTimer::class.java).apply {
            putExtra("PROJECT_NAME", projectName)
            putExtra("TASK_NAME", taskName)
        }
        startActivity(intent)
    }
}
