package com.example.infinityfitness.fragments

import android.Manifest
import android.Manifest.*
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

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

        binding = RegisterBinding.bind(view)

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
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterFragment.requireContext(), "Customer added successfully", Toast.LENGTH_SHORT).show()
                            setCurrentFragement(HomeFragement())
                        }
                    } catch (e: Exception) {
                        Log.e("Register","$e")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterFragment.requireContext() , "$e There was an Error while adding" , Toast.LENGTH_SHORT).show()
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
        changeBackgroundOnText(view.findViewById<EditText>(R.id.add))
        changeBackgroundOnText(binding.age)
        changeBackgroundOnText(binding.name)
        changeBackgroundOnText(binding.date)
        changeBackgroundOnText(binding.amt)
        changeBackgroundOnText(binding.phno)





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
                    editText.setBackgroundResource(R.drawable.greenbutton)
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
        // Define the new width and height (reduce by 50%, for example)
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
        val formattedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=$formattedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

}
