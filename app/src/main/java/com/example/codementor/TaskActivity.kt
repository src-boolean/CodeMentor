package com.example.codementor

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class TaskActivity : AppCompatActivity() {

    private lateinit var tvTaskDescription: TextView
    private lateinit var etCodeEditor: EditText
    private lateinit var btnSubmitCode: Button

    private lateinit var responseLayout: LinearLayout
    private lateinit var tvResponseMessage: TextView
    private lateinit var btnGoToChat: Button
    private lateinit var tvResponseTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val toolbar: Toolbar = findViewById(R.id.toolbar_task)
        setSupportActionBar(toolbar)
        val selectedLanguage = intent.getStringExtra("TASK_LANGUAGE") ?: ""
        val selectedDifficulty = intent.getStringExtra("TASK_DIFFICULTY") ?: ""
        supportActionBar?.title = "$selectedLanguage: $selectedDifficulty"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        tvTaskDescription = findViewById(R.id.tvTaskDescription)
        etCodeEditor = findViewById(R.id.etCodeEditor)
        btnSubmitCode = findViewById(R.id.btnSubmitCode)
        responseLayout = findViewById(R.id.responseLayout)
        tvResponseMessage = findViewById(R.id.tvResponseMessage)
        btnGoToChat = findViewById(R.id.btnGoToChat)
        tvResponseTitle = findViewById(R.id.tvResponseTitle)

        val description = intent.getStringExtra("TASK_DESCRIPTION")
        tvTaskDescription.text = "Задача: $description"

        btnSubmitCode.setOnClickListener {
            handleSubmission()
        }

        btnGoToChat.setOnClickListener {
            Toast.makeText(this, "Переход в чат в разработке", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSubmission() {
        val userCode = etCodeEditor.text.toString()
        if (userCode.isBlank()) {
            Toast.makeText(this, "Введите код", Toast.LENGTH_SHORT).show()
            return
        }

        etCodeEditor.visibility = View.GONE
        btnSubmitCode.visibility = View.GONE
        tvTaskDescription.visibility = View.GONE

        responseLayout.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({

            val isSuccess = false

            if (isSuccess) {
                tvResponseTitle.text = "Молодец! Ты справился!"
                tvResponseMessage.text = "Код выглядит довольно хорошо! Но можно было еще лучше! Могу показать тебе более улучшенную версию."
            } else {
                tvResponseTitle.text = "Ты молодец! Но в коде есть ошибки"
                tvResponseMessage.text = "В коде есть ошибки, но ничего! Давай я тебе помогу их исправить и объяснить, где ты ошибся!"
            }
        }, 1500)
    }
}