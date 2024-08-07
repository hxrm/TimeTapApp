package com.example.timetapwebapp

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize UI elements
        txtUsername = findViewById(R.id.txtUsername)
        txtPassword = findViewById(R.id.txtPassword)
        btnSignIn = findViewById(R.id.btnOkay)
        signUpTextView = findViewById(R.id.clkSignUp)
        forgotPasswordTextView = findViewById(R.id.textView5)

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listeners
        signUpTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        val usernameOrEmail = txtUsername.text.toString().trim()
        val password = txtPassword.text.toString().trim()

        if (usernameOrEmail.isEmpty()) {
            txtUsername.error = "Username or Email is required"
            txtUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            txtPassword.error = "Password is required"
            txtPassword.requestFocus()
            return
        }

        if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
            // Input is an email address
            signInWithEmail(usernameOrEmail, password)
        } else {
            // Input is a username
            fetchEmailFromUsername(usernameOrEmail, password)
        }
    }

    private fun fetchEmailFromUsername(username: String, password: String) {
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                } else {
                    val email = documents.documents[0].getString("email")
                    if (email != null) {
                        signInWithEmail(email, password)
                    } else {
                        Toast.makeText(this, "Email not found for the username", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching username: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInWithEmail(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkProjectAndNavigate(email)
                } else {
                    Toast.makeText(this, "Sign In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkProjectAndNavigate(email: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.documents[0].id

                    // Reference to the specific user's projects node
                    val projectsRef = FirebaseDatabase.getInstance().getReference("Projects").child(userId)

                    // Check if there are any projects for this user
                    projectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // If projects exist, navigate to ActivityStartTask
                                startActivity(Intent(this@SignInActivity, ActivityStartTask::class.java))
                            } else {
                                // If no projects exist, navigate to HomeActivity
                                navigateToHomeActivity()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle any errors here
                            Toast.makeText(this@SignInActivity, "Error checking projects: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                            // Log the error
                            Log.e("SignInActivity", "Error checking projects", databaseError.toException())
                        }
                    })
                } else {
                    // If no user found, show an error message
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching user: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is View) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard() {
        // Add your method to hide the keyboard
    }
}
