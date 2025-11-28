package com.example.codementor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val homeFragment = HomeFragment()
                    openFragment(homeFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_ai -> {
                    val aiChatFragment = AiChatFragment()
                    openFragment(aiChatFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_practice -> {
                    startActivity(Intent(this, TaskSelectionActivity::class.java))
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_history -> {
                    val historyFragment = HistoryFragment()
                    openFragment(historyFragment)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainRoot = findViewById<android.view.ViewGroup>(R.id.main_root)
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navView.setOnItemSelectedListener(onNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            val homeFragment = HomeFragment()
            openFragment(homeFragment)
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }
}

// логин: user5@gmail.com
//пароль: main555