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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update the name to show "Admin"
        val tvName = view.findViewById<TextView>(R.id.tvName)
        tvName.text = "Admin"

        // Set up logout button
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutConfirmation() // Fixed: call showLogoutConfirmation instead of logout directly
        }
    }

    private fun showLogoutConfirmation() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, which ->
                logout()
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

    private fun logout() {
        val sharedPreferences = requireContext().getSharedPreferences("MyVotingAppPrefs", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("remember_me", false)
        editor.remove("logged_in_user_id")
        editor.remove("is_admin")
        editor.apply()

        val intent = Intent(requireContext(), FirstScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        }
}