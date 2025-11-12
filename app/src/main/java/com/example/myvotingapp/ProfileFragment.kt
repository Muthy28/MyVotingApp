package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myvotingapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var loggedInVoterId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        // Get logged in voter ID from arguments or MainActivity
        loggedInVoterId = arguments?.getString("LOGGED_IN_VOTER_ID") ?: ""

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadVoterProfile()
        setupClickListeners()
    }

    private fun loadVoterProfile() {
        lifecycleScope.launch {
            if (loggedInVoterId.isNotEmpty()) {
                val voter = db.voterDao().getVoterById(loggedInVoterId)
                voter?.let {
                    binding.tvName.text = "${it.firstName} ${it.lastName}"
                    binding.tvFirstName.text = it.firstName
                    binding.tvLastName.text = it.lastName
                    binding.tvMobile.text = it.mobile
                    binding.tvPassword.text = "xxxxxxxx" // Always show as xxxxxxxx for security
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
        }

        binding.btnChangePassword.setOnClickListener {
            // TODO: Implement change password functionality
        }

        binding.btnLogout.setOnClickListener {
            // Navigate back to login screen
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}