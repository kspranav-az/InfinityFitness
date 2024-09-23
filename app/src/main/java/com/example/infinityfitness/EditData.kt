package com.example.infinityfitness

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.temporal.ChronoUnit

class EditData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_data)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editData)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get data from the intent
        val name = intent.getStringExtra("name")
        val address = intent.getStringExtra("address")
        val age = intent.getStringExtra("age")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val activeTill = intent.getStringExtra("activeTill")
        val packageType = intent.getStringExtra("package")
        val gender = intent.getStringExtra("gender")
        val mop = intent.getStringExtra("mop")

        // Set initial data in the EditText and Spinners
        findViewById<EditText>(R.id.edname).setText(name)
        findViewById<EditText>(R.id.edadd).setText(address)
        findViewById<EditText>(R.id.edage).setText(age)
        findViewById<EditText>(R.id.edphno).setText(phoneNumber)
        findViewById<EditText>(R.id.eddate).setText(activeTill)

        // Populate Spinner for Package
        val spinner2: Spinner = findViewById(R.id.pack)
        ArrayAdapter.createFromResource(
            this,
            R.array.PACKAGE,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }

        // Set initial selection for Package Spinner based on the intent data
        val packageIndex = resources.getStringArray(R.array.PACKAGE).indexOf(packageType)
        if (packageIndex >= 0) {
            spinner2.setSelection(packageIndex)
        }

        // Populate Spinner for Gender
        val spinner: Spinner = findViewById(R.id.sex)
        ArrayAdapter.createFromResource(
            this,
            R.array.SEX,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Set initial selection for Gender Spinner
        val genderIndex = resources.getStringArray(R.array.SEX).indexOf(gender)
        if (genderIndex >= 0) {
            spinner.setSelection(genderIndex)
        }

        // Populate Spinner for Mode of Payment
        val spinner1: Spinner = findViewById(R.id.mop)
        ArrayAdapter.createFromResource(
            this,
            R.array.MODE_OF_PAYMENT,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner1.adapter = adapter
        }

        // Set initial selection for MOP Spinner
        val mopIndex = resources.getStringArray(R.array.MODE_OF_PAYMENT).indexOf(mop)
        if (mopIndex >= 0) {
            spinner1.setSelection(mopIndex)
        }

        // Save and Cancel buttons
        val save: Button = findViewById(R.id.save)
        val cancel: Button = findViewById(R.id.cancel2)

        save.setOnClickListener {
            // Save logic here (if needed)
            startActivity(
                Intent(
                    this,
                    home::class.java
                )
            )
        }

        cancel.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    home::class.java
                )
            )
        }

        // Date selection logic for package
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formattedDate = currentDate.format(formatter)

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Use findViewById() to access the EditText from the activity layout, not the spinner row.
                val dateEditText: EditText = findViewById(R.id.eddate)  // Correct ID of your date EditText
                val amountEditText: EditText = findViewById(R.id.edamnt)   // Correct ID of your amount EditText
                val selectedItem = parent.getItemAtPosition(position).toString()

                // Update EditText fields based on the selection
                when (selectedItem) {
                    "1 Day" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.DAYS).format(formatter))
                        amountEditText.setText("200")
                    }
                    "1 Month" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.MONTHS).format(formatter))
                        amountEditText.setText("1000")
                    }
                    "3 Month" -> {
                        dateEditText.setText(currentDate.plus(3, ChronoUnit.MONTHS).format(formatter))
                        amountEditText.setText("2000")
                    }
                    "4 Month" -> {
                        dateEditText.setText(currentDate.plus(4, ChronoUnit.MONTHS).format(formatter))
                        amountEditText.setText("3000")
                    }
                    "6 Month" -> {
                        dateEditText.setText(currentDate.plus(6, ChronoUnit.MONTHS).format(formatter))
                        amountEditText.setText("4000")
                    }
                    "1 Year" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.YEARS).format(formatter))
                        amountEditText.setText("8000")
                    }
                    else -> {
                        // Handle default or other cases
                        dateEditText.setText("DATE")
                        amountEditText.setText("")
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Handle nothing selected case if needed
            }


    }

        // Set up DatePickerDialog for manual date selection
        val dateEdt: EditText = findViewById(R.id.eddate)
        dateEdt.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val formattedDate = "$selectedDayOfMonth-${selectedMonth + 1}-$selectedYear"
                    dateEdt.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }
}