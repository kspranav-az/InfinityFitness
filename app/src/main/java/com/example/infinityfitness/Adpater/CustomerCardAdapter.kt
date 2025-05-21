package com.example.infinityfitness.Adpater

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.paging.PagingDataAdapter
import com.example.infinityfitness.R

data class CustomerCard(
    val customerName: String,
    val customerId: String,
    val dueDate: String,
    val imageResourceId: Bitmap,
    val joinDate: String,
    val payType: String
)

interface OnCustomerButtonClickListener {
    fun onButtonClick(customer: CustomerCard)
}

class CustomerCardAdapter(
    private val listener: OnCustomerButtonClickListener
) : PagingDataAdapter<CustomerCard, CustomerCardAdapter.CustomerCardViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CustomerCard>() {
            override fun areItemsTheSame(oldItem: CustomerCard, newItem: CustomerCard): Boolean {
                return oldItem.customerId == newItem.customerId
            }

            override fun areContentsTheSame(oldItem: CustomerCard, newItem: CustomerCard): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custcard, parent, false)
        return CustomerCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerCardViewHolder, position: Int) {
        val customer = getItem(position) ?: return

        holder.customerNameTextView.text = customer.customerName
        holder.customerIdTextView.text = customer.customerId
        holder.dueTextView.text = customer.dueDate
        holder.joinTextView.text = customer.joinDate
        holder.payType.text = customer.payType
        holder.imageView.setImageBitmap(customer.imageResourceId)

        holder.openButton.setOnClickListener {
            listener.onButtonClick(customer)
        }
    }

    class CustomerCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.CustImg)
        val customerNameTextView: TextView = itemView.findViewById(R.id.customerName)
        val customerIdTextView: TextView = itemView.findViewById(R.id.customerId)
        val dueTextView: TextView = itemView.findViewById(R.id.due)
        val joinTextView: TextView = itemView.findViewById(R.id.join)
        val openButton: ImageButton = itemView.findViewById(R.id.Open)
        val payType: TextView = itemView.findViewById(R.id.payType)
    }
}
