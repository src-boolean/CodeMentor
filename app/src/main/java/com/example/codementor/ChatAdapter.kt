package com.example.codementor

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val messageContainer: LinearLayout = view.findViewById(R.id.messageContainer)
        val ivBotAvatar: ImageView = view.findViewById(R.id.ivBotAvatar)
        val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]
        val context = holder.itemView.context

        holder.tvMessage.text = msg.text

        if (msg.isUser) {
            holder.messageContainer.gravity = Gravity.END
            holder.ivBotAvatar.visibility = View.GONE
            holder.tvSenderName.visibility = View.GONE

            holder.tvMessage.background.setTint(ContextCompat.getColor(context, R.color.primary))
            holder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.messageContainer.gravity = Gravity.START
            holder.ivBotAvatar.visibility = View.VISIBLE
            holder.tvSenderName.visibility = View.VISIBLE

            holder.tvMessage.background.setTint(0xFFEEEEEE.toInt())
            holder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    override fun getItemCount() = messages.size
}