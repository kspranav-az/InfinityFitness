package com.example.infinityfitness

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class DueCust : AppCompatActivity() , OnCustomerButtonClickListener{private lateinit var adapter: CustomerCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: GymDatabase

    private var customerList = mutableListOf<CustomerCard>()
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cust_due)

        recyclerView = findViewById(R.id.dueView)
        database = GymDatabase.getDatabase(this)
        adapter = CustomerCardAdapter(customerList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set up infinite scrolling
        setUpRecyclerViewScrollListener()

        // Initially load the first page
        loadCustomers()
    }

    private fun setUpRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isLoading) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (firstVisibleItemPosition + visibleItemCount >= totalItemCount && totalItemCount >= pageSize) {
                        loadCustomers() // Only load more customers when at the end
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCustomers() {
        isLoading = true

        lifecycleScope.launch {
            try {
                val customerDao = database.customerDao()

                // Fetch paginated customers (no search query)
                val newCustomers = customerDao.getActiveCustomersPaged(
                    "",
                    0,
                    0,
                    pageSize,
                    currentPage * pageSize
                )

                // Check if newCustomers is empty, which would mean no more data to load
                if (newCustomers.isNotEmpty()) {
                    val newCustomerCards = newCustomers.map { customer ->
                        CustomerCard(
                            customerName = customer.name,
                            customerId = customer.billNo.toString(),
                            dueDate = customer.activeTill.toString(),
                            imageResourceId = customer.image!!
                        )
                    }

                    // Append new customers to the list (do not clear the list)
                    customerList.addAll(newCustomerCards)

                    // Notify the adapter of changes
                    if (currentPage == 0) {
                        // First page, set adapter
                        adapter = CustomerCardAdapter(customerList, this@DueCust)
                        recyclerView.adapter = adapter
                    } else {
                        // Subsequent pages, just notify the adapter
                        adapter.notifyDataSetChanged()
                    }

                    // Increment the page number for the next fetch
                    currentPage++
                } else {
                    // No more data to load
                    isLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log any errors
                isLoading = false
            }
        }
    }

    override fun onButtonClick(customer: CustomerCard) {
        // Start CustDataActivity with the clicked customer data
        val intent = Intent(this, CustData::class.java).apply {
            putExtra("customerName", customer.customerName)
            putExtra("customerId", customer.customerId.toLongOrNull())
            putExtra("dueDate", customer.dueDate)
        }
        startActivity(intent)
    }
}