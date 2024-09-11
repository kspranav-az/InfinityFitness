package com.example.infinityfitness

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText

class EditData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_data)

        // Apply edge-to-edge mode for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editData)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the EditText for the date input
        val dateEdt: EditText = findViewById(R.id.eddate)

        // Set click listener for the EditText
        dateEdt.setOnClickListener {
            // Get the instance of Calendar
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    // Set date to the EditText in the format "day-month-year"
                    val formattedDate = "$selectedDayOfMonth-${selectedMonth + 1}-$selectedYear"
                    dateEdt.setText(formattedDate)
                },
                year,
                month,
                day
            )
            // Show the DatePickerDialog
            datePickerDialog.show()
        }
    }
}
