package com.example.infinityfitness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.infinityfitness.EditData
import com.example.infinityfitness.R
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.home
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

        val customerId = intent.getStringExtra("customerId")?.toIntOrNull()

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

        // Set listeners for buttons
        extendButton.setOnClickListener {
            val intent = Intent(this, EditData::class.java)
            intent.putExtra("name", vname.text.toString())
            intent.putExtra("address", vadd.text.toString())
            intent.putExtra("age", vage.text.toString())
            intent.putExtra("phoneNumber", vphno.text.toString())
            intent.putExtra("activeTill", vdate.text.toString())
            intent.putExtra("package", vpack.text.toString())
            intent.putExtra("gender", vsex.text.toString())
            intent.putExtra("mop", vmop.text.toString())
            // Assuming you're passing an image path or other identification for image
            // intent.putExtra("image", "your_image_reference")
            startActivity(intent)
        }


        cancelButton.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        // Populate views with data
        if (customerId != null) {
            populateViews(customerId)
        } else {
            Toast.makeText(this, "Customer ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateViews(customerId: Int) {
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
    private suspend fun getCustomerByBillNo(customerId: Int): Customer? {
        return database.customerDao().getCustomerByBillNo(customerId) // Adjust this line to call the DAO method
    }
}
