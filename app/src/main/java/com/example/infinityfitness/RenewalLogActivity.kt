package com.example.infinityfitness

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.Adpater.RenewalLogAdapter
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.dao.RenewalSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RenewalLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RenewalLogAdapter
    private lateinit var database: GymDatabase
    private val renewalList = mutableListOf<RenewalSummary>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.renewal_log)

        database = GymDatabase.getDatabase(this)

        recyclerView = findViewById(R.id.renewalLogView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RenewalLogAdapter(renewalList)
        recyclerView.adapter = adapter

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        loadRenewalLog()
    }

    private fun loadRenewalLog() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val summary = database.subscriptionDao().getRenewalSummary()
                renewalList.clear()
                renewalList.addAll(summary)

                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    if (renewalList.isEmpty()) {
                        Toast.makeText(
                            this@RenewalLogActivity,
                            "No renewals found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RenewalLogActivity,
                        "Failed to load renewal log",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
