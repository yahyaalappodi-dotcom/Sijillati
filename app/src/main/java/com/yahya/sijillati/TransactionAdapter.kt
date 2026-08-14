package com.yahya.sijillati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val items: List<Transaction>,
    private val onLongPress: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.tvTitle.text = t.title
        holder.tvAmount.text = String.format("%,.0f %s", t.amount, t.currency)
        holder.tvType.text = t.type
        holder.tvDate.text = "${t.date} • ${t.paymentMethod}"
        holder.itemView.setOnLongClickListener { onLongPress(t); true }
    }

    override fun getItemCount() = items.size
}
