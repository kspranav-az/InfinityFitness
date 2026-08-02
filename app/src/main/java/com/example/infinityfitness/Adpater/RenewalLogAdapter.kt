package com.example.infinityfitness.Adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R
import com.example.infinityfitness.database.dao.RenewalSummary

class RenewalLogAdapter(
    private val renewalList: List<RenewalSummary>
) : RecyclerView.Adapter<RenewalLogAdapter.RenewalLogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenewalLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.renewal_log_day_item, parent, false)
        return RenewalLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: RenewalLogViewHolder, position: Int) {
        val item = renewalList[position]
        holder.dateTextView.text = item.date
        holder.countTextView.text = "Renewals: ${item.count}"
        holder.totalTextView.text = "₹ ${String.format("%.2f", item.totalAmount)}"
    }

    override fun getItemCount(): Int = renewalList.size

    class RenewalLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val countTextView: TextView = itemView.findViewById(R.id.tvCount)
        val totalTextView: TextView = itemView.findViewById(R.id.tvTotal)
    }
}
