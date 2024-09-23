package com.example.infinityfitness.fragments

import android.app.DatePickerDialog
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
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
import com.example.infinityfitness.R
import java.util.*
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.database.entity.Subscription
import com.example.infinityfitness.databinding.RegisterBinding
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RegisterFragment : Fragment(R.layout.register) {

    // Declare your EditText for date selection
    private lateinit var dateEditText: EditText
    private lateinit var binding: RegisterBinding
    private lateinit var database: GymDatabase
    private lateinit var selectedImage : Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = GymDatabase.getDatabase(this.requireContext())
        // Handle edge-to-edge and window insets
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = RegisterBinding.bind(view)

        binding.img.setOnClickListener {
            Log.d("ImageSelection", "Choosing image source")
            pickImage()
        }

        // Handle button click
        binding.regbtn.setOnClickListener {
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
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterFragment.requireContext(), "Customer added successfully", Toast.LENGTH_SHORT).show()

                    }
                } catch (e: Exception) {
                    Log.e("Register","$e")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterFragment.requireContext() , "$e There was an Error while adding" , Toast.LENGTH_SHORT).show()
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
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
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
                } else { // Image from camera
                    val imageBitmap = data.extras?.get("data") as Bitmap?
                    if (imageBitmap != null) {
                        binding.img.setImageBitmap(imageBitmap)
                        selectedImage = imageBitmap
                    }
                }
            }
        }
    }



    // Call this method when you need to choose an image
    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), READ_EXTERNAL_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this.requireContext(), CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ImageSelection", "Started activity for permission")
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(READ_EXTERNAL_STORAGE_PERMISSION, CAMERA_PERMISSION), REQUEST_IMAGE_CAPTURE)
        } else {
            Log.d("ImageSelection", "Started activity for result")
            chooseImageSource()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageSource()
            } else {
                // Handle permission denial
            }
        }
    }

    private fun chooseImageSource() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // Or MediaStore.ACTION_IMAGE_CAPTURE_SECURE

        val chooser = Intent.createChooser(galleryIntent, "Select Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        }

        getImage.launch(chooser)
    }

private fun calculateEndDate(pack: String, startDate: String): String {

    return startDate

}



    private fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        val formattedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=$formattedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }




}
