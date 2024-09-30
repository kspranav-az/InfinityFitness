package com.example.infinityfitness

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.infinityfitness.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    private var isBiometricAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the executor for biometric prompt
        executor = ContextCompat.getMainExecutor(this)

        // Check if biometric authentication is available and set the flag
        checkBiometricSupport()

        // Display biometric prompt if available
        if (isBiometricAvailable) {
            biometricPrompt.authenticate(promptInfo)
        }

        // Set the button listener for username/password login
        binding.button.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            // If biometric is not used or available, validate username and password
            if (username == "GYM" && password == "12321") {
                // Proceed to home screen if credentials are correct
                Toast.makeText(this, "Welcome! Proceeding to home screen.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, home::class.java))
            } else {
                // Incorrect credentials
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                isBiometricAvailable = true
                setupBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Biometric authentication is not available; fallback to username and password
                isBiometricAvailable = false
            }
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Biometric authentication succeeded, proceed to home screen
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, home::class.java))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle the case where biometric authentication is unavailable or cancelled
                    Toast.makeText(applicationContext, "Biometric authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        // Set up the prompt info
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate using fingerprint or face")
            .setDescription("You can authenticate with biometrics or use your username and password.")
            .setNegativeButtonText("Use username and password")
            .build()
    }
}
