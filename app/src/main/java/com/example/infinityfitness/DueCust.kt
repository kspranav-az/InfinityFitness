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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


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

                val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                val formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                // Map due customers to customer cards
                val dueCustomerCards = dueCustomers?.map { customer ->
                    CustomerCard(
                        customerName = customer.name,
                        customerId = customer.billNo.toString(),
                        dueDate = LocalDateTime.parse(customer.activeTill.toString(), formatter).toLocalDate().format(formatter2).toString(),
                        joinDate = LocalDateTime.parse(customer.joiningDate.toString(), formatter).toLocalDate().format(formatter2).toString(),
                        imageResourceId = customer.image!!, // Use a default image if null
                        payType = database.subscriptionDao().getSubscriptionById(customer.billNo)?.paymentMethod.toString()
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
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=91$phoneNumber&text=$formattedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onButtonClick(customer: CustomerCard) {
        val customerDao = database.customerDao()
        lifecycleScope.launch {
            val cus = customerDao.getCustomerByBillNo(customer.customerId.toLong())
            val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val message = "Dear ${cus!!.name},\n" +
                    "\n" +
                    "We hope this message finds you well!\n" +
                    "\n" +
                    "We wanted to take a moment to remind you that your gym membership is set to expire on ${LocalDateTime.parse(cus.activeTill.toString(), formatter).toLocalDate().toString()}. We value your commitment to your fitness journey and would like to encourage you to renew your membership before the expiration date to continue enjoying all the facilities and benefits we offer.\n" +
                    "\n" +
                    "If you have any questions about the renewal process or need assistance, please don’t hesitate to reach out. We’re here to help!\n" +
                    "\n" +
                    "Thank you for being a part of our Infinity Fitness family. We look forward to seeing you continue your journey with us!"
            cus.phoneNumber?.let { sendWhatsAppMessage(it,message) }

        }
    }

    override fun onBackPressed() {

        if (false){
            super.onBackPressed()
        }
        startActivity(
            Intent(
                this,
                home::class.java
            )
        )

    }


}