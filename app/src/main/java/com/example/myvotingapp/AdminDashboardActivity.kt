package com.example.myvotingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupBottomNavigation()

        // Load home by default
        showHomeFragment()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeFragment()
                    true
                }
                R.id.nav_candidates -> {
                    showCandidatesFragment()
                    true
                }
                R.id.nav_positions -> {
                    showPositionsFragment()
                    true
                }
                R.id.nav_profile -> {
                    showProfileFragment()
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.menu?.let { menu ->
            for (i in 0 until menu.size()) {
                menu.getItem(i).isVisible = true
            }
        }
    }

    private fun showHomeFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, AdminHomeFragment())
            setReorderingAllowed(true)
        }
        supportActionBar?.title = "Admin Dashboard"
    }

    private fun showCandidatesFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CandidatesFragment())
            setReorderingAllowed(true)
        }
        supportActionBar?.title = "Manage Candidates"
    }

    private fun showPositionsFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, PositionsFragment())
            setReorderingAllowed(true)
        }
        supportActionBar?.title = "Manage Positions"
    }

    private fun showProfileFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, AdminProfileFragment())
            setReorderingAllowed(true)
        }
        supportActionBar?.title = "Admin Profile"
    }
}