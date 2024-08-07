package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.functions.FirebaseFunctions

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var txtEmail: TextInputEditText
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        txtEmail = findViewById(R.id.txtEmail)
        functions = FirebaseFunctions.getInstance()

        val goBackButton = findViewById<Button>(R.id.btnGoBack)
        goBackButton.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnSend = findViewById<Button>(R.id.btnSend)
        btnSend.setOnClickListener {
            sendPassword()
        }
    }

    private fun sendPassword() {
        val email = txtEmail.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        // Call the Cloud Function
        functions.getHttpsCallable("sendPassword")
            .call(mapOf("email" to email))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password sent to your email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ActivityMailSent::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error sending password email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
