package com.example.codementor

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TaskSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val languageMap = mutableMapOf<String, Int>()
    private val difficultyMap = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_selection)

        dbHelper = DatabaseHelper(this)

        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val spinnerDifficulty = findViewById<Spinner>(R.id.spinnerDifficulty)
        val btnGenerateTask = findViewById<Button>(R.id.btnGenerateTask)

        loadSpinnerData(spinnerLanguage, "Languages", languageMap)
        loadSpinnerData(spinnerDifficulty, "Difficulties", difficultyMap)

        btnGenerateTask.setOnClickListener {
            val selectedLanguageName = spinnerLanguage.selectedItem.toString()
            val selectedDifficultyName = spinnerDifficulty.selectedItem.toString()

            val langId = languageMap[selectedLanguageName]
            val diffId = difficultyMap[selectedDifficultyName]

            if (langId != null && diffId != null) {
                findAndStartTask(langId, diffId)
            }
        }
    }

    private fun loadSpinnerData(spinner: Spinner, tableName: String, map: MutableMap<String, Int>) {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT id, name FROM $tableName", null)
        val items = ArrayList<String>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                items.add(name)
                map[name] = id
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
    }

    private fun findAndStartTask(languageId: Int, difficultyId: Int) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT title, description FROM Tasks WHERE language_id = ? AND difficulty_id = ? ORDER BY RANDOM() LIMIT 1",
            arrayOf(languageId.toString(), difficultyId.toString())
        )

        if (cursor.moveToFirst()) {
            val title = cursor.getString(0)
            val description = cursor.getString(1)

            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("TASK_TITLE", title)
            intent.putExtra("TASK_DESCRIPTION", description)
            startActivity(intent)

        } else {
            Toast.makeText(this, "Задачи с такими параметрами не найдены", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }
}