package com.example.infinityfitness

import android.annotation.SuppressLint
import android.app.Activity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.infinityfitness.databinding.EditDataBinding
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.database.entity.Subscription
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
import com.example.infinityfitness.fragments.HomeFragement
import com.example.infinityfitness.fragments.RegisterFragment.Companion.AUTOCOMPLETE_REQUEST_CODE
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class EditData : AppCompatActivity() {
    private lateinit var database: GymDatabase
    private lateinit var binding: EditDataBinding
    private  var customer: Customer? = null

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_data)
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyAJ95y136G--MkKU8VymzB26UjlypFl4G4")
        }
        database = GymDatabase.getDatabase(this@EditData)
        binding = EditDataBinding.inflate(layoutInflater)

        binding.edadd.setOnClickListener {
            // List of place fields you want to fetch
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

            // Create an intent for Autocomplete activity
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editData)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val customerId = intent.getLongExtra("customerId",0)

        var  job  : Job = CoroutineScope(Dispatchers.IO).launch {
            customer = customerId?.let { getCustomerByBillNo(it) }!!
        }

        // Populate Spinner for Package
        val spinner2: Spinner = findViewById(R.id.edpack)
        ArrayAdapter.createFromResource(
            this,
            R.array.PACKAGE,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }



        // Populate Spinner for Gender
        val spinner: Spinner = findViewById(R.id.edsex)
        ArrayAdapter.createFromResource(
            this,
            R.array.SEX,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }


        // Populate Spinner for Mode of Payment
        val spinner1: Spinner = findViewById(R.id.edmop)
        ArrayAdapter.createFromResource(
            this,
            R.array.MODE_OF_PAYMENT,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner1.adapter = adapter
        }



        lifecycleScope.launch(Dispatchers.IO) {
            job.join()
            // Populate views with data
            withContext(Dispatchers.Main) {
                // Set initial data in the EditText and Spinners
                findViewById<EditText>(R.id.edname).setText(customer!!.name)
                findViewById<EditText>(R.id.edadd).setText(customer!!.address)
                findViewById<EditText>(R.id.edage).setText(customer?.age.toString())
                findViewById<EditText>(R.id.edphno).setText(customer!!.phoneNumber)
                findViewById<ImageButton>(R.id.edimg).setImageBitmap(customer!!.image)

                // Set initial selection for Package Spinner based on the intent data
                val packageIndex = resources.getStringArray(R.array.PACKAGE).indexOf(customer!!.lastPack)
                if (packageIndex >= 0) {
                    spinner2.setSelection(packageIndex)
                }

                println(customer!!.gender.toString())
                // Set initial selection for Gender Spinner
                val genderIndex = resources.getStringArray(R.array.SEX).indexOf(customer!!.gender.toString())
                if (genderIndex >= 0) {
                    spinner.setSelection(genderIndex)
                }


            }
        }

        // Save and Cancel buttons
        val save: Button = findViewById(R.id.save)
        val cancel: Button = findViewById(R.id.cancel2)

        save.setOnClickListener {
            if (findViewById<EditText>(R.id.edname).text.toString() == "") {
                Toast.makeText(this,"Enter a valid name", Toast.LENGTH_SHORT).show()
            } else if (findViewById<EditText>(R.id.edadd).text.toString() == "") {
                Toast.makeText(this, "Enter a valid address", Toast.LENGTH_SHORT).show()
            } else if ((findViewById<EditText>(R.id.edage).text.toString().toIntOrNull() ?: 0) <= 0) {
                Toast.makeText(this, "Enter a valid age", Toast.LENGTH_SHORT).show()
            } else if (findViewById<EditText>(R.id.edphno).text.toString() == "" || findViewById<EditText>(R.id.edphno).text.toString().length < 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
            } else if (findViewById<Spinner>(R.id.edsex).selectedItem.toString() == "SEX") {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            } else if (findViewById<Spinner>(R.id.edpack).selectedItem.toString() == "PACK") {
                Toast.makeText(this, "Please select a pack", Toast.LENGTH_SHORT).show()
            } else if (findViewById<Spinner>(R.id.edmop).selectedItem.toString() == "MODE OF PAYMENT") {
                Toast.makeText(this, "Please select a mode of payment", Toast.LENGTH_SHORT).show()
            } else if ((findViewById<EditText>(R.id.edamnt).text.toString().toDoubleOrNull() ?: 0.0) <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }else {

                lifecycleScope.launch(Dispatchers.IO) {
                    try {

                        val dateformat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val name = findViewById<EditText>(R.id.edname).text.toString()
                        val address = findViewById<EditText>(R.id.edadd).text.toString()
                        val age =
                            findViewById<EditText>(R.id.edage).text.toString().toIntOrNull() ?: 0
                        val phoneNumber = findViewById<EditText>(R.id.edphno).text.toString()
                        val amount =
                            findViewById<EditText>(R.id.edamnt).text.toString().toDoubleOrNull()
                                ?: 0.0
                        val endDate = findViewById<EditText>(R.id.eddate).text.toString()
                        val gender = findViewById<Spinner>(R.id.edsex).selectedItem.toString()
                        val selectedPack =
                            findViewById<Spinner>(R.id.edpack).selectedItem.toString()
                        val paymentMethod =
                            findViewById<Spinner>(R.id.edmop).selectedItem.toString()
                        println(endDate)
                        database.withTransaction {
                            val getPack = database.packDao().getPackByType(selectedPack)
                            val enddate = dateformat.parse(endDate)

                            val customer = Customer(
                                name = name,
                                gender = SEX.valueOf(gender),
                                age = age,
                                address = address,
                                phoneNumber = phoneNumber,
                                isActive = true,
                                image = customer!!.image,
                                lastPack = selectedPack,
                                activeTill = enddate,
                                billNo = customerId
                            )

                            println(customer)
                            database.customerDao().updateCustomer(customer)
                            customer.billNo = intent.getLongExtra("customerId", 0)
                            val currentDate = dateformat.format(java.util.Calendar.getInstance().time)

                            val subscription = Subscription(
                                customerId = customer.billNo,
                                packId = getPack?.packId,
                                startDate = java.util.Calendar.getInstance().time,
                                endDate = enddate,
                                finalPrice = amount,
                                paymentMethod = PaymentMethod.valueOf(paymentMethod)
                            )

                            database.subscriptionDao().insertSubscription(subscription)

                            val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)


                                val inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                val popupView: View = inflater.inflate(R.layout.popup, null)

                                // Create the PopupWindow
                                val popupWindow = PopupWindow(
                                    popupView,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    true
                                )
                                withContext(Dispatchers.Main) {

                                    // Set the background for the popup
                                    popupWindow.setBackgroundDrawable(
                                        ContextCompat.getDrawable(
                                            this@EditData,
                                            android.R.color.transparent
                                        )
                                    )

                                    // Show the popup window
                                    popupWindow.showAtLocation(
                                        save,
                                        android.view.Gravity.CENTER,
                                        0,
                                        0
                                    )
                                }
                                val yesb: Button = popupView.findViewById<Button>(R.id.yesbtn)
                                val nob: Button = popupView.findViewById<Button>(R.id.nobtn)

                                yesb.setOnClickListener {
                                    customer.phoneNumber?.let { it1 ->
                                        sendBillToUser(
                                            customer.billNo.toString(),
                                            name,
                                            address,
                                            LocalDateTime.parse(
                                                customer.joiningDate.toString(),
                                                formatter
                                            ).toLocalDate().toString(),
                                            LocalDateTime.parse(enddate.toString(), formatter)
                                                .toLocalDate().toString(),
                                            selectedPack,
                                            amount.toString(),
                                            paymentMethod,
                                            it1
                                        )
                                    }
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        popupWindow.dismiss()
                                        startActivity(
                                            Intent(
                                                this@EditData,
                                                home::class.java
                                            )
                                        )
                                    }
                                }
                                nob.setOnClickListener{
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        popupWindow.dismiss()
                                    }
                                }


                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@EditData,
                                "Customer updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    } catch (e: Exception) {
                        Log.e("Register", "$e")
                        throw e
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@EditData , "$e There was an Error while adding" , Toast.LENGTH_SHORT).show()
//                        //setCurrentFragement(HomeFragement())
//                    }
                    }
                }

            }
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
        changeBackgroundOnText(findViewById<EditText>(R.id.edadd))
        changeBackgroundOnText(binding.edage)
        changeBackgroundOnText(binding.edname)
        changeBackgroundOnText(binding.eddate)
        changeBackgroundOnText(binding.edamnt)
        changeBackgroundOnText(binding.edphno)
    }


    private fun changeBackgroundOnText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    // Set the background when text is entered
                    editText.setBackgroundResource(R.drawable.text_box_green)
                } else {
                    // Revert to the original background when no text
                    editText.setBackgroundResource(R.drawable.text_box)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }
        })
    }

    // Function to retrieve customer by bill number from the database
    private suspend fun getCustomerByBillNo(customerId: Long): Customer? {
        return database.customerDao().getCustomerByBillNo(customerId) // Adjust this line to call the DAO method
    }

    private fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        val formattedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=$formattedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun updateBill(number: Number){
        binding.edbillno.setText(number.toString())
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    binding.edadd.setText(place.address)  // Set the selected address to EditText
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // Handle error
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation
                }
            }
        }
}

    private fun sendBillToUser(
        billno : String,
        userName: String,
        userAddress: String,
        joiningDate: String,
        expiryDate: String,
        packageName: String,
        amountPaid: String,
        paymentMode: String,
        phoneNumber: String
    ) {
        try {
            // Step 1: Load the HTML template from assets
            val inputStream: InputStream = this?.getAssets()!!.open("Bill.html")
            val htmlTemplate = inputStream.bufferedReader().use { it.readText() }

            // Step 2: Replace placeholders with actual data
            val modifiedHtml = htmlTemplate
                .replace("#123456", billno)
                .replace("John Doe", userName)
                .replace("123 Street Name, City, State, 632006", userAddress)
                .replace("01-Oct-2024", joiningDate)
                .replace("01-Oct-2025", expiryDate)
                .replace("Annual - Full Access", packageName)
                .replace("₹ 12,000", amountPaid)
                .replace("Credit Card", paymentMode)

            // Step 3: Save the modified HTML to a file
            val fileName = "user_bill_${System.currentTimeMillis()}.html"
            val file = File(this.getExternalFilesDir(null), fileName)


            val outputStream = FileOutputStream(file)
            outputStream.write(modifiedHtml.toByteArray(Charset.defaultCharset()))
            outputStream.close()

            // Step 4: Send the file via WhatsApp
            sendFileViaWhatsApp(file,phoneNumber)
            //openHtmlFile(this, file)

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Bill", "Error creating bill")
        }
    }

    private fun sendFileViaWhatsApp(file: File , phoneNumber: String) {
        // Get the URI using FileProvider
        val fileUri: Uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.provider", // Use the authority defined in AndroidManifest.xml
            file
        )


        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/html"

        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.setPackage("com.whatsapp.w4b")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intent.putExtra("jid", "91$phoneNumber@s.whatsapp.net")
        intent.putExtra(Intent.EXTRA_TEXT,"Bill")

        // Check if WhatsApp is installed
        if (intent.resolveActivity(this.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(intent)
            Log.e("WhatsApp", "WhatsApp not installed")
        }
    }

    private fun openHtmlFile(context: Context, file: File) {
        // Create an Intent to open the HTML file in a web browser
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "text/html")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Check if any app can handle the intent (browser, for example)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Log.e("HTML", "No app available to open the file")
        }
    }
}