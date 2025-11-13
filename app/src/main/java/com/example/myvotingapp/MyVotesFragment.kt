package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myvotingapp.databinding.FragmentMyVotesBinding
import com.example.myvotingapp.databinding.ItemMyVoteBinding
import com.example.myvotingapp.databinding.ItemSectionHeaderVoteBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MyVotesFragment : Fragment() {

    private var _binding: FragmentMyVotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var loggedInVoterId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyVotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get logged in voter ID from arguments
        loggedInVoterId = arguments?.getString("LOGGED_IN_VOTER_ID") ?: ""

        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        loadMyVotes()
    }

    private fun setupRecyclerView() {
        val adapter = MyVotesAdapter()
        binding.rvMyVotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyVotes.adapter = adapter
    }

    private fun loadMyVotes() {
        lifecycleScope.launch {
            // Combine votes, candidates, and positions to get complete vote information
            combine(
                db.voteDao().getVotesForVoterFlow(loggedInVoterId),
                db.candidateDao().getAllCandidatesFlow(),
                db.positionDao().getAllPositionsFlow()
            ) { votes, candidates, positions ->
                if (votes.isEmpty()) {
                    // User hasn't voted yet
                    showNoVotesMessage()
                    emptyList()
                } else {
                    // User has voted - create list items
                    hideNoVotesMessage()
                    createVoteListItems(votes, candidates, positions)
                }
            }.collect { items ->
                if (items.isNotEmpty()) {
                    (binding.rvMyVotes.adapter as MyVotesAdapter).submitList(items)
                }
            }
        }
    }

    private fun createVoteListItems(
        votes: List<Vote>,
        candidates: List<Candidate>,
        positions: List<Position>
    ): List<MyVoteListItem> {
        val items = mutableListOf<MyVoteListItem>()

        // Group votes by position
        val votesByPosition = votes.groupBy { it.positionId }

        positions.forEach { position ->
            val positionVotes = votesByPosition[position.positionId]
            if (!positionVotes.isNullOrEmpty()) {
                // Add header for this position
                items.add(MyVoteListItem.Header(position))

                // Add the voted candidate for this position
                positionVotes.forEach { vote ->
                    val candidate = candidates.find { it.candidateId == vote.candidateId }
                    candidate?.let {
                        items.add(MyVoteListItem.VoteItem(it, position))
                    }
                }
            }
        }

        return items
    }

    private fun showNoVotesMessage() {
        binding.tvNoVotes.visibility = View.VISIBLE
        binding.rvMyVotes.visibility = View.GONE
    }

    private fun hideNoVotesMessage() {
        binding.tvNoVotes.visibility = View.GONE
        binding.rvMyVotes.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for displaying user's votes
    private inner class MyVotesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items = listOf<MyVoteListItem>()

        fun submitList(newItems: List<MyVoteListItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is MyVoteListItem.Header -> TYPE_HEADER
                is MyVoteListItem.VoteItem -> TYPE_VOTE_ITEM
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
                TYPE_VOTE_ITEM -> {
                    val binding = ItemMyVoteBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    VoteItemViewHolder(binding)
                }
                else -> throw IllegalArgumentException("Unknown view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is HeaderViewHolder -> {
                    val header = items[position] as MyVoteListItem.Header
                    holder.bind(header.position.name)
                }
                is VoteItemViewHolder -> {
                    val voteItem = items[position] as MyVoteListItem.VoteItem
                    holder.bind(voteItem.candidate, voteItem.position)
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

        private inner class VoteItemViewHolder(
            private val binding: ItemMyVoteBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(candidate: Candidate, position: Position) {
                binding.tvCandidateName.text = candidate.name
                binding.tvPosition.text = position.name
                binding.tvVoteStatus.text = "✓ Voted"

                // Load candidate image
                loadCandidateImage(candidate)
            }

            private fun loadCandidateImage(candidate: Candidate) {
                try {
                    if (!candidate.imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(candidate.imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivCandidate)
                    } else {
                        binding.ivCandidate.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } catch (e: Exception) {
                    binding.ivCandidate.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_VOTE_ITEM = 1

        fun newInstance(voterId: String): MyVotesFragment {
            return MyVotesFragment().apply {
                arguments = Bundle().apply {
                    putString("LOGGED_IN_VOTER_ID", voterId)
                }
            }
        }
    }
}

// Sealed class for my votes list items
sealed class MyVoteListItem {
    data class Header(val position: Position) : MyVoteListItem()
    data class VoteItem(val candidate: Candidate, val position: Position) : MyVoteListItem()
}