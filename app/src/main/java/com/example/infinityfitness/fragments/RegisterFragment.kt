package com.example.infinityfitness.fragments

import android.Manifest
import android.Manifest.*
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentProviderOperation
import android.content.Context
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        binding.add.setOnClickListener {
            // List of place fields you want to fetch
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

            // Create an intent for Autocomplete activity
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        database = GymDatabase.getDatabase(this.requireContext())
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

                        database.withTransaction {
                            val getPack = database.packDao().getPackByType(selectedPack)
                            val enddate = dateformat.parse(endDate)
                            val customer = Customer(
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

                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_CONTACTS), 1)
                            }

                            saveContact(
                                "$custId | ${customer.name} | GYM",
                                customer.phoneNumber.toString(), customer.image
                            )
                            delay(3450)
                            sendBillToUser(
                                custId.toString(),
                                name,
                                address,
                                currentDate,
                                endDate,
                                selectedPack,
                                amount.toString(),
                                paymentMethod.toString(),
                                phoneNumber
                            )



                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterFragment.requireContext(), "Customer added successfully", Toast.LENGTH_SHORT).show()
                            setCurrentFragement(HomeFragement())
                        }



                    } catch (e: Exception) {
                        Log.e("Register","$e")
                        withContext(Dispatchers.Main) {
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
        private const val REQUEST_IMAGE_CAPTURE = 123
        private  val READ_EXTERNAL_STORAGE_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission.READ_MEDIA_IMAGES
        } else {
            permission.READ_EXTERNAL_STORAGE
        }
        private const val CAMERA_PERMISSION = permission.CAMERA
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

        val targetWidth = 450
        val targetHeight = 600

        // Create a new scaled bitmap with reduced resolution
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
    }

    private fun setCurrentFragement(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }



    fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        val formattedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=$formattedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
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
            val inputStream: InputStream = context?.getAssets()!!.open("Bill.html")
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
            val file = File(requireContext().getExternalFilesDir(null), fileName)


            val outputStream = FileOutputStream(file)
            outputStream.write(modifiedHtml.toByteArray(Charset.defaultCharset()))
            outputStream.close()

            // Step 4: Send the file via WhatsApp
            sendFileViaWhatsApp(file,phoneNumber)
            //openHtmlFile(requireContext(), file)

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Bill", "Error creating bill")
        }
    }

    private fun sendFileViaWhatsApp(file: File, phoneNumber: String) {
        // Get the URI using FileProvider
        val fileUri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireActivity().packageName}.provider", // Use the authority defined in AndroidManifest.xml
            file
        )


        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/html"

        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.setPackage("com.whatsapp")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intent.putExtra("jid", "91$phoneNumber@s.whatsapp.net")

        // Check if WhatsApp is installed
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
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




}
