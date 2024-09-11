package com.example.infinityfitness

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.infinityfitness.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            // Check if the username and password match the required values
            if (username == "GYM" && password == "12321") {
                // Correct credentials, proceed to the home screen

                Toast.makeText(this, "Welcome lets train", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, home::class.java))
            } else {
                // Incorrect credentials, show a toast message
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
