package com.example.infinityfitness

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.infinityfitness.EditData
import com.example.infinityfitness.R
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.home
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    private lateinit var customer: Customer

    private lateinit var database: GymDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cust_data)

        database = GymDatabase.getDatabase(this) // Get the database instance



        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.custData)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val customerId : Long? = intent.getLongExtra("customerId",0)
        var  job  : Job = CoroutineScope(Dispatchers.IO).launch {
            customer = customerId?.let { getCustomerByBillNo(it) }!!
        }

        // Initialize all the views using findViewById
        extendButton = findViewById(R.id.extend)
        cancelButton = findViewById(R.id.cancel)
        vimg = findViewById(R.id.vimg)
        vname = findViewById(R.id.vname)
        vadd = findViewById(R.id.vadd)
        vage = findViewById(R.id.vage)
        vphno = findViewById(R.id.vphno)
        vdate = findViewById(R.id.vdate)
        vpack = findViewById(R.id.vpack)
        vsex = findViewById(R.id.vsex)
        vmop = findViewById(R.id.vmop)


        vimg.setOnClickListener{
            showImageInDialog(vimg)
        }

        // Set listeners for buttons
        extendButton.setOnClickListener {
            if (customerId != null) {
            val intent = Intent(this, EditData::class.java)
            intent.putExtra("customerId", customerId)
//            intent.putExtra("name", customer.name)
//            intent.putExtra("address", customer.address)
//            intent.putExtra("age", customer.age)
//            intent.putExtra("phoneNumber", customer.phoneNumber)
//            intent.putExtra("activeTill", customer.activeTill)
//            intent.putExtra("package", customer.lastPack )
//            intent.putExtra("gender", customer.gender.toString() )
//            intent.putExtra("mop", customer.paymentMethod.toString() )
//            intent.putExtra("image",vimg.drawable.toBitmap())
            // Assuming you're passing an image path or other identification for image
            // intent.putExtra("image", "your_image_reference")
            startActivity(intent)

            } else {
                Toast.makeText(this@CustData, "Customer ID is missing", Toast.LENGTH_SHORT).show()
            }
        }


        cancelButton.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        lifecycleScope.launch(Dispatchers.IO) {
            job.join()
            // Populate views with data
            if (customerId != null) {
                populateViews(customerId)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CustData, "Customer ID is missing", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun showImageInDialog(CustImg: ImageView) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image)  // custom layout for enlarged image

        // Find the ImageView inside the dialog
        val enlargedImageView = dialog.findViewById<ImageView>(R.id.enlargedImageView)

        // Set the clicked image to the enlarged ImageView
        enlargedImageView.setImageBitmap(CustImg.drawable.toBitmap())

        // Show the dialog
        dialog.show()
    }


    private fun populateViews(customerId: Long) {
        // Launch a coroutine to fetch data from the database in the background
        CoroutineScope(Dispatchers.IO).launch {
            val customer = getCustomerByBillNo(customerId) // Fetch customer data from database

            withContext(Dispatchers.Main) {
                if (customer != null) {
                    // Populate the views with customer data
                    vname.text = customer.name
                    vadd.text = customer.address
                    vage.text = customer.age.toString()
                    vphno.text = customer.phoneNumber
                    vdate.text = customer.activeTill.toString()
                    vpack.text = customer.lastPack
                    vsex.text = customer.gender.toString()
                    vimg.setImageBitmap(customer.image)
                } else {
                    Toast.makeText(this@CustData, "Customer not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to retrieve customer by bill number from the database
    private suspend fun getCustomerByBillNo(customerId: Long): Customer? {
        return database.customerDao().getCustomerByBillNo(customerId) // Adjust this line to call the DAO method
    }
}
