package com.example.myvotingapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myvotingapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var loggedInVoterId: String = ""
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        loggedInVoterId = arguments?.getString("LOGGED_IN_VOTER_ID") ?: ""

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadVoterProfile()
        setupClickListeners()
        setupPasswordToggle()
    }

    private fun loadVoterProfile() {
        lifecycleScope.launch {
            if (loggedInVoterId.isNotEmpty()) {
                val voter = db.voterDao().getVoterById(loggedInVoterId)
                voter?.let {
                    updateUI(voter)
                }
            }
        }
    }

    private fun updateUI(voter: Voter) {
        binding.tvName.text = "${voter.firstName} ${voter.lastName}"
        binding.tvFirstName.text = voter.firstName
        binding.tvLastName.text = voter.lastName
        binding.tvMobile.text = voter.mobile
        binding.tvPassword.text = "xxxxxxxxxxx" // Show as xxxxxxxxxxx in personal info

        // Set edit text values
        binding.etIdNumber.setText(voter.idNumber)
        binding.etFirstName.setText(voter.firstName)
        binding.etLastName.setText(voter.lastName)
        binding.etMobile.setText(voter.mobile)
        binding.etPassword.setText(voter.password) // Show actual password in edit mode
    }

    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun saveProfileChanges() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (mobile.length != 10) {
            Toast.makeText(requireContext(), "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val voter = db.voterDao().getVoterById(loggedInVoterId)
                voter?.let { existingVoter ->
                    val updatedVoter = existingVoter.copy(
                        firstName = firstName,
                        lastName = lastName,
                        mobile = mobile,
                        password = password
                    )

                    db.voterDao().updateVoter(updatedVoter)

                    requireActivity().runOnUiThread {
                        // Update the personal information display
                        updateUI(updatedVoter)
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupPasswordToggle() {
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                // Hide password
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }

            // Move cursor to end
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    private fun showLogoutConfirmation() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .create()

        alertDialog.setOnShowListener {
            // Set white background
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.white)

            // Get the buttons
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Set button colors
            positiveButton.setTextColor(Color.RED)
            negativeButton.setTextColor(Color.GREEN)

            // Make title bold and black
            val titleTextView = alertDialog.findViewById<TextView>(android.R.id.title)
            titleTextView?.let {
                it.setTextColor(Color.BLACK)
                val spannableTitle = SpannableString(it.text)
                spannableTitle.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, spannableTitle.length, 0)
                it.text = spannableTitle
            }

            // Make message black
            val messageTextView = alertDialog.findViewById<TextView>(android.R.id.message)
            messageTextView?.setTextColor(Color.BLACK)
        }

        alertDialog.show()
    }

    private fun performLogout() {
        val sharedPreferences = requireContext().getSharedPreferences("MyVotingAppPrefs", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("remember_me", false)
        editor.remove("logged_in_user_id")
        editor.remove("is_admin")
        editor.apply()

        val intent = Intent(requireActivity(), FirstScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteAccountConfirmation() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently lost.")
            .setPositiveButton("Yes") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create()

        alertDialog.setOnShowListener {
            // Set white background
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.white)

            // Get the buttons
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Set button colors
            positiveButton.setTextColor(Color.RED)
            negativeButton.setTextColor(Color.GREEN)

            // Make title bold and black
            val titleTextView = alertDialog.findViewById<TextView>(android.R.id.title)
            titleTextView?.let {
                it.setTextColor(Color.BLACK)
                val spannableTitle = SpannableString(it.text)
                spannableTitle.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, spannableTitle.length, 0)
                it.text = spannableTitle
            }

            // Make message black
            val messageTextView = alertDialog.findViewById<TextView>(android.R.id.message)
            messageTextView?.setTextColor(Color.BLACK)
        }

        alertDialog.show()
    }

    private fun deleteAccount() {
        lifecycleScope.launch {
            try {
                val voter = db.voterDao().getVoterById(loggedInVoterId)
                voter?.let {
                    db.voterDao().deleteVoter(it)

                    val sharedPreferences = requireContext().getSharedPreferences("MyVotingAppPrefs", android.content.Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("remember_me", false)
                    editor.remove("logged_in_user_id")
                    editor.remove("is_admin")
                    editor.apply()

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_LONG).show()

                        val intent = Intent(requireActivity(), FirstScreenActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
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