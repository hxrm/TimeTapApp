package com.example.timetapwebapp

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timetapwebapp.databinding.ActivityNewProjectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class NewProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewProjectBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Projects")


        binding.btnSaveProject.setOnClickListener {
            saveProject()
        }


        binding.imageBack.setOnClickListener {
            finish()
        }


        binding.Deadline.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.Deadline.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun saveProject() {
        val projectName = binding.txtProjectName.text.toString().trim()
        val clientName = binding.txtClient.text.toString().trim()
        val deadline = binding.Deadline.text.toString().trim()

        if (projectName.isEmpty() || clientName.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }


        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val project = ProjectData(projectName, clientName, deadline, System.currentTimeMillis())


        databaseReference.child(userId).push().setValue(project)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Project saved", Toast.LENGTH_SHORT).show()


                    val sharedPreferences = getSharedPreferences("com.example.timetapwebapp.PREFERENCES", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("PROJECT_TITLE", projectName)
                    editor.apply()


                    val intent = Intent(this, ActivityStartTask::class.java)
                    intent.putExtra("PROJECT_TITLE", projectName)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save project", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
