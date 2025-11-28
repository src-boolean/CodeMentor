package com.example.codementor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiChatFragment : Fragment() {

    private val messages = ArrayList<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var dbHelper: DatabaseHelper

    private val apiKey = "key"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai_chat, container, false)
        val rvChat = view.findViewById<RecyclerView>(R.id.rvChat)
        val etMessage = view.findViewById<EditText>(R.id.etChatMessage)
        val btnSend = view.findViewById<android.widget.ImageButton>(R.id.btnSendMessage)

        dbHelper = DatabaseHelper(requireContext())

        messages.addAll(dbHelper.getChatMessages())

        if (messages.isEmpty()) {
            messages.add(ChatMessage("Привет! Я твой AI-ментор. Задай мне вопрос по C++ или NASM!", false))
        }

        adapter = ChatAdapter(messages)
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(context)
        rvChat.scrollToPosition(messages.size - 1)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString()
            if (text.isNotEmpty()) {
                addMessage(text, true)
                etMessage.text.clear()
                sendToGemini(text)
            }
        }

        return view
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        dbHelper.saveChatMessage(text, isUser)
    }

    private fun sendToGemini(prompt: String) {
        val request = GeminiRequest(listOf(Content(listOf(Part(prompt)))))
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

        RetrofitClient.instance.generateContent(url, request, apiKey).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val aiText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    val finalResponse = aiText ?: "Не удалось получить ответ."
                    addMessage(finalResponse, false)
                } else {
                    addMessage("Ошибка сервера: ${response.code()}", false)
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                addMessage("Ошибка сети. Проверьте VPN.", false)
            }
        })
    }
}