package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ActivityEditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val imageBack = findViewById<ImageButton>(R.id.imageBack)

        imageBack.setOnClickListener {
            onBackPressed()
        }

        val editTextName = findViewById<EditText>(R.id.txtName)
        val editTextEmail = findViewById<EditText>(R.id.txtEmail)

        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val updatedName = editTextName.text.toString()
            val updatedEmail = editTextEmail.text.toString()

            val intent = Intent(this, ActivityProfileDetails::class.java).apply {
                putExtra("EXTRA_NAME", updatedName)
                putExtra("EXTRA_EMAIL", updatedEmail)
            }
            startActivity(intent)
            finish()
        }
    }
}
