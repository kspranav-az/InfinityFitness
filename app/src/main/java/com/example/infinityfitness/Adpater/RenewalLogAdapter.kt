package com.example.infinityfitness.Adpater

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.infinityfitness.R
import com.example.infinityfitness.database.dao.RenewalDetail
import java.text.SimpleDateFormat
import java.util.Locale

class RenewalLogAdapter(
    private val renewalList: List<RenewalDetail>
) : RecyclerView.Adapter<RenewalLogAdapter.RenewalLogViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenewalLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.renewal_log_item, parent, false)
        return RenewalLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: RenewalLogViewHolder, position: Int) {
        val item = renewalList[position]
        holder.nameTextView.text = item.customerName
        holder.amountTextView.text = "₹ ${String.format("%.2f", item.finalPrice)}"
        holder.billNoTextView.text = "Bill #${item.billNo}"
        holder.phoneTextView.text = item.phoneNumber ?: "-"
        holder.dateTextView.text = "Renewed: ${dateFormatter.format(item.startDate)}"
        holder.packTextView.text = item.packType ?: "-"

        if (item.customerImage != null) {
            holder.customerImageView.setImageBitmap(item.customerImage)
        } else {
            holder.customerImageView.setImageResource(R.drawable.uploadimage)
        }

        holder.customerImageView.setOnClickListener {
            showImageInDialog(holder.customerImageView)
        }
    }

    override fun getItemCount(): Int = renewalList.size

    private fun showImageInDialog(imageView: ImageView) {
        val context = imageView.context
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image)

        val enlargedImageView = dialog.findViewById<ImageView>(R.id.enlargedImageView)
        enlargedImageView.setImageBitmap(imageView.drawable.toBitmap())

        dialog.show()
    }

    class RenewalLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerImageView: ImageView = itemView.findViewById(R.id.ivCustomerImage)
        val nameTextView: TextView = itemView.findViewById(R.id.tvCustomerName)
        val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        val billNoTextView: TextView = itemView.findViewById(R.id.tvBillNo)
        val phoneTextView: TextView = itemView.findViewById(R.id.tvPhone)
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val packTextView: TextView = itemView.findViewById(R.id.tvPack)
    }
}
