package com.example.infinityfitness.fragments

import android.Manifest
import android.Manifest.*
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.infinityfitness.R
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.database.entity.Subscription
import com.example.infinityfitness.databinding.RegisterBinding
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream


class RegisterFragment : Fragment(R.layout.register) {

    // Declare your EditText for date selection
    private lateinit var dateEditText: EditText
    private lateinit var binding: RegisterBinding
    private lateinit var database: GymDatabase
    private lateinit var selectedImage : Bitmap
    private var imgselected : Boolean = false
    private lateinit var progressLayout: View
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize the Places API with the API key
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyAJ95y136G--MkKU8VymzB26UjlypFl4G4")
        }

        binding = RegisterBinding.bind(view)

        progressLayout = view.findViewById(R.id.progress_layout_r)
        progressBar = view.findViewById(R.id.progress_bar_r)

        binding.add.setOnClickListener {
            // List of place fields you want to fetch
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

            // Create an intent for Autocomplete activity
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        database = GymDatabase.getDatabase(this.requireContext())

        lifecycleScope.launch (Dispatchers.IO) {
            var bill = database.customerDao().getLastPrimaryKey()?.plus(1)

            if (bill != null) {
                updateBill(bill)
            } else {
                updateBill(1)
            }
        }

        // Handle edge-to-edge and window insets
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



//        binding.name.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                // No action needed after text has changed
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // No action needed before text changes
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Change the background color when editing starts
//                if (s.isNullOrEmpty()) {
//                    // Set background to default color if the text is empty
//                    binding.name.setBackgroundColor(Color.GREEN)
//                } else {
//                    // Set background to a different color when t
//                }
//            }
//        }




        binding.img.setOnClickListener {
            Log.d("ImageSelection", "Choosing image source")
            pickImage()
        }

        // Handle button click
        binding.regbtn.setOnClickListener {

            if (binding.name.text.toString() == "") {
                Toast.makeText(this@RegisterFragment.requireContext(), "Enter a valid name", Toast.LENGTH_SHORT).show()
            } else if (binding.add?.text.toString() == "") {
                Toast.makeText(this@RegisterFragment.requireContext(), "Enter a valid address", Toast.LENGTH_SHORT).show()
            } else if ((binding.age.text.toString().toIntOrNull() ?: 0) <= 0) {
                Toast.makeText(this@RegisterFragment.requireContext(), "Enter a valid age", Toast.LENGTH_SHORT).show()
            } else if (binding.phno.text.toString() == "" || binding.phno.text.toString().length < 10) {
                Toast.makeText(this@RegisterFragment.requireContext(), "Enter a valid phone number", Toast.LENGTH_SHORT).show()
            } else if (binding.sex.selectedItem.toString() == "SEX") {
                Toast.makeText(this@RegisterFragment.requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
            } else if (binding.pack.selectedItem.toString() == "PACK") {
                Toast.makeText(this@RegisterFragment.requireContext(), "Please select a pack", Toast.LENGTH_SHORT).show()
            } else if (binding.mop.selectedItem.toString() == "MODE OF PAYMENT") {
                Toast.makeText(this@RegisterFragment.requireContext(), "Please select a mode of payment", Toast.LENGTH_SHORT).show()
            } else if ((binding.amt.text.toString().toDoubleOrNull() ?: 0.0) <= 0) {
                Toast.makeText(this@RegisterFragment.requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
            } else if (!imgselected){
                Toast.makeText(this@RegisterFragment.requireContext(), "Select an image", Toast.LENGTH_SHORT).show()
            }
            else {
                val bill  = binding.billno.text.toString().toLongOrNull() ?: 0
                val name = binding.name.text.toString()
                val address = binding.add?.text.toString()
                val age = binding.age.text.toString().toIntOrNull() ?: 0
                val phoneNumber = binding.phno.text.toString()
                val amount = binding.amt.text.toString().toDoubleOrNull() ?: 0.0
                val endDate = binding.date.text.toString()
                val gender = SEX.valueOf(binding.sex.selectedItem.toString())
                val selectedPack = binding.pack.selectedItem.toString()
                val paymentMethod = PaymentMethod.valueOf(binding.mop.selectedItem.toString())

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val calendar = Calendar.getInstance()
                        val dateformat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val currentDate = dateformat.format(calendar.time)
                        var customer = Customer()
                        database.withTransaction {
                            val getPack = database.packDao().getPackByType(selectedPack)
                            val enddate = dateformat.parse(endDate)
                            customer = Customer(
                                billNo = bill,
                                name = name,
                                gender = gender,
                                age = age,
                                address = address,
                                phoneNumber = phoneNumber,
                                joiningDate = calendar.time,
                                isActive = true,
                                image = selectedImage,
                                lastPack = selectedPack,
                                activeTill = enddate,
                            )

                            val custId = database.customerDao().insertCustomer(customer)

                            val subscription = Subscription(
                                customerId = custId,
                                packId = getPack?.packId,
                                startDate = Calendar.getInstance().time,
                                endDate = enddate,
                                finalPrice = amount,
                                paymentMethod = paymentMethod
                            )

                            database.subscriptionDao().insertSubscription(subscription)

                            if (ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.WRITE_CONTACTS
                                )
                                != PackageManager.PERMISSION_GRANTED
                            ) {

                                ActivityCompat.requestPermissions(
                                    requireActivity(),
                                    arrayOf(Manifest.permission.WRITE_CONTACTS),
                                    1
                                )
                            }
                        }
                        lifecycleScope.launch(Dispatchers.Main){
                        Toast.makeText(this@RegisterFragment.requireContext(), "Customer added successfully", Toast.LENGTH_SHORT).show()
                        }
                            saveContact(
                                "${customer.billNo} | ${customer.name} | GYM",
                                customer.phoneNumber.toString(), customer.image
                            )

                            withContext(Dispatchers.Main) {

                                showProgressBar()

                            }
                            delay(4000)

                            sendWhatsAppMessage(
                                customer.phoneNumber.toString(),
                                "Thank you, ${customer.name}, for registering with InfinityFitness!"
                            )



                            delay(5000)

                            withContext(Dispatchers.Main) {

                                hideProgressBar()

                            }
                            val inflater: LayoutInflater = layoutInflater
                            val popupView: View = inflater.inflate(R.layout.popup, null)

                            // Create the PopupWindow
                            val popupWindow = PopupWindow(
                                popupView,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                true
                            )
                            popupWindow.setBackgroundDrawable(
                                ContextCompat.getDrawable(requireContext(), android.R.color.transparent)
                            )

                            // Show the popup window
                        lifecycleScope.launch(Dispatchers.Main) {
                            popupWindow.showAtLocation(view, android.view.Gravity.CENTER, 0, 0)
                        }
                            val yesb: Button = popupView.findViewById<Button>(R.id.yesbtn)
                            val nob: Button = popupView.findViewById<Button>(R.id.nobtn)
                            yesb.setOnClickListener {
                                // Close popup immediately
                                popupWindow.dismiss()
                                
                                // Start the bill preview process
                                showBillPreviewDialog(
                                    customer.billNo.toString(),
                                    name,
                                    address,
                                    currentDate,
                                    endDate,
                                    selectedPack,
                                    amount.toString(),
                                    paymentMethod.toString(),
                                    phoneNumber
                                )
                                
                                // Do NOT navigate away immediately. 
                                // The user will see Toasts for progress.
                                // You might want to delay navigation or let the user navigate manually.
                                
                                // OPTION: Navigate only after success? 
                                // For now, let's just NOT navigate so the coroutine scope doesn't die.
                                // If you NEED to navigate, you must use a scope tied to the Application or ViewModel, 
                                // not the Fragment's lifecycleScope which gets cancelled on navigation.
                            }
                            nob.setOnClickListener{
                                popupWindow.dismiss()
                                lifecycleScope.launch(Dispatchers.Main) {
                                    setCurrentFragement(HomeFragement())
                                }
                            }




                    } catch (e: Exception) {
                        Log.e("Register","$e")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@RegisterFragment.requireContext() ,
                                "$e There was an Error while adding" , Toast.LENGTH_SHORT).show()
                            setCurrentFragement(HomeFragement())
                        }
                    }
                }
            }
        }

        // Initialize Spinners and Adapters
        val spinner2: Spinner = binding.pack
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.PACKAGE,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }

        val currentDate = LocalDate.now()
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedItem = parent.getItemAtPosition(position).toString()
                val dateEditText : EditText = binding.date
                val amountEditText : EditText  = binding.amt
                // Update EditText fields based on the selection
                when (selectedItem) {
                    "1 Day" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("200")
                    }
                    "1 Month" -> {
                        dateEditText.setText(currentDate.plus(1, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("1000")
                    }
                    "3 Month" -> {
                        dateEditText.setText(currentDate.plus(3, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("2000")
                    }
                    "4 Month" -> {
                        dateEditText.setText(currentDate.plus(4, ChronoUnit.MONTHS).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        amountEditText.setText("3000")
                    }
                    "6 Month" -> {
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
                // Handle if nothing is selected, if necessary
            }
        }

        val spinner: Spinner = binding.sex
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.SEX,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val spinner1: Spinner = binding.mop
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.MODE_OF_PAYMENT,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner1.adapter = adapter
        }

        // Initialize the EditText for the date picker
        dateEditText = binding.date

        // Set an onClickListener to show the DatePickerDialog when clicked
        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }
        changeBackgroundOnText(binding.add)
        changeBackgroundOnText(binding.age)
        changeBackgroundOnText(binding.name)
        changeBackgroundOnText(binding.date)
        changeBackgroundOnText(binding.amt)
        changeBackgroundOnText(binding.phno)


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    binding.add.setText(place.address)  // Set the selected address to EditText
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // Handle error
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation
                }
            }
        }
    }

    private fun updateBill(number: Long){
        lifecycleScope.launch(Dispatchers.Main) {
            binding.billno.setText(number.toString())
        }
    }


    private fun showDatePickerDialog() {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create and show the DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the date and set it to the EditText
                val formattedDate = "${selectedDay}-${selectedMonth + 1}-${selectedYear}"
                dateEditText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()


    }

    companion object {
        internal const val AUTOCOMPLETE_REQUEST_CODE = 1
        const val REQUEST_IMAGE_CAPTURE = 123
        private  val READ_EXTERNAL_STORAGE_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission.READ_MEDIA_IMAGES
        } else {
            permission.READ_EXTERNAL_STORAGE
        }
        const val CAMERA_PERMISSION = permission.CAMERA
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                if (data.data != null) { // Image from gallery
                    val imageUri = data.data!!
                    binding.img.setImageURI(imageUri)
                    selectedImage = MediaStore.Images.Media
                        .getBitmap(
                            this.requireContext().contentResolver,
                            imageUri
                        )
                    selectedImage = reduceImageResolution(selectedImage)
                } else { // Image from camera
                    val imageBitmap = data.extras?.get("data") as Bitmap?
                    if (imageBitmap != null) {
                        binding.img.setImageBitmap(imageBitmap)
                        selectedImage = imageBitmap
                    }
                    selectedImage = reduceImageResolution(selectedImage)
                }

                imgselected = true
            }
        }
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


    // Call this method when you need to choose an image
    private fun pickImage() {
        // Check for permissions (READ and CAMERA)
        val permissions = mutableListOf(CAMERA_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this.requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.d("ImageSelection", "Started activity for permission")
            ActivityCompat.requestPermissions(this.requireActivity(), missingPermissions.toTypedArray(), REQUEST_IMAGE_CAPTURE)
        } else {
            Log.d("ImageSelection", "Started activity for result")
            chooseImageSource()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                chooseImageSource()
            } else {
                // Handle permission denial
                Log.d("Permissions", "Permission denied")
            }
        }
    }

    private fun chooseImageSource() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val chooser = Intent.createChooser(galleryIntent, "Select Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        }

        getImage.launch(chooser)
    }

    private fun calculateEndDate(pack: String, startDate: String): String {

    return startDate

}

    // Function to reduce image resolution
    private fun reduceImageResolution(originalBitmap: Bitmap): Bitmap {

        val targetWidth = 300
        val targetHeight = 400

        // Create a new scaled bitmap with reduced resolution
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
    }

    private fun setCurrentFragement(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }



    fun sendWhatsAppMessage(phoneNumber: String, message: String) {
       // NOTE: Initialize with context. Credentials should ideally come from a secure source.
       // Ideally initialized in onViewCreated or via DI
       val reportService = com.example.infinityfitness.services.WhatsAppReportService(requireContext())
       
       lifecycleScope.launch {
           try {
               // Toast.makeText(requireContext(), "Sending Message...", Toast.LENGTH_SHORT).show()
               reportService.sendText("91$phoneNumber", message)
               // Toast.makeText(requireContext(), "Message sent!", Toast.LENGTH_SHORT).show()
           } catch (e: Exception) {
               Log.e("WhatsApp", "Failed to send message: ${e.message}")
               e.printStackTrace()
           }
       }
    }

    private fun showBillPreviewDialog(
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
            // Step 1: Load and Prepare HTML
            val inputStream: InputStream = context?.getAssets()!!.open("Bill.html")
            val htmlTemplate = inputStream.bufferedReader().use { it.readText() }
            val modifiedHtml = htmlTemplate
                .replace("#123456", billno)
                .replace("John Doe", userName)
                .replace("123 Street Name, City, State, 632006", userAddress)
                .replace("01-Oct-2024", joiningDate)
                .replace("01-Oct-2025", expiryDate)
                .replace("Annual - Full Access", packageName)
                .replace("₹ 12,000", amountPaid)
                .replace("Credit Card", paymentMode)

            // Step 2: Show Dialog with WebView
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.bill_preview_dialog) // We need to create this layout
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            
            val webView = dialog.findViewById<android.webkit.WebView>(R.id.previewWebView)
            val btnSend = dialog.findViewById<Button>(R.id.btnSendBill)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancelBill)
            
            webView.settings.javaScriptEnabled = false
            webView.loadDataWithBaseURL("file:///android_asset/", modifiedHtml, "text/html", "UTF-8", null)

            btnCancel.setOnClickListener { dialog.dismiss() }

            btnSend.setOnClickListener {
                Toast.makeText(requireContext(), "Generating PDF...", Toast.LENGTH_SHORT).show()
                
                val fileName = "Invoice_$billno.pdf"
                val pdfFile = File(requireContext().getExternalFilesDir(null), fileName)
                val pdfService = com.example.infinityfitness.services.PdfService(requireContext())
                
                // capture the ALREADY RENDERING WebView
                pdfService.createPdfFromWebView(webView, pdfFile, 
                    onComplete = { file ->
                        dialog.dismiss()
                        sendPdfToWhatsApp(phoneNumber, file, fileName, billno)
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            dialog.show()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Bill", "Error showing preview: ${e.message}")
        }
    }
    
    private fun sendPdfToWhatsApp(phoneNumber: String, file: File, fileName: String, billNo: String) {
         val reportService = com.example.infinityfitness.services.WhatsAppReportService(requireContext())
         Toast.makeText(requireContext(), "Sending Bill...", Toast.LENGTH_SHORT).show()
         
         lifecycleScope.launch {
            reportService.sendPdf(
                phoneNumber = "91$phoneNumber", 
                pdfFile = file,
                billFileName = fileName,
                caption = "Here is your invoice #$billNo",
                onProgress = { status -> Log.d("WhatsApp", status) },
                onError = { error -> 
                    Log.e("WhatsApp", error)
                    Toast.makeText(requireContext(), "Failed: $error", Toast.LENGTH_LONG).show()
                },
                onSuccess = {
                    Toast.makeText(requireContext(), "Bill sent successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate Home
                    lifecycleScope.launch(Dispatchers.Main) {
                        setCurrentFragement(HomeFragement())
                    }
                }
            )
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

    private suspend fun saveContact(contactName: String, phoneNumber: String, contactImage: Bitmap?) {
        val ops = ArrayList<ContentProviderOperation>()

        // Add the contact's display name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Add the contact's name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                .build()
        )

        // Add the contact's phone number
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        )

        // Add the contact's photo (if provided)
        contactImage?.let {
            val imageBytes = bitmapToByteArray(it)
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                    .build()
            )
        }

        try {
            // Execute the operations
            requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

            // Notify system to refresh contacts
            notifyContactChanged()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Contact saved successfully with image!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Failed to save contact.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun notifyContactChanged() {
        // Send a broadcast intent to notify the system that contacts were changed
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED)
        intent.data = ContactsContract.Contacts.CONTENT_URI
        requireContext().sendBroadcast(intent)
    }


    // Helper function to convert a Bitmap to byte array
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun showProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressLayout.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressLayout.visibility = View.GONE
        }
    }




}
