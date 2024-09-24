package com.example.infinityfitness

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.Adpater.CustomerCard
import com.example.infinityfitness.Adpater.CustomerCardAdapter
import com.example.infinityfitness.Adpater.OnCustomerButtonClickListener
import com.example.infinityfitness.database.GymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DueCust : AppCompatActivity(), OnCustomerButtonClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerCardAdapter
    private lateinit var database: GymDatabase
    private var customerList = mutableListOf<CustomerCard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cust_due) // Use the layout that contains your RecyclerView

        database = GymDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.dueView)

        // Initialize the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CustomerCardAdapter(customerList, this)
        recyclerView.adapter = adapter

        // Load due customers
        loadDueCustomers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadDueCustomers() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val customerDao = database.customerDao()
                val dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 2) // Change to 2 days as per your requirement
                }

                // Fetch customers whose due date is within the next 2 days
                val dueCustomers = customerDao.getDueCustomers(dueDate.time)

                // Map due customers to customer cards
                val dueCustomerCards = dueCustomers?.map { customer ->
                    CustomerCard(
                        customerName = customer.name,
                        customerId = customer.billNo.toString(),
                        dueDate = customer.activeTill.date.toString(),
                        imageResourceId = customer.image!! // Use a default image if null
                    )
                } ?: emptyList() // If null, provide an empty list

                // Clear the existing list and add new due customers
                customerList.clear()
                customerList.addAll(dueCustomerCards)

                // Notify the adapter of changes on the main thread
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log any errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DueCust, "Failed to load due customers.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handle button click in each item
    override fun onButtonClick(customer: CustomerCard) {
        Toast.makeText(this, "Clicked: ${customer.customerName}", Toast.LENGTH_SHORT).show()
        // Handle navigation or any other action here
    }

    // Sample method to get a Bitmap for testing (you can load from resources or files)
    private fun getSampleBitmap(): Bitmap {
        // Create a simple bitmap or use any image loading library like Glide or Picasso
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }
}
