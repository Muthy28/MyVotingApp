package com.example.myvotingapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class VotersFragment : Fragment() {

    private val viewModel: VotersViewModel by viewModels()

    private lateinit var btnUpdateVoter: Button
    private lateinit var btnDeleteVoter: Button
    private lateinit var formContainer: LinearLayout
    private lateinit var tvFormTitle: TextView
    private lateinit var tvFormHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_voters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupObservers()
    }

    private fun initializeViews(view: View) {
        btnUpdateVoter = view.findViewById(R.id.btnUpdateVoter)
        btnDeleteVoter = view.findViewById(R.id.btnDeleteVoter)
        formContainer = view.findViewById(R.id.formContainer)
        tvFormTitle = view.findViewById(R.id.tvFormTitle)
        tvFormHint = view.findViewById(R.id.tvFormHint)
    }

    private fun setupClickListeners() {
        btnUpdateVoter.setOnClickListener {
            showForm("Update Voter")
            viewModel.showUpdateVoterForm()
        }

        btnDeleteVoter.setOnClickListener {
            showForm("Delete Voter")
            viewModel.showDeleteVoterForm()
        }
    }

    private fun setupObservers() {
        viewModel.formContent.observe(viewLifecycleOwner) { formView ->
            formContainer.removeAllViews()
            formContainer.addView(tvFormTitle)

            if (formView != null) {
                formContainer.addView(formView)
                tvFormHint.visibility = View.GONE
            } else {
                tvFormHint.visibility = View.VISIBLE
            }
        }

        // Observe toast messages and handle special cases
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                when {
                    it.startsWith("CONFIRM_DELETE_VOTER|") -> {
                        // Handle voter deletion confirmation
                        val parts = it.split("|")
                        if (parts.size >= 3) {
                            val voterId = parts[1]
                            val voterName = parts[2] // Full name is now in one part
                            showDeleteVoterConfirmation(voterId, voterName)
                        }
                        viewModel.onToastMessageShown()
                    }
                    it.startsWith("Error:") -> {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        viewModel.onToastMessageShown()
                    }
                    else -> {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.onToastMessageShown()
                    }
                }
            }
        }

        // Observe toast messages and handle special cases
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                when {
                    it.startsWith("CONFIRM_DELETE_VOTER:") -> {
                        // Handle voter deletion confirmation
                        val parts = it.split(":")
                        if (parts.size >= 4) {
                            val voterId = parts[1]
                            val voterName = parts[2] + " " + parts[3]
                            showDeleteVoterConfirmation(voterId, voterName)
                        }
                        viewModel.onToastMessageShown()
                    }
                    it.startsWith("Error:") -> {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        viewModel.onToastMessageShown()
                    }
                    else -> {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.onToastMessageShown()
                    }
                }
            }
        }
    }

    private fun showDeleteVoterConfirmation(voterId: String, voterName: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Remove Voter")
            .setMessage("Are you sure you want to remove $voterName?")
            .setPositiveButton("Remove") { dialog, which ->
                // Perform deletion
                viewModel.deleteVoterById(voterId)
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
            titleTextView?.let { tv ->
                tv.setTextColor(Color.BLACK)
                val spannableTitle = SpannableString(tv.text)
                spannableTitle.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, spannableTitle.length, 0)
                tv.text = spannableTitle
            }

            // Make message black
            val messageTextView = alertDialog.findViewById<TextView>(android.R.id.message)
            messageTextView?.setTextColor(Color.BLACK)
        }

        alertDialog.show()
    }

    private fun showForm(title: String) {
        tvFormTitle.text = title
        formContainer.visibility = View.VISIBLE
        tvFormHint.text = "Loading form..."
    }
}