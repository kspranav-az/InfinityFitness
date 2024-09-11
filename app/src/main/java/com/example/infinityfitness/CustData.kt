package com.example.infinityfitness

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class CustData : AppCompatActivity() {
    // Declare all the views
    private lateinit var extendButton: Button
    private lateinit var cancelButton: Button
    private lateinit var vimg: ImageView
    private lateinit var vname: TextView
    private lateinit var vadd: TextView
    private lateinit var vage: TextView
    private lateinit var vphno: TextView
    private lateinit var vamt: TextView
    private lateinit var vdate: TextView
    private lateinit var vpack: TextView
    private lateinit var vsex: TextView
    private lateinit var vmop: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cust_data)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.custData)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize all the views using findViewById
        extendButton = findViewById(R.id.extend)
        cancelButton = findViewById(R.id.cancel)
        vimg = findViewById(R.id.vimg)
        vname = findViewById(R.id.vname)
        vadd = findViewById(R.id.vadd)
        vage = findViewById(R.id.vage)
        vphno = findViewById(R.id.vphno)
        vamt = findViewById(R.id.vamt)
        vdate = findViewById(R.id.vdate)
        vpack = findViewById(R.id.vpack)
        vsex = findViewById(R.id.vsex)
        vmop = findViewById(R.id.vmop)

        // Set listeners for buttons
        extendButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    EditData::class.java
                )
            )
        }

        cancelButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    home::class.java
                )
            )
        }

        populateViews()
    }

    private fun populateViews() {
        // Example data to populate the views
        vname.text = "John Doe"
        vadd.text = "123 Main Street, Springfield"
        vage.text = "28"
        vphno.text = "+1234567890"
        vamt.text = "$100.00"
        vdate.text = "2024-09-11"
        vpack.text = "Premium Package"
        vsex.text = "Male"
        vmop.text = "Credit Card"

        // Set image for ImageView
        vimg.setImageResource(R.drawable.uploadimage) // Replace with your actual image resource
    }
}
