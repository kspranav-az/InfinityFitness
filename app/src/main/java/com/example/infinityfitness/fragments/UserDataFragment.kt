package com.example.infinityfitness.fragments

import android.annotation.SuppressLint
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

class UserDataFragment : Fragment(R.layout.userdata) {

    private lateinit var adapter: CustomerCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var database: GymDatabase

    private var customerList = mutableListOf<CustomerCard>()
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 8 // Limit for pagination
    private var searchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        adapter = CustomerCardAdapter(customerList)
        database = GymDatabase.getDatabase(this.requireContext())

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
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && firstVisibleItemPosition + visibleItemCount >= totalItemCount && totalItemCount >= pageSize) {
                    loadCustomers() // Load more customers when the user scrolls down
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCustomers() {
        isLoading = true

        // Fetch customers from the Room database (DAO call)
        lifecycleScope.launch {
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

            // Map to CustomerCard and add to list
            val newCustomerCards = newCustomers.map { customer : Customer ->
                CustomerCard(
                    customerName = customer.name,
                    customerId = customer.billNo.toString(),
                    dueDate = customer.activeTill.toString(),
                    imageResourceId = customer.image!!
                )
            }

            customerList.clear()
            customerList.addAll(newCustomerCards)
            adapter.notifyDataSetChanged()
            isLoading = false
            currentPage++
        }
    }
}
