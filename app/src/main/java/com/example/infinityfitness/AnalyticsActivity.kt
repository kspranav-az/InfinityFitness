package com.example.infinityfitness

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.infinityfitness.database.GymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var database: GymDatabase
    private lateinit var tvTotalActiveCount: TextView
    private lateinit var tvMaleCount: TextView
    private lateinit var tvFemaleCount: TextView
    private lateinit var tvTotalCustomerCount: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)

        database = GymDatabase.getDatabase(this)

        btnBack = findViewById(R.id.back)
        tvTotalActiveCount = findViewById(R.id.tvTotalActiveCount)
        tvMaleCount = findViewById(R.id.tvMaleCount)
        tvFemaleCount = findViewById(R.id.tvFemaleCount)
        tvTotalCustomerCount = findViewById(R.id.tvTotalCustomerCount)

        btnBack.setOnClickListener {
            finish()
        }

        loadAnalytics()
    }

    override fun onResume() {
        super.onResume()
        loadAnalytics()
    }

    private fun loadAnalytics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val today = cal.time

            val customerDao = database.customerDao()
            val maleCount = customerDao.getActiveMaleCount(today)
            val femaleCount = customerDao.getActiveFemaleCount(today)
            val totalActive = customerDao.getTotalActiveCount(today)
            val totalRegistered = customerDao.getTotalCustomerCount()

            withContext(Dispatchers.Main) {
                tvMaleCount.text = maleCount.toString()
                tvFemaleCount.text = femaleCount.toString()
                tvTotalActiveCount.text = totalActive.toString()
                tvTotalCustomerCount.text = totalRegistered.toString()
            }
        }
    }
}
