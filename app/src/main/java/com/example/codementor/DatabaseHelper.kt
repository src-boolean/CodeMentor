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
        val createTasksTable = "CREATE TABLE Tasks (id INTEGER PRIMARY KEY, language_id INTEGER, difficulty_id INTEGER, title TEXT, description TEXT, FOREIGN KEY(language_id) REFERENCES Languages(id), FOREIGN KEY(difficulty_id) REFERENCES Difficulties(id))"

        db?.execSQL(createUsersTable)
        db?.execSQL(createDifficultiesTable)
        db?.execSQL(createLanguagesTable)
        db?.execSQL(createTasksTable)

        populateInitialData(db)
    }

    private fun populateInitialData(db: SQLiteDatabase?) {
        db?.execSQL("INSERT INTO Languages (id, name) VALUES (1, 'C++'), (2, 'NASM');")
        db?.execSQL("INSERT INTO Difficulties (id, name) VALUES (1, 'Легкий'), (2, 'Нормальный'), (3, 'Средний'), (4, 'Профессиональный');")

        db?.execSQL("INSERT INTO Tasks (language_id, difficulty_id, title, description) VALUES (1, 1, 'Сумма двух чисел', 'Напишите программу на C++, которая принимает два целых числа и выводит их сумму.');")
        db?.execSQL("INSERT INTO Tasks (language_id, difficulty_id, title, description) VALUES (1, 2, 'Факториал числа', 'Реализуйте функцию на C++ для вычисления факториала заданного числа.');")
        db?.execSQL("INSERT INTO Tasks (language_id, difficulty_id, title, description) VALUES (1, 3, 'Сортировка массива', 'Напишите программу на C++ для сортировки массива целых чисел методом пузырька.');")
        db?.execSQL("INSERT INTO Tasks (language_id, difficulty_id, title, description) VALUES (2, 1, 'Hello, World! на NASM', 'Напишите программу на NASM (синтаксис Linux x86-64), которая выводит в консоль строку \"Hello, World!\".');")
        db?.execSQL("INSERT INTO Tasks (language_id, difficulty_id, title, description) VALUES (2, 2, 'Сложение в NASM', 'Напишите программу на NASM, которая складывает два числа, заданных в регистрах, и сохраняет результат в третий регистр.');")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS Tasks")
        db?.execSQL("DROP TABLE IF EXISTS Languages")
        db?.execSQL("DROP TABLE IF EXISTS Difficulties")
        onCreate(db)
    }
}