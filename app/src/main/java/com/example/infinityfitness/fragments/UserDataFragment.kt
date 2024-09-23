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

                // Check if search query is numeric (for billNo search)
                val isNumeric = searchQuery.toIntOrNull() != null
                val numericValue = searchQuery.toIntOrNull() ?: 0

                // Fetch paginated and filtered customers
                val newCustomers = customerDao.getActiveCustomersPaged(
                    searchQuery,
                    if (isNumeric) 1 else 0,
                    numericValue,
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
                        adapter = CustomerCardAdapter(customerList, this@UserDataFragment)
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
        val intent = Intent(requireContext(), CustData::class.java).apply {
            putExtra("customerName", customer.customerName)
            putExtra("customerId", customer.customerId)
            putExtra("dueDate", customer.dueDate)
        }
        startActivity(intent)
    }
}
