package com.example.codementor

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskActivity : AppCompatActivity() {

    private lateinit var tvTaskTitle: TextView
    private lateinit var tvTaskDescription: TextView
    private lateinit var etCodeEditor: EditText
    private lateinit var btnSubmitCode: Button
    private lateinit var tvAiResponse: TextView

    private val apiKey = "AIzaSyBvr_ryPHkjb6fMwwJcEe7nVKtqDITi8vU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        tvTaskTitle = findViewById(R.id.tvTaskTitle)
        tvTaskDescription = findViewById(R.id.tvTaskDescription)
        etCodeEditor = findViewById(R.id.etCodeEditor)
        btnSubmitCode = findViewById(R.id.btnSubmitCode)
        tvAiResponse = findViewById(R.id.tvAiResponse)

        val title = intent.getStringExtra("TASK_TITLE")
        val description = intent.getStringExtra("TASK_DESCRIPTION")

        tvTaskTitle.text = title
        tvTaskDescription.text = description

        btnSubmitCode.setOnClickListener {
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val userCode = etCodeEditor.text.toString()
        if (userCode.isBlank()) {
            Toast.makeText(this, "Поле с кодом не может быть пустым", Toast.LENGTH_SHORT).show()
            return
        }

        if (apiKey == "YOUR_API_KEY") {
            Toast.makeText(
                this,
                "Пожалуйста, вставьте ваш API ключ в TaskActivity.kt",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        btnSubmitCode.isEnabled = false
        tvAiResponse.visibility = View.VISIBLE
        tvAiResponse.text = "Анализирую решение..."

        val taskDescription = tvTaskDescription.text.toString()
        val prompt = """
        Проверь решение задачи.
        Условие задачи: "$taskDescription".
        Код пользователя:
        ```
        $userCode
        ```
        Твой ответ должен быть кратким:
        1. Если решение верное, напиши: "Решение верное." и можешь предложить эталонный вариант.
        2. Если есть ошибка, укажи на нее и объясни, как исправить.
    """.trimIndent()

        val request = GeminiRequest(listOf(Content(listOf(Part(prompt)))))

        val url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.0-pro:generateContent"

        RetrofitClient.instance.generateContent(url, request, apiKey)
            .enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(
                    call: Call<GeminiResponse>,
                    response: Response<GeminiResponse>
                ) {
                    btnSubmitCode.isEnabled = true
                    if (response.isSuccessful) {
                        val aiText =
                            response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        tvAiResponse.text = aiText ?: "Получен пустой ответ"
                    } else {
                        tvAiResponse.text =
                            "Ошибка от сервера: ${response.code()} - ${response.message()}"
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    btnSubmitCode.isEnabled = true
                    tvAiResponse.text = "Ошибка сети: ${t.message}"
                }
            })
    }
}