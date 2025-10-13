package com.example.codementor

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val tvTaskTitle = findViewById<TextView>(R.id.tvTaskTitle)
        val tvTaskDescription = findViewById<TextView>(R.id.tvTaskDescription)

        val title = intent.getStringExtra("TASK_TITLE")
        val description = intent.getStringExtra("TASK_DESCRIPTION")

        tvTaskTitle.text = title
        tvTaskDescription.text = description
    }
}