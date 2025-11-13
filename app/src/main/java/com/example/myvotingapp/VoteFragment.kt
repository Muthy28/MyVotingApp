package com.example.myvotingapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myvotingapp.databinding.FragmentVoteBinding
import com.example.myvotingapp.databinding.ItemCandidateVoteBinding
import com.example.myvotingapp.databinding.ItemSectionHeaderVoteBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VoteFragment : Fragment() {

    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var loggedInVoterId: String = ""
    private val selectedCandidates = mutableMapOf<Long, Long>() // positionId to candidateId
    private var totalPositionsCount = 0
    private lateinit var adapter: VoteCandidateAdapter
    private var searchJob: Job? = null
    private var checkVoteJob: Job? = null

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
        loggedInVoterId = arguments?.getString("LOGGED_IN_VOTER_ID") ?: run {
            Toast.makeText(requireContext(), "Error: Voter ID not found", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupSubmitButton()
        setupSearchFunctionality()

        // Check if user has already voted
        checkIfUserHasVoted()
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Cancel previous search job
                searchJob?.cancel()

                val query = s.toString().trim()
                if (binding.rvCandidates.visibility == View.VISIBLE) {
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
                    totalPositionsCount = positions.size
                    val filteredItems = mutableListOf<VoteListItem>()

                    positions.forEach { position ->
                        val positionCandidates = candidates.filter { candidate ->
                            candidate.positionId == position.positionId &&
                                    (candidate.name.contains(query, ignoreCase = true) ||
                                            candidate.manifesto.contains(query, ignoreCase = true) ||
                                            query.isEmpty())
                        }

                        // Only add header if there are candidates for this position after filtering
                        if (positionCandidates.isNotEmpty()) {
                            filteredItems.add(VoteListItem.Header(position))

                            // Add filtered candidates for this position
                            positionCandidates.forEach { candidate ->
                                filteredItems.add(VoteListItem.CandidateItem(candidate))
                            }
                        }
                    }

                    filteredItems
                }.collect { filteredItems ->
                    // Only update if RecyclerView is still visible and job is active
                    if (binding.rvCandidates.visibility == View.VISIBLE && isActive) {
                        adapter.submitList(filteredItems)
                    }
                }
            } catch (e: Exception) {
                // Only show error for non-cancellation exceptions and if fragment is still active
                if (e !is kotlinx.coroutines.CancellationException &&
                    binding.rvCandidates.visibility == View.VISIBLE &&
                    isAdded) {
                    Toast.makeText(requireContext(), "Error filtering candidates", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkIfUserHasVoted() {
        checkVoteJob = lifecycleScope.launch {
            try {
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
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error checking vote status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAlreadyVotedUI() {
        // Cancel any ongoing search operations
        searchJob?.cancel()

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
        adapter = VoteCandidateAdapter()
        binding.rvCandidates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCandidates.adapter = adapter

        // Load initial data
        filterCandidates("")
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (selectedCandidates.isEmpty()) {
                showCustomToast("Please select at least one candidate")
                return@setOnClickListener
            }

            // Check if user has selected one candidate for each position
            if (selectedCandidates.size < totalPositionsCount) {
                showCustomToast("Please select one candidate for each position")
                return@setOnClickListener
            }

            // Confirm submission with custom dialog
            showCustomConfirmDialog()
        }
    }

    private fun showCustomConfirmDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Vote")
            .setMessage("Are you sure you want to submit your vote?")
            .setPositiveButton("Yes") { dialog, _ ->
                submitVotes()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Apply custom styling after dialog is created
        dialog.setOnShowListener {
            // Set white background
            dialog.window?.setBackgroundDrawableResource(android.R.color.white)

            // Set title style
            val titleTextView = dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            titleTextView?.setTypeface(titleTextView.typeface, android.graphics.Typeface.BOLD)

            // Set message style
            val messageTextView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
            messageTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            messageTextView?.setTypeface(messageTextView.typeface, android.graphics.Typeface.BOLD)

            // Set button styles
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            positiveButton?.setTypeface(positiveButton.typeface, android.graphics.Typeface.BOLD)

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            negativeButton?.setTypeface(negativeButton.typeface, android.graphics.Typeface.BOLD)
        }

        dialog.show()
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
                if (isAdded) {
                    showCustomToast("Error submitting vote: ${e.message}")
                }
            }
        }
    }

    private fun showVoteSuccessMessage() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Vote Submitted")
            .setMessage("Your Vote has been cast.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // After successful vote, show the "already voted" UI
                showAlreadyVotedUI()
            }
            .setCancelable(false)
            .create()

        // Apply custom styling after dialog is created
        dialog.setOnShowListener {
            // Set white background
            dialog.window?.setBackgroundDrawableResource(android.R.color.white)

            // Set title style
            val titleTextView = dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            titleTextView?.setTypeface(titleTextView.typeface, android.graphics.Typeface.BOLD)

            // Set message style
            val messageTextView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
            messageTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            messageTextView?.setTypeface(messageTextView.typeface, android.graphics.Typeface.BOLD)

            // Set button style
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            positiveButton?.setTypeface(positiveButton.typeface, android.graphics.Typeface.BOLD)
        }

        dialog.show()
    }

    private fun showCustomToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel all ongoing jobs when fragment is destroyed
        searchJob?.cancel()
        checkVoteJob?.cancel()
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

                // Make checkbox outline more visible
                binding.cbVote.buttonTintList = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf(android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        ContextCompat.getColor(requireContext(), android.R.color.darker_gray),
                        ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                    )
                )
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