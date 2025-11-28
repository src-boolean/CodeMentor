package com.example.codementor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class HistoryItem(val id: Int, val title: String, val description: String, val solution: String, val lang: String, val diff: String, val isSolved: Boolean)

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onClick: (HistoryItem) -> Unit,
    private val onLongClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvHistoryTitle)
        val tvLang: TextView = view.findViewById(R.id.tvHistoryLang)
        val tvDiff: TextView = view.findViewById(R.id.tvHistoryDiff)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvLang.text = item.lang
        holder.tvDiff.text = item.diff

        if (item.isSolved) {
            holder.itemView.background.setTint(0xFFE8F5E9.toInt())
        } else {
            holder.itemView.background.setTint(0xFFEEEEEE.toInt())
        }

        holder.tvLang.setTextColor(android.graphics.Color.BLACK)

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount() = items.size
}