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


class DueCust : AppCompatActivity() , OnCustomerButtonClickListener{

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
        recyclerView.layoutManager = LinearLayoutManager(this@DueCust)

        // Create an adapter with an empty listener as no click handling is needed
        adapter = CustomerCardAdapter(customerList, this@DueCust )

        var btn : ImageButton = findViewById(R.id.back)

        btn.setOnClickListener {
            startActivity(Intent(this,home::class.java))
        }

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


                val dueDate = Calendar.getInstance()
                dueDate.add(Calendar.DAY_OF_YEAR, 3)
                // Fetch customers whose due date is 2 days later
                val dueCustomers = customerDao.getDueCustomers(dueDate.time)

                println(dueCustomers.orEmpty().toString())

                // Map due customers to customer cards
                val dueCustomerCards = dueCustomers?.map { customer ->
                    CustomerCard(
                        customerName = customer.name,
                        customerId = customer.billNo.toString(),
                        dueDate = customer.activeTill.date.toString(),
                        imageResourceId = customer.image!! // Use a default image if null
                    )
                }

                println(dueCustomerCards.orEmpty().toString())

                // Clear the existing list and add new due customers
                customerList.clear() // Clear existing list for fresh data
                if (dueCustomerCards != null) {
                    customerList.addAll(dueCustomerCards)
                }

                println(customerList.orEmpty().toString())



                withContext(Dispatchers.Main) {
                    adapter = CustomerCardAdapter(customerList, this@DueCust )
                    recyclerView.adapter = adapter
                    // Notify the adapter of changes
                    adapter.notifyDataSetChanged()

                }


            } catch (e: Exception) {
                e.printStackTrace() // Log any errors
            } finally {
                isLoading = false
            }
        }
    }

    private fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        val formattedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=$formattedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onButtonClick(customer: CustomerCard) {
        val customerDao = database.customerDao()
        lifecycleScope.launch {
            val cus = customerDao.getCustomerByBillNo(customer.customerId.toLong())
            if (cus != null) {
                cus.phoneNumber?.let { sendWhatsAppMessage(it,"you have your due on ${cus.activeTill}") }
            }

        }
    }



}