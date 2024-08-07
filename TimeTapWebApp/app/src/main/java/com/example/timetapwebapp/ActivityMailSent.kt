package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityMailSent : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail_sent)

        // Initialize the "Ok" button
        val btnOkay: Button = findViewById(R.id.btnOkay)

        // Set a click listener on the button
        btnOkay.setOnClickListener {
            // Create an Intent to start SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}
