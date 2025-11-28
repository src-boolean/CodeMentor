package com.example.codementor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val btnSettings = view.findViewById<ImageView>(R.id.btnSettings)

        val cardStartPractice = view.findViewById<CardView>(R.id.cardStartPractice)
        val tvXPAmount = view.findViewById<TextView>(R.id.tvXPAmount)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvUserRank = view.findViewById<TextView>(R.id.tvUserRank)

        btnSettings.setOnClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }

        cardStartPractice.setOnClickListener {
            startActivity(Intent(activity, TaskSelectionActivity::class.java))
        }

        val dbHelper = DatabaseHelper(requireContext())
        val totalXP = dbHelper.getTotalXP()
        dbHelper.close()

        tvXPAmount.text = totalXP.toString()

        val nextLevelXP = 500
        var progress = (totalXP * 100) / nextLevelXP
        if (progress > 100) progress = 100
        progressBar.progress = progress

        val rank = when {
            totalXP < 100 -> "Новичок"
            totalXP < 500 -> "Джуниор"
            totalXP < 1500 -> "Разработчик"
            totalXP < 5000 -> "Сеньор"
            else -> "Гуру кода"
        }
        tvUserRank.text = rank

        return view
    }

    override fun onResume() {
        super.onResume()
        val dbHelper = DatabaseHelper(requireContext())
        val totalXP = dbHelper.getTotalXP()
        dbHelper.close()

        view?.findViewById<TextView>(R.id.tvXPAmount)?.text = totalXP.toString()

        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        val nextLevelXP = 500
        var progress = (totalXP * 100) / nextLevelXP
        if (progress > 100) progress = 100
        progressBar?.progress = progress
    }
}