package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myvotingapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: CandidateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Welcome message
        lifecycleScope.launch {
            val voter = db.voterDao().getVoterById("12345678") // example for testing
            voter?.let {
                binding.tvWelcome.text = "Welcome, ${it.firstName}"
            }
        }

        // RecyclerView for candidates
        adapter = CandidateAdapter()
        binding.rvCandidates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCandidates.adapter = adapter

        // Fetch all candidates
        lifecycleScope.launch {
            db.candidateDao().getAllCandidatesFlow().collectLatest { candidates ->
                adapter.submitList(candidates)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
