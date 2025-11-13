package com.example.myvotingapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myvotingapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: SectionCandidateAdapter
    private var loggedInVoterId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the logged-in voter ID from intent
        loggedInVoterId = intent.getStringExtra("LOGGED_IN_VOTER_ID") ?: ""

        db = AppDatabase.getDatabase(this)

        setupUI()
        setupBottomNavigation()

        // Load home by default
        showHomeContent()
    }

    private fun setupUI() {
        // Set default welcome message first
        binding.tvWelcome.text = "Welcome, Voter"

        // Welcome message with actual voter name
        lifecycleScope.launch {
            try {
                val voter = if (loggedInVoterId.isNotEmpty()) {
                    db.voterDao().getVoterById(loggedInVoterId)
                } else {
                    null
                }

                voter?.let {
                    binding.tvWelcome.text = "Welcome, ${it.firstName} ${it.lastName}"
                }
            } catch (e: Exception) {
                // Keep default welcome message if error
                binding.tvWelcome.text = "Welcome, Voter"
            }
        }

        // RecyclerView for candidates with sections
        adapter = SectionCandidateAdapter()
        binding.rvCandidates.layoutManager = LinearLayoutManager(this)
        binding.rvCandidates.adapter = adapter

        // Fetch candidates and positions, then group them
        lifecycleScope.launch {
            // Combine both flows to get positions and candidates
            combine(
                db.positionDao().getAllPositionsFlow(),
                db.candidateDao().getAllCandidatesFlow()
            ) { positions, candidates ->
                // Create a list with headers and candidates grouped by position
                val items = mutableListOf<ListItem>()

                positions.forEach { position ->
                    // Add header for this position
                    items.add(ListItem.Header(position.name))

                    // Add all candidates for this position
                    val positionCandidates = candidates.filter { it.positionId == position.positionId }
                    positionCandidates.forEach { candidate ->
                        items.add(ListItem.CandidateItem(candidate))
                    }
                }

                items
            }.collect { items ->
                adapter.submitList(items)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeContent()
                    true
                }
                R.id.nav_votes -> {
                    showVoteFragment()
                    true
                }
                R.id.nav_my_votes -> {
                    showMyVotesFragment()
                    true
                }
                R.id.nav_profile -> {
                    showProfileFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun showHomeContent() {
        // Clear any fragments
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Make home content visible and hide fragment container
        binding.homeContent.visibility = View.VISIBLE
        binding.fragmentContainerView.visibility = View.GONE
    }

    private fun showVoteFragment() {
        // Hide home content and show fragment container
        binding.homeContent.visibility = View.GONE
        binding.fragmentContainerView.visibility = View.VISIBLE

        // Create and show VoteFragment
        val voteFragment = VoteFragment.newInstance(loggedInVoterId)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, voteFragment)
            addToBackStack("vote")
        }
    }

    private fun showMyVotesFragment() {
        // Hide home content and show fragment container
        binding.homeContent.visibility = View.GONE
        binding.fragmentContainerView.visibility = View.VISIBLE

        // Create and show MyVotesFragment
        val myVotesFragment = MyVotesFragment.newInstance(loggedInVoterId)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, myVotesFragment)
            addToBackStack("my_votes")
        }
    }

    private fun showProfileFragment() {
        // Hide home content and show fragment container
        binding.homeContent.visibility = View.GONE
        binding.fragmentContainerView.visibility = View.VISIBLE

        // Create and show ProfileFragment
        val profileFragment = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString("LOGGED_IN_VOTER_ID", loggedInVoterId)
            }
        }
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, profileFragment)
            addToBackStack("profile")
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            // If we're back to no fragments, show home content
            if (supportFragmentManager.backStackEntryCount == 0) {
                showHomeContent()
                binding.bottomNav.selectedItemId = R.id.nav_home
            }
        } else {
            super.onBackPressed()
        }
    }
}