package com.example.infinityfitness.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R
import com.example.infinityfitness.Adpater.CustomerCard
import com.example.infinityfitness.Adpater.CustomerCardAdapter
import android.widget.SearchView

class UserDataFragment : Fragment(R.layout.userdata) {

    private lateinit var adapter: CustomerCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)


        //make changes here and add the dao pranav fucker
        val customerList = listOf(
            CustomerCard("John Doe", "001", "12.12.2024"),
            CustomerCard("Jane Smith", "002", "15.10.2024"),
            CustomerCard("Michael Johnson", "003", "01.09.2024")
        )

        adapter = CustomerCardAdapter(customerList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Set up SearchView listener
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
}
