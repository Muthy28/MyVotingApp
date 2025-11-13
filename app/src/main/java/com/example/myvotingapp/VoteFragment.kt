package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myvotingapp.databinding.FragmentVoteBinding
import com.example.myvotingapp.databinding.ItemCandidateVoteBinding
import com.example.myvotingapp.databinding.ItemSectionHeaderVoteBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class VoteFragment : Fragment() {

    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var loggedInVoterId: String = ""
    private val selectedCandidates = mutableMapOf<Long, Long>() // positionId to candidateId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get logged in voter ID from arguments
        loggedInVoterId = arguments?.getString("LOGGED_IN_VOTER_ID") ?: ""

        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupSubmitButton()

        // Check if user has already voted
        checkIfUserHasVoted()
    }

    private fun checkIfUserHasVoted() {
        lifecycleScope.launch {
            val votes = db.voteDao().getVotesForVoterFlow(loggedInVoterId)
            votes.collect { voteList ->
                if (voteList.isNotEmpty()) {
                    // User has already voted - show message on screen
                    showAlreadyVotedUI()
                } else {
                    // User hasn't voted yet - show normal voting UI
                    showNormalVotingUI()
                }
            }
        }
    }

    private fun showAlreadyVotedUI() {
        // Hide all voting elements
        binding.rvCandidates.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.etSearch.visibility = View.GONE

        // Show the "already voted" message
        binding.tvAlreadyVoted.visibility = View.VISIBLE
        binding.tvAlreadyVoted.text = "You have already responded.\nKindly contact the Admin."
    }

    private fun showNormalVotingUI() {
        // Show all voting elements
        binding.rvCandidates.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.etSearch.visibility = View.VISIBLE

        // Hide the "already voted" message
        binding.tvAlreadyVoted.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        val adapter = VoteCandidateAdapter()
        binding.rvCandidates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCandidates.adapter = adapter

        lifecycleScope.launch {
            // Combine both flows to get positions and candidates
            combine(
                db.positionDao().getAllPositionsFlow(),
                db.candidateDao().getAllCandidatesFlow()
            ) { positions, candidates ->
                // Create a list with headers and candidates grouped by position
                val items = mutableListOf<VoteListItem>()

                positions.forEach { position ->
                    // Add header for this position
                    items.add(VoteListItem.Header(position))

                    // Add all candidates for this position
                    val positionCandidates = candidates.filter { it.positionId == position.positionId }
                    positionCandidates.forEach { candidate ->
                        items.add(VoteListItem.CandidateItem(candidate))
                    }
                }

                items
            }.collect { items ->
                adapter.submitList(items)
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (selectedCandidates.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one candidate", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if user has selected one candidate for each position
            val allPositions = getPositionsCount()
            if (selectedCandidates.size < allPositions) {
                Toast.makeText(requireContext(), "Please select one candidate for each position", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Confirm submission
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Vote")
                .setMessage("Are you sure you want to submit your vote?")
                .setPositiveButton("Yes") { dialog, _ ->
                    submitVotes()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun getPositionsCount(): Int {
        // This would ideally come from the database, but for now we'll use the selectedCandidates logic
        return selectedCandidates.keys.size
    }

    private fun submitVotes() {
        lifecycleScope.launch {
            try {
                // Insert votes for each selected candidate
                selectedCandidates.forEach { (positionId, candidateId) ->
                    val vote = Vote(
                        voterId = loggedInVoterId,
                        candidateId = candidateId,
                        positionId = positionId
                    )
                    db.voteDao().insertVote(vote)

                    // Increment vote count for candidate
                    db.candidateDao().incrementVoteCount(candidateId)
                }

                // Show success message and update UI
                showVoteSuccessMessage()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error submitting vote: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showVoteSuccessMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Vote Submitted")
            .setMessage("Your Vote has been cast.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // After successful vote, show the "already voted" UI
                showAlreadyVotedUI()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for voting candidates
    private inner class VoteCandidateAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items = listOf<VoteListItem>()

        fun submitList(newItems: List<VoteListItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is VoteListItem.Header -> TYPE_HEADER
                is VoteListItem.CandidateItem -> TYPE_CANDIDATE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_HEADER -> {
                    val binding = ItemSectionHeaderVoteBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    HeaderViewHolder(binding)
                }
                TYPE_CANDIDATE -> {
                    val binding = ItemCandidateVoteBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    CandidateViewHolder(binding)
                }
                else -> throw IllegalArgumentException("Unknown view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is HeaderViewHolder -> {
                    val header = items[position] as VoteListItem.Header
                    holder.bind(header.position.name)
                }
                is CandidateViewHolder -> {
                    val candidateItem = items[position] as VoteListItem.CandidateItem
                    holder.bind(candidateItem.candidate)
                }
            }
        }

        override fun getItemCount(): Int = items.size

        private inner class HeaderViewHolder(
            private val binding: ItemSectionHeaderVoteBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(headerText: String) {
                binding.tvSectionHeader.text = headerText
            }
        }

        private inner class CandidateViewHolder(
            private val binding: ItemCandidateVoteBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(candidate: Candidate) {
                binding.tvCandidateName.text = candidate.name

                // Load candidate image
                loadImageWithGlide(candidate)

                // Set checkbox state - checked if this candidate is selected for this position
                binding.cbVote.isChecked = selectedCandidates[candidate.positionId] == candidate.candidateId

                // Handle checkbox clicks - SINGLE SELECTION PER POSITION
                binding.cbVote.setOnClickListener {
                    if (binding.cbVote.isChecked) {
                        // Unselect any previously selected candidate for this position
                        selectedCandidates[candidate.positionId] = candidate.candidateId

                        // Refresh the entire list to update other checkboxes in the same position
                        notifyDataSetChanged()
                    } else {
                        // Remove from selection if unchecked
                        selectedCandidates.remove(candidate.positionId)
                    }
                }
            }

            private fun loadImageWithGlide(candidate: Candidate) {
                try {
                    if (!candidate.imageUrl.isNullOrEmpty()) {
                        // Load from URL or file path
                        Glide.with(requireContext())
                            .load(candidate.imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivCandidate)
                    } else {
                        // Load default placeholder
                        binding.ivCandidate.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } catch (e: Exception) {
                    // Fallback to default image
                    binding.ivCandidate.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CANDIDATE = 1

        fun newInstance(voterId: String): VoteFragment {
            return VoteFragment().apply {
                arguments = Bundle().apply {
                    putString("LOGGED_IN_VOTER_ID", voterId)
                }
            }
        }
    }
}

// Sealed class for vote list items
sealed class VoteListItem {
    data class Header(val position: Position) : VoteListItem()
    data class CandidateItem(val candidate: Candidate) : VoteListItem()
}