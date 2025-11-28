package com.example.codementor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Topic(val name: String, val description: String)

class TopicAdapter(
    private val topics: List<Topic>,
    private val onClick: (Topic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvTopicName)
        val tvDesc: TextView = view.findViewById(R.id.tvTopicDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.tvName.text = topic.name
        holder.tvDesc.text = topic.description
        holder.itemView.setOnClickListener { onClick(topic) }
    }

    override fun getItemCount() = topics.size
}