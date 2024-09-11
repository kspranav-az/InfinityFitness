package com.example.infinityfitness.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R
import com.example.infinityfitness.Adpater.CustomerCard
import com.example.infinityfitness.Adpater.CustomerCardAdapter
import com.example.infinityfitness.Adpater.OnCustomerButtonClickListener
import android.widget.SearchView
import com.example.infinityfitness.CustData

class UserDataFragment : Fragment(R.layout.userdata), OnCustomerButtonClickListener {

    private lateinit var adapter: CustomerCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)

        val customerList = listOf(
            CustomerCard("John Doe", "001", "12.12.2024"),
            CustomerCard("Jane Smith", "002", "15.10.2024"),
            CustomerCard("Michael Johnson", "003", "01.09.2024")
        )

        adapter = CustomerCardAdapter(customerList, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
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
