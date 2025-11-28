package com.example.codementor

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val toolbar: Toolbar = findViewById(R.id.toolbar_task)
        setSupportActionBar(toolbar)

        val selectedLanguage = intent.getStringExtra("TASK_LANGUAGE") ?: "Язык"
        val selectedDifficulty = intent.getStringExtra("TASK_DIFFICULTY") ?: "Сложность"
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Задача"

        supportActionBar?.title = "$taskTitle ($selectedDifficulty)"
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
            tvResponseTitle.text = "Архивное решение"
            tvResponseMessage.text = formatAiResponse(savedSolution)
            etCodeEditor.visibility = View.GONE
            btnSubmitCode.visibility = View.GONE
        }

        btnSubmitCode.setOnClickListener {
            val code = etCodeEditor.text.toString()
            if (code.isBlank()) {
                Toast.makeText(this, "Введите код", Toast.LENGTH_SHORT).show()
            } else {
                sendPromptToGemini(
                    "Проверь код задачи: $description. Код: $code. " +
                            "Ответ должен быть БЕЗ жирного текста (**), используй простой текст. " +
                            "В начале напиши 'ВЕРНО' или 'ЕСТЬ ОШИБКИ'.",
                    true
                )
            }
        }

        btnSaveManual.setOnClickListener {
            val currentSolution = tvResponseMessage.text.toString()
            val isSolved = tvResponseTitle.text.toString().contains("УСПЕХ", ignoreCase = true)
            saveResult(taskTitle, description, currentSolution, selectedLanguage, selectedDifficulty, isSolved, 0)
            Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show()
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
        val options = arrayOf("Хочу подсказку", "Реши за меня с объяснением")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Что не получается?")
        builder.setItems(options) { _, which ->
            val taskDesc = tvTaskDescription.text.toString()
            val language = intent.getStringExtra("TASK_LANGUAGE") ?: "C++"

            val basePrompt = "Задача на языке $language: $taskDesc. Не используй Markdown форматирование (звездочки). "

            when (which) {
                0 -> sendPromptToGemini(basePrompt + "Дай подсказку, но не пиши код.", false)
                1 -> sendPromptToGemini(basePrompt + "Напиши полное решение с комментариями.", false)
            }
        }
        builder.show()
    }

    private fun sendPromptToGemini(prompt: String, isCheckTask: Boolean) {
        btnSubmitCode.isEnabled = false
        responseLayout.visibility = View.VISIBLE
        etCodeEditor.visibility = View.GONE
        btnSubmitCode.visibility = View.GONE

        tvResponseTitle.text = "Анализ..."
        tvResponseMessage.text = "Запрос отправлен..."

        val request = GeminiRequest(listOf(Content(listOf(Part(prompt)))))

        RetrofitClient.instance.generateContent(apiUrl, request, apiKey).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Нет ответа"

                    val cleanText = formatAiResponse(rawText)

                    tvResponseMessage.text = cleanText
                    tvResponseTitle.text = "Ответ ментора"

                    if (isCheckTask && (rawText.contains("ВЕРНО", ignoreCase = true) ||
                                rawText.contains("Correct", ignoreCase = true))) {

                        tvResponseTitle.text = "УСПЕХ! (+XP)"

                        val diff = intent.getStringExtra("TASK_DIFFICULTY") ?: ""
                        val xpAward = when {
                            diff.contains("Легк") -> 10
                            diff.contains("Норм") || diff.contains("Сред") -> 25
                            diff.contains("Слож") || diff.contains("Тяжел") -> 40
                            diff.contains("PRO") -> 60
                            else -> 15
                        }

                        saveSuccessToHistory(cleanText, xpAward)
                        Toast.makeText(applicationContext, "Вы получили $xpAward XP!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    tvResponseMessage.text = "Ошибка: ${response.code()}"
                }
                btnSubmitCode.isEnabled = true
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                tvResponseMessage.text = "Ошибка сети: ${t.message}"
                btnSubmitCode.isEnabled = true
            }
        })
    }

    private fun formatAiResponse(text: String): String {
        var result = text

        result = result.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        result = result.replace("*", "") // Убираем оставшиеся одиночные звездочки
        result = result.replace("#", "") // Убираем заголовки Markdown

        if (result.contains("```")) {
            result = result.replace("```cpp", "\n----- КОД -----\n")
            result = result.replace("```nasm", "\n----- КОД -----\n")
            result = result.replace("```", "\n---------------\n")
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