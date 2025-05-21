package com.example.infinityfitness.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.Adpater.CustomerCardAdapter
import com.example.infinityfitness.Adpater.OnCustomerButtonClickListener
import com.example.infinityfitness.CustData
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.databinding.UserdataBinding
import com.example.infinityfitness.Adpater.CustomerCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.paging.map
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class UserDataFragment : Fragment(), OnCustomerButtonClickListener {

    private var _binding: UserdataBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CustomerCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: GymDatabase

    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserdataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView
        database = GymDatabase.getDatabase(requireContext())

        adapter = CustomerCardAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSearchView()
        fetchPagedData()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                fetchPagedData()
                return true
            }
        })
    }



    private fun fetchPagedData() {
        val isNumeric = currentSearchQuery.toIntOrNull() != null
        val numericValue = currentSearchQuery.toIntOrNull() ?: 0

        val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        lifecycleScope.launch {
            Pager(
                config = PagingConfig(
                    pageSize = 8,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    database.customerDao().getActiveCustomersPaged(
                        currentSearchQuery,
                        if (isNumeric) 1 else 0,
                        numericValue
                    )
                }
            ).flow
                .map { pagingData ->
                    // Map Customer -> CustomerCard here
                    pagingData.map { customer ->
                        CustomerCard(
                            customerName = customer.name,
                            customerId = customer.billNo.toString(),
                            dueDate = LocalDateTime.parse(customer.activeTill.toString(), formatter)
                                .toLocalDate()
                                .format(formatter2),
                            imageResourceId = customer.image!!,
                            joinDate = LocalDateTime.parse(customer.joiningDate.toString(), formatter)
                                .toLocalDate()
                                .format(formatter2),
                            payType = database.subscriptionDao().getSubscriptionById(customer.billNo)?.paymentMethod.toString()
                        )
                    }
                }
                .cachedIn(lifecycleScope)
                .collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
        }
    }


    override fun onButtonClick(customerCard: CustomerCard) {
        val intent = Intent(requireContext(), CustData::class.java).apply {
            putExtra("customerId", customerCard.customerId.toLongOrNull())
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
