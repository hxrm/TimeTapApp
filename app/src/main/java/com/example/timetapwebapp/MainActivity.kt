package com.example.timetapwebapp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import org.json.JSONObject
import java.util.Base64

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure the status bar and navigation bar are transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        // Adjust padding for insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBarsInsets.left,
                top = systemBarsInsets.top,
                right = systemBarsInsets.right,
                bottom = systemBarsInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        // Get reference to the Get Started button
        val getStartedButton: Button = findViewById(R.id.button4)

        // Delay the appearance of the button by 5 seconds (5000 milliseconds)
        Handler(Looper.getMainLooper()).postDelayed({
            getStartedButton.visibility = View.VISIBLE
        }, 5000)

        // Set OnClickListener for the Get Started button
        getStartedButton.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase
        Firebase.initialize(context = this)

        // Initialize Firebase App Check with debug provider factory
        setupAppCheck()

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Get the current user and their ID token
        getCurrentUserToken()
    }

    private fun setupAppCheck() {
        val appCheck = Firebase.appCheck
        appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        Log.d("AppCheck", "App Check Debug Provider initialized")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentUserToken() {
        val user = auth.currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    idToken?.let {
                        decodeAndLogIdToken(it)
                    }
                } else {
                    Log.e("IDToken", "Error getting ID token: ${task.exception}")
                }
            }
        } else {
            Log.e("IDToken", "No user is currently signed in.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decodeAndLogIdToken(idToken: String) {
        try {
            // Split the JWT token into header, payload, and signature
            val parts = idToken.split(".")
            if (parts.size < 2) {
                Log.e("IDToken", "Invalid ID token.")
                return
            }
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))

            // Decode the payload
            val payloadJson = JSONObject(payload)
            Log.d("IDToken", "Decoded Token: $payloadJson")

            // Check if it's a valid token
            val issuer = payloadJson.optString("iss", "")
            if (issuer == "https://securetoken.google.com/backend-c0faa") {
                Log.d("IDToken", "This is a valid ID token.")
            } else {
                Log.d("IDToken", "This may be a debug token.")
            }

        } catch (e: Exception) {
            Log.e("IDToken", "Error decoding ID token: ${e.message}")
        }
    }
}
