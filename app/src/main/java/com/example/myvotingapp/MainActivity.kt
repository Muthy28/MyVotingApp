package com.example.myvotingapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myvotingapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: SectionCandidateAdapter
    private var loggedInVoterId: String = ""
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the logged-in voter ID from intent
        loggedInVoterId = intent.getStringExtra("LOGGED_IN_VOTER_ID") ?: ""

        db = AppDatabase.getDatabase(this)

        setupUI()
        setupBottomNavigation()
        setupSearchFunctionality()

        // Load home by default
        showHomeContent()
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Cancel previous search job
                searchJob?.cancel()

                val query = s.toString().trim()
                if (binding.homeContent.visibility == View.VISIBLE) {
                    filterCandidates(query)
                }
            }
        })
    }

    private fun filterCandidates(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                combine(
                    db.positionDao().getAllPositionsFlow(),
                    db.candidateDao().getAllCandidatesFlow()
                ) { positions, candidates ->
                    // Create a list with headers and candidates grouped by position
                    val items = mutableListOf<ListItem>()

                    positions.forEach { position ->
                        val positionCandidates = candidates.filter { candidate ->
                            candidate.positionId == position.positionId &&
                                    (candidate.name.contains(query, ignoreCase = true) ||
                                            candidate.manifesto.contains(query, ignoreCase = true) ||
                                            query.isEmpty())
                        }

                        // Only add header if there are candidates for this position after filtering
                        if (positionCandidates.isNotEmpty()) {
                            items.add(ListItem.Header(position.name))

                            // Add filtered candidates for this position
                            positionCandidates.forEach { candidate ->
                                items.add(ListItem.CandidateItem(candidate))
                            }
                        }
                    }

                    items
                }.collect { items ->
                    // Only update if home content is still visible and job is active
                    if (binding.homeContent.visibility == View.VISIBLE && isActive) {
                        adapter.submitList(items)
                    }
                }
            } catch (e: Exception) {
                // Only show error for non-cancellation exceptions and if activity is still active
                if (e !is kotlinx.coroutines.CancellationException &&
                    binding.homeContent.visibility == View.VISIBLE &&
                    !isFinishing) {
                    android.widget.Toast.makeText(this@MainActivity, "Error filtering candidates", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
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

        // Load initial data
        filterCandidates("")
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
        // Cancel any ongoing search operations
        searchJob?.cancel()

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
        // Cancel any ongoing search operations
        searchJob?.cancel()

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
        // Cancel any ongoing search operations
        searchJob?.cancel()

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
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all ongoing jobs when activity is destroyed
        searchJob?.cancel()
    }
}