package com.example.myvotingapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var arrowIcon: ImageView
    private lateinit var manageMenu: LinearLayout
    private var isExpanded = false

    private lateinit var btnDashboard: Button
    private lateinit var btnVotes: Button
    private lateinit var btnPositions: Button
    private lateinit var btnCandidates: Button
    private lateinit var btnElectionTitle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        arrowIcon = findViewById(R.id.arrowIcon)
        manageMenu = findViewById(R.id.manageMenu)
        val dropdown = findViewById<LinearLayout>(R.id.manageDropdown)

        dropdown.setOnClickListener {
            if (isExpanded) {
                manageMenu.visibility = View.GONE
                arrowIcon.rotation = 0f
            } else {
                manageMenu.visibility = View.VISIBLE
                arrowIcon.rotation = 180f
            }
            isExpanded = !isExpanded
        }

        btnDashboard = findViewById(R.id.btnDashboard)
        btnVotes = findViewById(R.id.btnVotes)
        btnPositions = findViewById(R.id.btnPositions)
        btnCandidates = findViewById(R.id.btnCandidates)
        btnElectionTitle = findViewById(R.id.btnElectionTitle)

        val fragmentContainer = findViewById<View>(R.id.fragmentContainer)
        val dashboardContainer = findViewById<LinearLayout>(R.id.dashboardContainer)

        // Show PositionsFragment
        btnPositions.setOnClickListener {
            dashboardContainer.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, PositionsFragment())
            }

            manageMenu.visibility = View.GONE
            arrowIcon.rotation = 0f
            isExpanded = false
        }

        // Show Dashboard again
        btnDashboard.setOnClickListener {
            dashboardContainer.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE

            manageMenu.visibility = View.GONE
            arrowIcon.rotation = 0f
            isExpanded = false
        }

        btnCandidates.setOnClickListener {
            dashboardContainer.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, CandidatesFragment())
            }

            manageMenu.visibility = View.GONE
            arrowIcon.rotation = 0f
            isExpanded = false
        }

        // Other buttons can still handle fragments or actions
    }
}
