package com.example.timetapwebapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ActivityEditProfile : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var editTextName: EditText
    private lateinit var editTextSurname: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextMinHrs: EditText
    private lateinit var editTextMaxHrs: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI elements
        editTextName = findViewById(R.id.txtName)
        editTextSurname = findViewById(R.id.txtSurname)
        editTextEmail = findViewById(R.id.txtEmail)
        editTextMinHrs = findViewById(R.id.txtMinHrs)
        editTextMaxHrs = findViewById(R.id.txtMaxHrs)
        btnSave = findViewById(R.id.btnSave)

        // Set click listener for the Save button
        btnSave.setOnClickListener {
            saveChanges()
        }

        // Load current user data into fields
        loadCurrentUserData()
    }

    private fun loadCurrentUserData() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val minHoursGoal = snapshot.child("min_hours_goal").getValue(Int::class.java) ?: 0
                val maxHoursGoal = snapshot.child("max_hours_goal").getValue(Int::class.java) ?: 0
                editTextName.setText(name)
                editTextSurname.setText(surname)
                editTextEmail.setText(email)
                editTextMinHrs.setText(minHoursGoal.toString())
                editTextMaxHrs.setText(maxHoursGoal.toString())
            }
        }
    }

    private fun saveChanges() {
        val userId = mAuth.currentUser?.uid
        val name = editTextName.text.toString()
        val surname = editTextSurname.text.toString()
        val email = editTextEmail.text.toString()
        val minHoursGoal = editTextMinHrs.text.toString().toIntOrNull() ?: 0
        val maxHoursGoal = editTextMaxHrs.text.toString().toIntOrNull() ?: 0

        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            val userMap = mapOf(
                "name" to name,
                "surname" to surname,
                "email" to email,
                "min_hours_goal" to minHoursGoal,
                "max_hours_goal" to maxHoursGoal
            )
            userRef.updateChildren(userMap).addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Finish the activity after saving changes
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
