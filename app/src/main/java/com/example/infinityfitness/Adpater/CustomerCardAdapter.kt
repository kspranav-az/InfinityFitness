package com.example.infinityfitness.Adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R
import android.widget.Filter
import android.widget.Filterable

data class CustomerCard(
    val customerName: String,
    val customerId: String,
    val dueDate: String,
    val imageResourceId: Int = 0
)

class CustomerCardAdapter(private val customerList: List<CustomerCard>) :
    RecyclerView.Adapter<CustomerCardAdapter.CustomerCardViewHolder>(), Filterable {

    private var filteredCustomerList = customerList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.custcard, parent, false)
        return CustomerCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerCardViewHolder, position: Int) {
        val customer = filteredCustomerList[position]

        holder.customerNameTextView.text = customer.customerName
        holder.customerIdTextView.text = customer.customerId
        holder.dueTextView.text = customer.dueDate

        if (customer.imageResourceId != 0) {
            holder.imageView.setImageResource(customer.imageResourceId)
        } else {
            // Optionally set a default image
        }
    }

    override fun getItemCount(): Int = filteredCustomerList.size

    class CustomerCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView3)
        val customerNameTextView: TextView = itemView.findViewById(R.id.customerName)
        val customerIdTextView: TextView = itemView.findViewById(R.id.customerId)
        val dueTextView: TextView = itemView.findViewById(R.id.due)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase()

                val filteredList = if (query.isNullOrEmpty()) {
                    customerList
                } else {
                    customerList.filter {
                        it.customerName.lowercase().contains(query) || it.customerId.contains(query)
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCustomerList = results?.values as MutableList<CustomerCard>
                notifyDataSetChanged()
            }
        }
    }
}