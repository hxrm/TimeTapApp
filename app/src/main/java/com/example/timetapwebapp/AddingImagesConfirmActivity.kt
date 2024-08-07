package com.example.timetapwebapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AddingImagesConfirmActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val imageUris = mutableListOf<Uri>()

    private lateinit var txtProjectName: EditText
    private lateinit var txtTaskName: EditText
    private lateinit var txtStartDate: EditText
    private lateinit var txtStartTime: EditText
    private lateinit var txtEndTime: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var txtDescription: EditText

    private var selectedStartDate: Long = 0
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val categories = mutableListOf("Select or add a category")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_image_confirm)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter(imageUris)
        recyclerView.adapter = imageAdapter

        txtProjectName = findViewById(R.id.txtProjectName)
        txtTaskName = findViewById(R.id.txtTaskName)
        txtStartDate = findViewById(R.id.txtStartDate)
        txtStartTime = findViewById(R.id.txtStartTime)
        txtEndTime = findViewById(R.id.txtEndTime)
        categorySpinner = findViewById(R.id.spinnerCategories)
        txtDescription = findViewById(R.id.txtDescription)

        val projectName = intent.getStringExtra("PROJECT_TITLE")
            ?: intent.getStringExtra("PROJECT_NAME")
            ?: "No project"
        txtProjectName.setText(projectName)
        txtProjectName.isEnabled = false

        // Set up date and time pickers
        txtStartDate.setOnClickListener { showDatePickerDialog(txtStartDate) }
        txtStartTime.setOnClickListener { showTimePickerDialog(txtStartTime) }
        txtEndTime.setOnClickListener { showTimePickerDialog(txtEndTime) }

        // Set up spinner adapter
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        categorySpinner.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showCategorySelectionDialog()
            }
            true
        }

        val btnConfirm: Button = findViewById(R.id.btnContinue)
        btnConfirm.setOnClickListener {
            if (validateFields()) {
                saveData()
            }
        }

        val backButton: ImageButton = findViewById(R.id.imageBack)
        backButton.setOnClickListener {
            finish()
        }

        val photographSwitch: Switch = findViewById(R.id.photograph)
        photographSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val intent = Intent(this, AddPhotosActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_ADD_PHOTO)
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val date = "$dayOfMonth/${month + 1}/$year"
            editText.setText(date)
            calendar.set(year, month, dayOfMonth)
            selectedStartDate = calendar.timeInMillis
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val time = String.format("%02d:%02d", hourOfDay, minute)
            editText.setText(time)

            when (editText.id) {
                R.id.txtStartTime -> selectedStartTime = time
                R.id.txtEndTime -> selectedEndTime = time
            }
        }, hour, minute, true)
        timePickerDialog.show()
    }

    private fun showCategorySelectionDialog() {
        fetchCategoriesFromFirebase { fetchedCategories ->
            val dialogView = layoutInflater.inflate(R.layout.dialog_category_selection, null)
            val editText = dialogView.findViewById<EditText>(R.id.editTextCategory)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCategories)

            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = CategoryDialogAdapter(fetchedCategories) { selectedCategory ->
                if (!categories.contains(selectedCategory)) {
                    categories.add(selectedCategory)
                    categoryAdapter.notifyDataSetChanged()
                }
                categorySpinner.setSelection(categories.indexOf(selectedCategory))
            }
            recyclerView.adapter = adapter

            val dialog = AlertDialog.Builder(this)
                .setTitle("Select or Enter Category")
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    val newCategory = editText.text.toString()
                    if (newCategory.isNotEmpty() && !categories.contains(newCategory)) {
                        categories.add(newCategory)
                        categoryAdapter.notifyDataSetChanged()
                        categorySpinner.setSelection(categories.indexOf(newCategory))
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }
    }

    private fun fetchCategoriesFromFirebase(callback: (List<String>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("timesheets").child(userId)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedCategories = mutableListOf<String>()
                    for (timesheetSnapshot in snapshot.children) {
                        val category = timesheetSnapshot.child("categories").getValue(String::class.java)
                        if (category != null && category.isNotEmpty()) {
                            fetchedCategories.add(category)
                        }
                    }
                    callback(fetchedCategories)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (txtProjectName.text.isEmpty()) {
            txtProjectName.error = "Project name is required"
            isValid = false
        }
        if (txtTaskName.text.isEmpty()) {
            txtTaskName.error = "Task name is required"
            isValid = false
        }
        if (txtStartDate.text.isEmpty()) {
            txtStartDate.error = "Start date is required"
            isValid = false
        }
        if (txtStartTime.text.isEmpty()) {
            txtStartTime.error = "Start time is required"
            isValid = false
        }
        if (txtEndTime.text.isEmpty()) {
            txtEndTime.error = "End time is required"
            isValid = false
        }
        if (txtDescription.text.isEmpty()) {
            txtDescription.error = "Description is required"
            isValid = false
        }
        if (categorySpinner.selectedItem.toString() == "Select or add a category") {
            Toast.makeText(this, "Please select or enter a category", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveData() {
        val selectedCategory = categorySpinner.selectedItem.toString()

        val projectName = txtProjectName.text.toString()
        val taskName = txtTaskName.text.toString()
        val description = txtDescription.text.toString()
        val startDate = txtStartDate.text.toString()

        // Create an instance of AddingImageTimesheet
        val timesheetData = AddingImageTimesheet(
            taskName,
            selectedCategory,
            description,
            startDate,
            selectedStartTime,
            selectedEndTime,
            true, // Assuming photos are required
            imageUris.firstOrNull()?.toString()
        )

        // Get the Firebase database reference
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val timesheetRef = database.reference.child("timesheets").child(userId)

            // Save the timesheet data
            timesheetRef.push().setValue(timesheetData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to ActivityStartTimer with project title and task name
                    val intent = Intent(this, ActivityStartTimer::class.java).apply {
                        putExtra("PROJECT_NAME", txtProjectName.text.toString())
                        putExtra("TASK_NAME", txtTaskName.text.toString())
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_PHOTO && resultCode == RESULT_OK) {
            data?.getStringExtra("image_uri")?.let { uriString ->
                Uri.parse(uriString)?.let { uri ->
                    imageUris.clear()
                    imageUris.add(uri)
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_ADD_PHOTO = 1001
    }
}
