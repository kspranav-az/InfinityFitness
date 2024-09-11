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

        val save : Button = findViewById(R.id.save)
        val cancel: Button  = findViewById(R.id.cancel2)
        save.setOnClickListener{
            startActivity(
                Intent(
                    this,
                    home::class.java
                )
            )
        }
        cancel.setOnClickListener{
            startActivity(
                Intent(
                    this,
                    home::class.java
                )
            )
        }


        val spinner2: Spinner = findViewById(R.id.pack)
        ArrayAdapter.createFromResource(
            this,
            R.array.PACKAGE,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Specify the format you want
        val formattedDate = currentDate.format(formatter)

        println("Current Date: $formattedDate")

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                val dateEditText : EditText = view!!.findViewById(R.id.date)
                val amountEditText : EditText  = view.findViewById(R.id.amt)
                val selectedItem = parent.getItemAtPosition(position).toString()

                // Update EditText fields based on the selection
                when (selectedItem) {
                    "1 DAY" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("200")
                    }
                    "1 MONTH" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("1000")
                    }
                    "3 MONTH" -> {
                        dateEditText.setText(currentDate.plus(3, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("2000")
                    }
                    "4 MONTH" -> {
                        dateEditText.setText(currentDate.plus(4, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("3000")
                    }
                    "6 MONTH" -> {
                        dateEditText.setText(currentDate.plus(6, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("4000")
                    }
                    "1 Year" ->{
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.YEARS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
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
                TODO("Not yet implemented")
            }
        }




        val spinner: Spinner = findViewById(R.id.sex)
        ArrayAdapter.createFromResource(
            this,
            R.array.SEX,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val spinner1: Spinner = findViewById(R.id.mop)
        ArrayAdapter.createFromResource(
            this,
            R.array.MODE_OF_PAYMENT,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner1.adapter = adapter
        }

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
