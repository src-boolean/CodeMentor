package com.example.codementor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        rvHistory = view.findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, task_title, description, solution, language, difficulty, is_solved FROM History ORDER BY id DESC", null)
        val list = ArrayList<HistoryItem>()

        if (cursor.moveToFirst()) {
            do {
                list.add(HistoryItem(
                    cursor.getInt(0),    // id
                    cursor.getString(1), // title
                    cursor.getString(2), // description
                    cursor.getString(3) ?: "", // solution
                    cursor.getString(4), // language
                    cursor.getString(5),  // difficulty
                    cursor.getInt(6) == 1 // is_solved
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        val adapter = HistoryAdapter(list,
            onClick = { item ->
                val intent = Intent(context, TaskActivity::class.java)
                intent.putExtra("TASK_TITLE", item.title)
                intent.putExtra("TASK_DESCRIPTION", item.description)
                intent.putExtra("TASK_LANGUAGE", item.lang)
                intent.putExtra("TASK_DIFFICULTY", item.diff)
                intent.putExtra("TASK_SOLUTION", item.solution)
                startActivity(intent)
            },
            onLongClick = { item ->
                showDeleteDialog(item)
            }
        )
        rvHistory.adapter = adapter
    }

    private fun showDeleteDialog(item: HistoryItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить из истории?")
            .setMessage("Задача \"${item.title}\" будет удалена.")
            .setPositiveButton("Удалить") { _, _ ->
                val dbHelper = DatabaseHelper(requireContext())
                dbHelper.deleteFromHistory(item.id)
                loadHistory()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}