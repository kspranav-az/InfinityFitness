package com.example.infinityfitness

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.Adpater.CustomerCard
import com.example.infinityfitness.Adpater.CustomerCardAdapter
import com.example.infinityfitness.Adpater.OnCustomerButtonClickListener
import com.example.infinityfitness.R
import com.example.infinityfitness.database.GymDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class DueCust : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerCardAdapter
    private lateinit var database: GymDatabase
    private val customerList = mutableListOf<CustomerCard>()

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cust_due)

        recyclerView = findViewById(R.id.dueView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter with an empty listener as no click handling is needed
        adapter = CustomerCardAdapter(customerList, object : OnCustomerButtonClickListener {
            override fun onButtonClick(customer: CustomerCard) {
                // No action required, you can leave it empty or log if needed
            }
        })

        recyclerView.adapter = adapter
        database = GymDatabase.getDatabase(this)

        loadDueCustomers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadDueCustomers() {
        isLoading = true

        lifecycleScope.launch {
            try {
                val customerDao = database.customerDao()

                // Calculate the due date as 2 days later using ChronoUnit
                val dueDate = LocalDate.now().plus(2, ChronoUnit.DAYS)
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Format to match database

                // Fetch customers whose due date is 2 days later
                val dueCustomers = customerDao.getDueCustomers(formatter.toString())

                // Map due customers to customer cards
                val dueCustomerCards = dueCustomers?.map { customer ->
                    CustomerCard(
                        customerName = customer.name,
                        customerId = customer.billNo.toString(),
                        dueDate = customer.activeTill.toString(),
                        imageResourceId = customer.image!! // Use a default image if null
                    )
                }

                // Clear the existing list and add new due customers
                customerList.clear() // Clear existing list for fresh data
                if (dueCustomerCards != null) {
                    customerList.addAll(dueCustomerCards)
                }

                // Notify the adapter of changes
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace() // Log any errors
            } finally {
                isLoading = false
            }
        }
    }
}