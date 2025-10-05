package com.example.codementor

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "codementor.db"
private const val DATABASE_VERSION = 1

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = "CREATE TABLE Users (id INTEGER PRIMARY KEY, email TEXT UNIQUE, password TEXT)"
        val createDifficultiesTable = "CREATE TABLE Difficulties (id INTEGER PRIMARY KEY, name TEXT UNIQUE)"
        val createLanguagesTable = "CREATE TABLE Languages (id INTEGER PRIMARY KEY, name TEXT UNIQUE)"
        val createTasksTable = "CREATE TABLE Tasks (id INTEGER PRIMARY KEY, language_id INTEGER, difficulty_id INTEGER, title TEXT, description TEXT)"

        db?.execSQL(createUsersTable)
        db?.execSQL(createDifficultiesTable)
        db?.execSQL(createLanguagesTable)
        db?.execSQL(createTasksTable)

        populateInitialData(db)
    }

    private fun populateInitialData(db: SQLiteDatabase?) {
        db?.execSQL("INSERT INTO Languages (id, name) VALUES (1, 'C++'), (2, 'NASM');")
        db?.execSQL("INSERT INTO Difficulties (id, name) VALUES (1, 'Easy'), (2, 'Normal'), (3, 'Hard');")
        db?.execSQL("INSERT INTO Tasks (id, language_id, difficulty_id, title, description) VALUES (1, 1, 1, 'Сумма двух чисел', 'Напишите программу на C++...');")
        db?.execSQL("INSERT INTO Tasks (id, language_id, difficulty_id, title, description) VALUES (2, 2, 3, 'Hello, World! на NASM', 'Напишите программу на NASM...');")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS Tasks")
        db?.execSQL("DROP TABLE IF EXISTS Languages")
        db?.execSQL("DROP TABLE IF EXISTS Difficulties")
        onCreate(db)
    }
}