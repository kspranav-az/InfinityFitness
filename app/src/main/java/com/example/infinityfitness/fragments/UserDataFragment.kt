package com.example.infinityfitness.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R
import com.example.infinityfitness.Adpater.CustomerCard
import com.example.infinityfitness.Adpater.CustomerCardAdapter
import com.example.infinityfitness.database.GymDatabase

import com.example.infinityfitness.database.entity.Customer
import kotlinx.coroutines.launch
import com.example.infinityfitness.Adpater.OnCustomerButtonClickListener
import com.example.infinityfitness.CustData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class UserDataFragment : Fragment(R.layout.userdata), OnCustomerButtonClickListener {

    private lateinit var adapter: CustomerCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var database: GymDatabase


    private var customerList = mutableListOf<CustomerCard>()
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 8
    private var searchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        database = GymDatabase.getDatabase(this.requireContext())
        adapter = CustomerCardAdapter(customerList, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Set up search and infinite scrolling
        setUpSearchView()
        setUpRecyclerViewScrollListener()

        // Initially load the first page
        loadCustomers()



    }

    private fun setUpSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                customerList.clear() // Clear the current list when a new search is initiated
                currentPage = 0 // Reset page for new search
                loadCustomers() // Fetch customers based on search query
                return true
            }
        })
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

                    // Check if we have reached the end of the list
                    if ((firstVisibleItemPosition + visibleItemCount) >= totalItemCount && totalItemCount >= pageSize) {
                        println("Reached the end, loading more customers...")
                        loadCustomers() // Load more customers when at the end
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

                // Check if search query is numeric (for billNo search)
                val isNumeric = searchQuery.toIntOrNull() != null
                val numericValue = searchQuery.toIntOrNull() ?: 0

                // Fetch paginated and filtered customers
                val newCustomers = customerDao.getActiveCustomers(
                    searchQuery,
                    if (isNumeric) 1 else 0,
                    numericValue,

                )

                val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                val formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                // Check if newCustomers is empty, which would mean no more data to load

                if (newCustomers.isNotEmpty()) {
                    val newCustomerCards = newCustomers.map { customer ->
                        CustomerCard(
                            customerName = customer.name,
                            customerId = customer.billNo.toString(),
                            dueDate = LocalDateTime.parse(customer.activeTill.toString(), formatter).toLocalDate().format(formatter2).toString(),
                            imageResourceId = customer.image!!,
                            joinDate = LocalDateTime.parse(customer.joiningDate.toString() , formatter).toLocalDate().format(formatter2).toString()
                        )
                    }

                    customerList.clear()

                    // Append new customers to the list (do not clear the list)
                    customerList.addAll(newCustomerCards)
                    // First page, set adapter
                    println(customerList.toString())
                    adapter = CustomerCardAdapter(customerList, this@UserDataFragment)
                    recyclerView.adapter = adapter
                    withContext(Dispatchers.Main) {
                        adapter.notifyDataSetChanged()
                    }


//                    // Notify the adapter of changes
//                    if (currentPage == 0) {
//                        // First page, set adapter
//                        println(customerList.toString())
//                        adapter = CustomerCardAdapter(customerList, this@UserDataFragment)
//                        recyclerView.adapter = adapter
//                    } else {
//                        // Subsequent pages, just notify the adapter
//                        withContext(Dispatchers.Main) {
//                            adapter.notifyDataSetChanged()
//                        }
//                    }

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
        val intent = Intent(requireContext(), CustData::class.java).apply {
            putExtra("customerId", customer.customerId.toLongOrNull())
        }
        startActivity(intent)
    }

}
