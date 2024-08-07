package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var txtProjectName: EditText
    private lateinit var txtClient: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtPasswordConfirm: EditText
    private lateinit var txtFirstName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnCancel: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        txtFirstName = findViewById(R.id.txtProjectName)
        txtLastName = findViewById(R.id.txtClient)
        txtEmail = findViewById(R.id.txtEmail)
        txtUsername = findViewById(R.id.txtUsername)
        txtPassword = findViewById(R.id.txtPassword)
        txtPasswordConfirm = findViewById(R.id.txtPasswordConfirm)
        btnSignUp = findViewById(R.id.btnLater)
        btnCancel = findViewById(R.id.btnCancel)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnSignUp.setOnClickListener {
            checkIfUserExistsAndSignUp()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun checkIfUserExistsAndSignUp() {
        val email = txtEmail.text.toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.error = "Please provide a valid email"
            txtEmail.requestFocus()
            return
        }

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.isEmpty == false) {
                        Toast.makeText(
                            this,
                            "Email already exists. Please sign in.",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToSignInActivity()
                    } else {
                        signUpUser()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Failed to check if user exists: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun signUpUser() {
        val firstName = txtFirstName.text.toString().trim()
        val lastName = txtLastName.text.toString().trim()
        val email = txtEmail.text.toString().trim()
        val username = txtUsername.text.toString().trim()
        val password = txtPassword.text.toString().trim()
        val confirmPassword = txtPasswordConfirm.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.error = "Please provide a valid email"
            txtEmail.requestFocus()
            return
        }

        if (password != confirmPassword) {
            txtPasswordConfirm.error = "Passwords do not match"
            txtPasswordConfirm.requestFocus()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(firstName, lastName, email, username)
                } else {
                    Toast.makeText(
                        this,
                        "Sign Up Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(
        firstName: String,
        lastName: String,
        email: String,
        username: String
    ) {
        val user = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "username" to username
        )

        db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                    navigateToWelcomeActivity(firstName, lastName)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to register user: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToWelcomeActivity(firstName: String, lastName: String) {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.putExtra("firstName", firstName)
        intent.putExtra("lastName", lastName)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
