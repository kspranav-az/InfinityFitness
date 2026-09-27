package com.example.infinityfitness

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.Adpater.RenewalLogAdapter
import com.example.infinityfitness.database.GymDatabase
import com.example.infinityfitness.database.dao.RenewalDetail
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RenewalLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RenewalLogAdapter
    private lateinit var database: GymDatabase
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnClear: Button

    private val renewalList = mutableListOf<RenewalDetail>()
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val queryDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var currentPage = 0
    private val pageSize = 15
    private var isLoading = false
    private var hasMoreData = true

    private var loadJob: Job? = null
    private var searchDebounceJob: Job? = null

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            searchDebounceJob?.cancel()
            searchDebounceJob = lifecycleScope.launch {
                delay(500)
                loadInitial()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.renewal_log)

        database = GymDatabase.getDatabase(this)

        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnClear = findViewById(R.id.btnClear)

        recyclerView = findViewById(R.id.renewalLogView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RenewalLogAdapter(renewalList)
        recyclerView.adapter = adapter

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        etFromDate.setOnClickListener { showDatePicker(etFromDate) }
        etToDate.setOnClickListener { showDatePicker(etToDate) }

        btnSearch.setOnClickListener { searchRenewals() }
        btnClear.setOnClickListener {
            etSearch.removeTextChangedListener(searchTextWatcher)
            etFromDate.text.clear()
            etToDate.text.clear()
            etSearch.text.clear()
            etSearch.addTextChangedListener(searchTextWatcher)
            loadInitial()
        }

        etSearch.addTextChangedListener(searchTextWatcher)
        setUpRecyclerViewScrollListener()

        // Default to today
        val today = displayDateFormat.format(Calendar.getInstance().time)
        etFromDate.setText(today)
        etToDate.setText(today)
        loadInitial()
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                target.setText(displayDateFormat.format(selectedCalendar.time))
                loadInitial()
            },
            year, month, day
        ).show()
    }

    private fun searchRenewals() {
        val fromDateStr = etFromDate.text.toString().trim()
        val toDateStr = etToDate.text.toString().trim()

        when {
            fromDateStr.isEmpty() && toDateStr.isEmpty() -> {
                Toast.makeText(this, "Please select at least one date", Toast.LENGTH_SHORT).show()
                return
            }
            fromDateStr.isNotEmpty() && toDateStr.isEmpty() -> {
                etToDate.setText(fromDateStr)
            }
            fromDateStr.isEmpty() && toDateStr.isNotEmpty() -> {
                etFromDate.setText(toDateStr)
            }
        }

        loadInitial()
    }

    private fun parseDisplayDate(dateStr: String): String? {
        return try {
            val date = displayDateFormat.parse(dateStr)
            date?.let { queryDateFormat.format(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun setUpRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isLoading || !hasMoreData) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((firstVisibleItemPosition + visibleItemCount) >= totalItemCount - 3) {
                    loadPage(currentPage + 1)
                }
            }
        })
    }

    private fun loadInitial() {
        loadJob?.cancel()
        searchDebounceJob?.cancel()
        renewalList.clear()
        adapter.notifyDataSetChanged()
        currentPage = 0
        hasMoreData = true
        isLoading = false
        loadPage(0)
    }

    private fun loadPage(page: Int) {
        if (isLoading) return
        isLoading = true

        val searchText = etSearch.text.toString().trim().lowercase(Locale.getDefault())
        val numericSearch = searchText.toLongOrNull() ?: -1L
        val fromDateStr = etFromDate.text.toString().trim()
        val toDateStr = etToDate.text.toString().trim()

        loadJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val details = if (fromDateStr.isEmpty() && toDateStr.isEmpty()) {
                    database.subscriptionDao().getAllRenewalDetailsPaged(
                        searchText,
                        numericSearch,
                        pageSize,
                        page * pageSize
                    )
                } else {
                    val fromQueryDate = parseDisplayDate(fromDateStr)
                    val toQueryDate = parseDisplayDate(toDateStr)
                    if (fromQueryDate == null || toQueryDate == null) {
                        showError("Invalid date format")
                        isLoading = false
                        return@launch
                    }
                    database.subscriptionDao().getRenewalDetailsByDateRangePaged(
                        fromQueryDate,
                        toQueryDate,
                        searchText,
                        numericSearch,
                        pageSize,
                        page * pageSize
                    )
                }

                val startIndex = renewalList.size
                renewalList.addAll(details)
                currentPage = page
                hasMoreData = details.size == pageSize

                withContext(Dispatchers.Main) {
                    if (startIndex == 0) {
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.notifyItemRangeInserted(startIndex, details.size)
                    }

                    if (renewalList.isEmpty()) {
                        Toast.makeText(this@RenewalLogActivity, "No renewals found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: CancellationException) {
                // ignored
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Failed to load renewal log")
            } finally {
                isLoading = false
            }
        }
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@RenewalLogActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
