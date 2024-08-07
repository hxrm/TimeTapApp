package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val okButton: Button = findViewById(R.id.btnOkay)
        okButton.setOnClickListener {
            // Create an Intent to start SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            // Clear the activity stack and start a new task
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Close the current activity
        }
    }
}