package com.example.myvotingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myvotingapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: CandidateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupUI()
        setupBottomNavigation()
    }

    private fun setupUI() {
        // Welcome message
        lifecycleScope.launch {
            val voter = db.voterDao().getVoterById("12345678") // example for testing
            voter?.let {
                binding.tvWelcome.text = "Welcome, ${it.firstName}"
            }
        }

        // RecyclerView for candidates
        adapter = CandidateAdapter()
        binding.rvCandidates.layoutManager = LinearLayoutManager(this)
        binding.rvCandidates.adapter = adapter

        // Fetch all candidates
        lifecycleScope.launch {
            db.candidateDao().getAllCandidatesFlow().collectLatest { candidates ->
                adapter.submitList(candidates)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, no need to change
                    true
                }
                R.id.nav_votes -> {
                    // TODO: Navigate to Votes screen
                    true
                }
                R.id.nav_my_votes -> {
                    // TODO: Navigate to MyVotes screen
                    true
                }
                R.id.nav_profile -> {
                    // TODO: Navigate to Profile screen
                    true
                }
                else -> false
            }
        }
    }
}