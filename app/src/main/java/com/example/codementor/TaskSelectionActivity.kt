package com.example.codementor

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskSelectionActivity : AppCompatActivity() {

    private lateinit var rvTopics: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var fabCustomTopic: FloatingActionButton
    private lateinit var chipGroupDifficulty: ChipGroup

    private val apiKey = "key"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    private val topicsCpp = listOf(
        Topic("Введение", "Вывод в консоль, структура программы"),
        Topic("Переменные", "Типы данных int, float, string"),
        Topic("Условия", "If, else, switch"),
        Topic("Циклы", "For, while, do-while"),
        Topic("Массивы", "Одномерные и многомерные массивы"),
        Topic("Функции", "Аргументы, возврат значений"),
        Topic("Указатели", "Ссылки и работа с памятью"),
        Topic("OOP", "Классы и объекты"),
        Topic("STL: Векторы", "std::vector, push_back"),
        Topic("Файлы", "Чтение и запись файлов")
    )

    private val topicsNasm = listOf(
        Topic("Основы", "Регистры, mov"),
        Topic("Арифметика", "Add, sub, mul, div"),
        Topic("Стек", "Push, pop"),
        Topic("Переходы", "Jmp, cmp, условные переходы"),
        Topic("Системные вызовы", "Linux syscalls (sys_write, sys_exit)"),
        Topic("Циклы", "Реализация циклов на метках")
    )

    private var currentLanguage = "C++"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_selection)

        val mainContainer = findViewById<android.view.ViewGroup>(R.id.main_container)
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        rvTopics = findViewById(R.id.rvTopics)
        tabLayout = findViewById(R.id.tabLayout)
        fabCustomTopic = findViewById(R.id.fabCustomTopic)
        chipGroupDifficulty = findViewById(R.id.chipGroupDifficulty)

        rvTopics.layoutManager = LinearLayoutManager(this)

        updateList("C++")

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentLanguage = tab?.text.toString()
                updateList(currentLanguage)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        fabCustomTopic.setOnClickListener {
            showCustomTopicDialog()
        }
    }


    private fun getSelectedDifficulty(): String {
        val chipId = chipGroupDifficulty.checkedChipId
        return if (chipId != -1) {
            val chip = findViewById<Chip>(chipId)
            chip.text.toString()
        } else {
            "Средний"
        }
    }

    private fun updateList(language: String) {
        val currentList = if (language == "C++") topicsCpp else topicsNasm
        rvTopics.adapter = TopicAdapter(currentList) { topic ->
            val difficulty = getSelectedDifficulty()
            generateTaskByTopic(language, topic.name, difficulty)
        }
    }

    private fun showCustomTopicDialog() {
        val input = EditText(this)
        val padding = (16 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding, padding, padding)
        input.hint = "Например: STL Map или QuickSort..."

        val container = FrameLayout(this)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Своя тема")
            .setMessage("Введите тему для практики ($currentLanguage):")
            .setView(container)
            .setPositiveButton("Создать") { _, _ ->
                val customTopic = input.text.toString()
                if (customTopic.isNotBlank()) {
                    // Передаем сложность и для кастомной темы
                    val difficulty = getSelectedDifficulty()
                    generateTaskByTopic(currentLanguage, customTopic, difficulty)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun generateTaskByTopic(language: String, topic: String, difficulty: String) {
        Toast.makeText(this, "Создаю задачу ($difficulty): $topic...", Toast.LENGTH_LONG).show()

        val prompt = "Придумай практическую задачу по программированию на языке $language. " +
                "Тема: '$topic'. " +
                "Сложность: $difficulty. " +
                "Ответ дай строго одной строкой в формате: " +
                "ЗАГОЛОВОК: Название задачи ||| ОПИСАНИЕ: Полный текст задания."

        val request = GeminiRequest(listOf(Content(listOf(Part(prompt)))))

        RetrofitClient.instance.generateContent(apiUrl, request, apiKey).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val aiText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (aiText != null && aiText.contains("|||")) {
                        val parts = aiText.split("|||")
                        var title = parts[0].replace("ЗАГОЛОВОК:", "").trim()
                        val desc = parts[1].replace("ОПИСАНИЕ:", "").trim()
                        title = title.replace("*", "")

                        val cleanDesc = desc.replace("*", "")

                        val intent = Intent(this@TaskSelectionActivity, TaskActivity::class.java)
                        intent.putExtra("TASK_TITLE", title)
                        intent.putExtra("TASK_DESCRIPTION", cleanDesc)
                        intent.putExtra("TASK_LANGUAGE", language)
                        intent.putExtra("TASK_DIFFICULTY", difficulty)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Формат ответа AI некорректен", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }
}