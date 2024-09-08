package com.example.infinityfitness.Adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R

data class CustomerCard(
    val customerName: String,
    val customerId: String,
    val dueDate: String,
    val imageResourceId: Int = 0 // Assuming you have image resources for customers (optional)
)

class CustomerCardAdapter(private val customerList: List<CustomerCard>) :
    RecyclerView.Adapter<CustomerCardAdapter.CustomerCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.custcard, parent, false)
        return CustomerCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerCardViewHolder, position: Int) {
        val customer = customerList[position]

        customer.customerName.also { holder.customerNameTextView.text = it }
        customer.customerId.also { holder.customerIdTextView.text = it }
        customer.dueDate.also { holder.dueTextView.text = it }

        if (customer.imageResourceId != 0) { // Check if image resource is available
            holder.imageView.setImageResource(customer.imageResourceId)
        } else {
            // Set a default image if needed
        }
    }

    override fun getItemCount(): Int = customerList.size

    class CustomerCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView3)
        val customerNameTextView: TextView = itemView.findViewById(R.id.customerName)
        val customerIdTextView: TextView = itemView.findViewById(R.id.customerId)
        val dueTextView: TextView = itemView.findViewById(R.id.due)
    }
}