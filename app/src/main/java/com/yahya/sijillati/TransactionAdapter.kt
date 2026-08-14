package com.yahya.sijillati

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yahya.sijillati.database.TransactionEntity
import com.yahya.sijillati.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private var items: List<TransactionEntity>, private val onItemClick: (TransactionEntity) -> Unit) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvAmount.text = "%,.0f %s".format(Locale.US, item.amount, if (item.currency == "IQD") "د.ع" else "$")
        holder.binding.tvDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.date))
        holder.binding.tvType.text = when (item.type) { "INCOME" -> "دخل"; "EXPENSE" -> "مصروف"; "LEND" -> "إقراض"; "BORROW" -> "اقتراض"; else -> item.type }
        holder.binding.tvPayment.text = if (item.paymentMethod == "CASH") "كاش" else "بطاقة"
        val color = when (item.type) { "INCOME" -> R.color.green; "EXPENSE" -> R.color.red; "LEND" -> R.color.orange; "BORROW" -> R.color.purple; else -> R.color.black }
        holder.binding.tvAmount.setTextColor(holder.binding.root.context.getColor(color))
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }
    override fun getItemCount() = items.size
    fun updateList(newList: List<TransactionEntity>) { items = newList; notifyDataSetChanged() }
}
