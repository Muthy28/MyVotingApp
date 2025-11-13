package com.example.myvotingapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            Toast.makeText(requireContext(), "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangePassword.setOnClickListener {
            // TODO: Implement change password functionality
            Toast.makeText(requireContext(), "Change Password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        binding.btnLogout.setOnClickListener {
            // Navigate back to login screen
            requireActivity().finish()
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently lost.")
            .setPositiveButton("Delete") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAccount() {
        lifecycleScope.launch {
            try {
                val voter = db.voterDao().getVoterById(loggedInVoterId)
                voter?.let {
                    // Delete the voter from database
                    db.voterDao().deleteVoter(it)

                    // Show success message
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_LONG).show()

                        // Navigate back to login screen
                        requireActivity().finish()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error deleting account: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}