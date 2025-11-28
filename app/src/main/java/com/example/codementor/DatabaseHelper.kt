package com.example.codementor

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "codementor.db"
private const val DATABASE_VERSION = 6

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE Users (id INTEGER PRIMARY KEY, email TEXT UNIQUE, password TEXT)")
        db?.execSQL("CREATE TABLE Difficulties (id INTEGER PRIMARY KEY, name TEXT UNIQUE)")
        db?.execSQL("CREATE TABLE Languages (id INTEGER PRIMARY KEY, name TEXT UNIQUE)")
        db?.execSQL("CREATE TABLE Tasks (id INTEGER PRIMARY KEY, language_id INTEGER, difficulty_id INTEGER, title TEXT, description TEXT, FOREIGN KEY(language_id) REFERENCES Languages(id), FOREIGN KEY(difficulty_id) REFERENCES Difficulties(id))")

        db?.execSQL("CREATE TABLE History (id INTEGER PRIMARY KEY AUTOINCREMENT, task_title TEXT, description TEXT, solution TEXT, language TEXT, difficulty TEXT, timestamp LONG, is_solved INTEGER, xp_earned INTEGER DEFAULT 0)")

        db?.execSQL("CREATE TABLE ChatMessages (id INTEGER PRIMARY KEY AUTOINCREMENT, isUser INTEGER, message TEXT, timestamp LONG)")

        populateInitialData(db)
    }

    private fun populateInitialData(db: SQLiteDatabase?) {
        db?.execSQL("INSERT INTO Languages (id, name) VALUES (1, 'C++'), (2, 'NASM');")
        db?.execSQL("INSERT INTO Difficulties (id, name) VALUES (1, 'Легкий'), (2, 'Нормальный'), (3, 'Средний'), (4, 'Профессиональный');")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS Tasks")
        db?.execSQL("DROP TABLE IF EXISTS Languages")
        db?.execSQL("DROP TABLE IF EXISTS Difficulties")
        db?.execSQL("DROP TABLE IF EXISTS History")
        db?.execSQL("DROP TABLE IF EXISTS ChatMessages")
        onCreate(db)
    }

    fun saveToHistory(title: String, description: String, solution: String, language: String, difficulty: String, isSolved: Boolean, xp: Int) {
        val db = writableDatabase

        val checkCursor = db.rawQuery("SELECT id FROM History WHERE task_title = ? AND description = ?", arrayOf(title, description))

        val values = ContentValues().apply {
            put("task_title", title)
            put("description", description)
            put("solution", solution)
            put("language", language)
            put("difficulty", difficulty)
            put("is_solved", if (isSolved) 1 else 0)
            put("xp_earned", xp)
            put("timestamp", System.currentTimeMillis())
        }

        if (checkCursor.moveToFirst()) {
            val id = checkCursor.getInt(0)
            db.update("History", values, "id = ?", arrayOf(id.toString()))
        } else {
            db.insert("History", null, values)
        }

        checkCursor.close()
        db.close()
    }

    fun deleteFromHistory(id: Int) {
        val db = writableDatabase
        db.delete("History", "id = ?", arrayOf(id.toString()))
        db.close()
    }
    fun clearAllData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM History")
        db.execSQL("DELETE FROM ChatMessages")
        db.close()
    }

    fun getTotalXP(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM(xp_earned) FROM History", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun countSolvedTasks(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM History WHERE is_solved = 1", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun saveChatMessage(message: String, isUser: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("message", message)
            put("isUser", if (isUser) 1 else 0)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("ChatMessages", null, values)
        db.close()
    }

    fun getChatMessages(): List<ChatMessage> {
        val list = ArrayList<ChatMessage>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT message, isUser FROM ChatMessages ORDER BY id ASC", null)
        if (cursor.moveToFirst()) {
            do {
                val text = cursor.getString(0)
                val isUserInt = cursor.getInt(1)
                list.add(ChatMessage(text, isUserInt == 1))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}