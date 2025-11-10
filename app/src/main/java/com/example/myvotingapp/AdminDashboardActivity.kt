package com.example.myvotingapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import android.view.View
import androidx.fragment.app.Fragment

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var dashboardView: View
    private lateinit var fragmentContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer setup
        drawerLayout = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        dashboardView = findViewById(R.id.dashboardView)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        // Sidebar button listeners
        findViewById<Button>(R.id.btnDashboard).setOnClickListener {
            showDashboard()
            drawerLayout.closeDrawers()
        }

        findViewById<Button>(R.id.btnVotes).setOnClickListener {
            showToast("Votes clicked")
        }

        findViewById<Button>(R.id.btnPositions).setOnClickListener {
            openFragment(PositionsFragment())
        }

        findViewById<Button>(R.id.btnCandidates).setOnClickListener {
            openFragment(CandidatesFragment())
        }

        findViewById<Button>(R.id.btnBallotPosition).setOnClickListener {
            showToast("Ballot Position clicked")
        }

        findViewById<Button>(R.id.btnElectionTitle).setOnClickListener {
            showToast("Election Title clicked")
        }

        // Print button on dashboard
        findViewById<Button>(R.id.btnPrint).setOnClickListener {
            showToast("Print pressed")
        }
    }

    // Show simple Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Open a fragment
    private fun openFragment(fragment: Fragment) {
        dashboardView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()

        drawerLayout.closeDrawers()
    }

    private fun showDashboard() {
        fragmentContainer.visibility = View.GONE
        dashboardView.visibility = View.VISIBLE
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // Handle back press
    override fun onBackPressed() {
        if (fragmentContainer.visibility == View.VISIBLE) {
            showDashboard()
        } else {
            super.onBackPressed()
        }
    }

}
