package com.example.codementor

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskActivity : AppCompatActivity() {

    private lateinit var tvTaskDescription: TextView
    private lateinit var etCodeEditor: EditText
    private lateinit var btnSubmitCode: Button
    private lateinit var btnHelp: Button
    private lateinit var btnSaveManual: Button
    private lateinit var responseLayout: LinearLayout
    private lateinit var tvResponseMessage: TextView
    private lateinit var btnGoToChat: Button
    private lateinit var tvResponseTitle: TextView


    private val apiKey = "key"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"


    private var isCheated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val toolbar: Toolbar = findViewById(R.id.toolbar_task)
        setSupportActionBar(toolbar)

        val selectedLanguage = intent.getStringExtra("TASK_LANGUAGE") ?: "Язык"
        val selectedDifficulty = intent.getStringExtra("TASK_DIFFICULTY") ?: "Сложность"
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Задача"

        supportActionBar?.title = "$taskTitle"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        tvTaskDescription = findViewById(R.id.tvTaskDescription)
        etCodeEditor = findViewById(R.id.etCodeEditor)
        btnSubmitCode = findViewById(R.id.btnSubmitCode)
        btnHelp = findViewById(R.id.btnHelp)
        btnSaveManual = findViewById(R.id.btnSaveManual)
        responseLayout = findViewById(R.id.responseLayout)
        tvResponseMessage = findViewById(R.id.tvResponseMessage)
        btnGoToChat = findViewById(R.id.btnGoToChat)
        tvResponseTitle = findViewById(R.id.tvResponseTitle)

        val description = intent.getStringExtra("TASK_DESCRIPTION") ?: ""
        tvTaskDescription.text = formatAiResponse(description)


        val savedSolution = intent.getStringExtra("TASK_SOLUTION")
        if (!savedSolution.isNullOrEmpty()) {
            responseLayout.visibility = View.VISIBLE
            tvResponseTitle.text = "Архив"
            tvResponseMessage.text = formatAiResponse(savedSolution)
            etCodeEditor.visibility = View.GONE
            btnSubmitCode.visibility = View.GONE
        }

        btnSubmitCode.setOnClickListener {
            val code = etCodeEditor.text.toString().trim()

            // ЗАЩИТА №1: Длина кода
            if (code.length < 10) {
                Toast.makeText(this, "Это не решение! Напишите код.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            checkCodeWithGemini(code, description)
        }

        btnSaveManual.setOnClickListener {

            val currentSolution = tvResponseMessage.text.toString()
            val isSolved = tvResponseTitle.text.toString().contains("УСПЕХ", ignoreCase = true)

            saveResult(taskTitle, description, currentSolution, selectedLanguage, selectedDifficulty, isSolved, 0)
            Toast.makeText(this, "Сохранено в историю", Toast.LENGTH_SHORT).show()
        }

        btnHelp.setOnClickListener {
            showHelpDialog()
        }

        btnGoToChat.setOnClickListener {
            responseLayout.visibility = View.GONE
            etCodeEditor.visibility = View.VISIBLE
            btnSubmitCode.visibility = View.VISIBLE
            btnSubmitCode.isEnabled = true
        }
    }

    private fun showHelpDialog() {
        val options = arrayOf("Хочу подсказку (XP доступны)", "Реши за меня (0 XP)")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Помощь ментора")
        builder.setItems(options) { _, which ->
            val taskDesc = tvTaskDescription.text.toString()
            val language = intent.getStringExtra("TASK_LANGUAGE") ?: "Code"

            when (which) {
                0 -> {

                    sendSimplePrompt("Дай короткую подсказку (идею) к задаче на $language: $taskDesc. Не пиши код.")
                }
                1 -> {

                    isCheated = true
                    sendSimplePrompt("Напиши полное решение задачи на $language: $taskDesc. Объясни код.")
                }
            }
        }
        builder.show()
    }


    private fun sendSimplePrompt(prompt: String) {
        sendRequest(prompt) { text ->
            tvResponseTitle.text = "Ментор"
            tvResponseMessage.text = formatAiResponse(text)
        }
    }


    private fun checkCodeWithGemini(code: String, taskDesc: String) {
        val language = intent.getStringExtra("TASK_LANGUAGE") ?: "Code"


        val strictPrompt = """
            Ты строгий преподаватель по $language.
            Ученик прислал решение задачи: "$taskDesc".
            
            Вот код ученика:
            ```
            $code
            ```
            
            Твоя задача проверить:
            1. Относится ли этот код к задаче?
            2. Является ли это валидным кодом (а не просто случайными цифрами или словами)?
            3. Правильно ли решена задача?
            
            ЕСЛИ КОД ВЕРНЫЙ: Начни ответ с фразы "[[VERDICT_OK]]". Потом похвали и покажи, как улучшить.
            ЕСЛИ КОД НЕВЕРНЫЙ или это мусор: Начни ответ с фразы "[[VERDICT_FAIL]]". Объясни ошибку.
        """.trimIndent()

        sendRequest(strictPrompt) { text ->

            if (text.contains("[[VERDICT_OK]]")) {

                val cleanText = text.replace("[[VERDICT_OK]]", "").trim()
                tvResponseMessage.text = formatAiResponse(cleanText)

                if (!isCheated) {
                    tvResponseTitle.text = "УСПЕХ! (+XP)"
                    val xp = calculateXP()
                    saveSuccessToHistory(cleanText, xp)
                    Toast.makeText(applicationContext, "Вам начислено $xp XP!", Toast.LENGTH_LONG).show()
                } else {
                    tvResponseTitle.text = "УСПЕХ (Без XP)"
                    saveSuccessToHistory(cleanText, 0) // Списал = 0 XP
                    Toast.makeText(applicationContext, "Решено верно. (XP не начислен: использована помощь)", Toast.LENGTH_LONG).show()
                }

            } else {
                
                tvResponseTitle.text = "ЕСТЬ ОШИБКИ"
                val cleanText = text.replace("[[VERDICT_FAIL]]", "").trim()
                tvResponseMessage.text = formatAiResponse(cleanText)
                Toast.makeText(applicationContext, "Код содержит ошибки", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendRequest(prompt: String, onSuccess: (String) -> Unit) {
        btnSubmitCode.isEnabled = false
        responseLayout.visibility = View.VISIBLE
        etCodeEditor.visibility = View.GONE
        btnSubmitCode.visibility = View.GONE

        tvResponseTitle.text = "Анализ..."
        tvResponseMessage.text = "AI думает..."

        val request = GeminiRequest(listOf(Content(listOf(Part(prompt)))))

        RetrofitClient.instance.generateContent(apiUrl, request, apiKey).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                btnSubmitCode.isEnabled = true
                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Пустой ответ"
                    onSuccess(rawText)
                } else {
                    tvResponseTitle.text = "Ошибка API"
                    tvResponseMessage.text = "Код ошибки: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                btnSubmitCode.isEnabled = true
                tvResponseTitle.text = "Ошибка сети"
                tvResponseMessage.text = t.message
            }
        })
    }

    private fun calculateXP(): Int {
        val diff = intent.getStringExtra("TASK_DIFFICULTY") ?: ""
        return when {
            diff.contains("Легк") -> 15
            diff.contains("Норм") || diff.contains("Сред") -> 30
            diff.contains("Слож") || diff.contains("Тяжел") -> 50
            diff.contains("PRO") -> 80
            else -> 20
        }
    }

    private fun formatAiResponse(text: String): String {
        var result = text
        // Убираем жирный шрифт Markdown
        result = result.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        result = result.replace("*", "")
        result = result.replace("#", "")

        // Оформляем код
        if (result.contains("```")) {
            result = result.replace("```cpp", "\n--- C++ ---\n")
            result = result.replace("```nasm", "\n--- NASM ---\n")
            result = result.replace("```", "\n-----------\n")
        }
        return result.trim()
    }

    private fun saveSuccessToHistory(solution: String, xp: Int) {
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Задача"
        val description = intent.getStringExtra("TASK_DESCRIPTION") ?: ""
        val lang = intent.getStringExtra("TASK_LANGUAGE") ?: ""
        val diff = intent.getStringExtra("TASK_DIFFICULTY") ?: ""

        saveResult(taskTitle, description, solution, lang, diff, true, xp)
    }

    private fun saveResult(title: String, desc: String, sol: String, lang: String, diff: String, isSolved: Boolean, xp: Int) {
        val dbHelper = DatabaseHelper(this)
        dbHelper.saveToHistory(title, desc, sol, lang, diff, isSolved, xp)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}