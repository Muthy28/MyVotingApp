package com.example.myvotingapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class CandidatesFragment : Fragment() {

    private val viewModel: CandidatesViewModel by viewModels()

    private lateinit var btnAddCandidate: Button
    private lateinit var btnUpdateCandidate: Button
    private lateinit var btnDeleteCandidate: Button
    private lateinit var formContainer: LinearLayout
    private lateinit var tvFormTitle: TextView
    private lateinit var tvFormHint: TextView

    // Store references to current form views for image handling
    private var currentImageView: ImageView? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                // Update the image view with selected image
                currentImageView?.let { imageView ->
                    viewModel.setSelectedImage(uri, imageView)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_candidates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupObservers()
    }

    private fun initializeViews(view: View) {
        btnAddCandidate = view.findViewById(R.id.btnAddCandidate)
        btnUpdateCandidate = view.findViewById(R.id.btnUpdateCandidate)
        btnDeleteCandidate = view.findViewById(R.id.btnDeleteCandidate)
        formContainer = view.findViewById(R.id.formContainer)
        tvFormTitle = view.findViewById(R.id.tvFormTitle)
        tvFormHint = view.findViewById(R.id.tvFormHint)
    }

    private fun setupClickListeners() {
        btnAddCandidate.setOnClickListener {
            showForm("Add Candidate")
            viewModel.showAddCandidateForm()
        }

        btnUpdateCandidate.setOnClickListener {
            showForm("Update Candidate")
            viewModel.showUpdateCandidateForm()
        }

        btnDeleteCandidate.setOnClickListener {
            showForm("Delete Candidate")
            viewModel.showDeleteCandidateForm()
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
                    it == "SELECT_PHOTO_ADD" -> openImagePickerForCurrentForm()
                    it == "SELECT_PHOTO_UPDATE" -> openImagePickerForCurrentForm()
                    it.startsWith("CONFIRM_DELETE:") -> {
                        // Handle candidate deletion confirmation
                        val parts = it.split(":")
                        if (parts.size >= 3) {
                            val candidateId = parts[1].toIntOrNull()
                            val candidateName = parts[2]
                            if (candidateId != null) {
                                showDeleteCandidateConfirmation(candidateId, candidateName)
                            }
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

    private fun showDeleteCandidateConfirmation(candidateId: Int, candidateName: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Remove Candidate")
            .setMessage("Are you sure you want to remove $candidateName?")
            .setPositiveButton("Remove") { dialog, which ->
                // Perform deletion
                viewModel.deleteCandidateById(candidateId)
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
                spannableTitle.setSpan(StyleSpan(Typeface.BOLD), 0, spannableTitle.length, 0)
                tv.text = spannableTitle
            }

            // Make message black
            val messageTextView = alertDialog.findViewById<TextView>(android.R.id.message)
            messageTextView?.setTextColor(Color.BLACK)
        }

        alertDialog.show()
    }

    private fun setupImageSelection(formView: View) {
        // Find the image view and select photo button in the current form
        val imgCandidatePhoto = formView.findViewById<ImageView?>(R.id.imgCandidatePhoto)
        val btnSelectPhoto = formView.findViewById<Button?>(R.id.btnSelectPhoto)

        currentImageView = imgCandidatePhoto

        btnSelectPhoto?.setOnClickListener {
            // Use the public methods to trigger image selection
            val formTitle = tvFormTitle.text.toString()
            if (formTitle.contains("Add", ignoreCase = true)) {
                viewModel.triggerAddPhotoSelection()
            } else if (formTitle.contains("Update", ignoreCase = true)) {
                viewModel.triggerUpdatePhotoSelection()
            }
        }
    }

    private fun openImagePickerForCurrentForm() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        // Create a chooser to ensure user can select from multiple apps
        val chooser = Intent.createChooser(intent, "Select Candidate Photo")

        try {
            imagePickerLauncher.launch(chooser)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening image picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showForm(title: String) {
        tvFormTitle.text = title
        formContainer.visibility = View.VISIBLE
        tvFormHint.text = "Loading form..."
        // Reset current image view when showing new form
        currentImageView = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up references
        currentImageView = null
    }
}