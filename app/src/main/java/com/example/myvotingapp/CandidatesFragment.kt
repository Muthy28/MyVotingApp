package com.example.myvotingapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CandidatesFragment : Fragment() {

    private val viewModel: CandidatesViewModel by viewModels()

    // Declare views
    private lateinit var edtCandidateName: EditText
    private lateinit var edtManifesto: EditText
    private lateinit var spinnerPositions: Spinner
    private lateinit var imgCandidatePhoto: ImageView
    private lateinit var btnSelectPhoto: Button
    private lateinit var btnAddCandidate: Button

    // Local cache for the positions list to easily find the ID of the selected item
    private var positionsList = listOf<Position>()
    private var selectedPositionId: Long? = null
    private var selectedImageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                imgCandidatePhoto.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_candidates, container, false)

        edtCandidateName = view.findViewById(R.id.edtCandidateName)
        edtManifesto = view.findViewById(R.id.edtManifesto)
        spinnerPositions = view.findViewById(R.id.spinnerPositions)
        imgCandidatePhoto = view.findViewById(R.id.imgCandidatePhoto)
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto)
        btnAddCandidate = view.findViewById(R.id.btnAddCandidate)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.allPositions.observe(viewLifecycleOwner) { dbPositions ->
            positionsList = dbPositions
            val positionNames = dbPositions.map { it.name }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positionNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPositions.adapter = adapter
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.onToastMessageShown()
            }
        }
    }

    private fun setupListeners() {
        btnAddCandidate.setOnClickListener {
            val candidateName = edtCandidateName.text.toString().trim()
            val manifesto = edtManifesto.text.toString().trim()

            if (candidateName.isBlank() || manifesto.isBlank() || selectedPositionId == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Copy image to internal storage and get file path
            val imagePath = selectedImageUri?.let { uri ->
                copyImageToInternalStorage(uri)
            }

            viewModel.addCandidate(candidateName, selectedPositionId!!, manifesto, imagePath)

            // Clear the input fields after attempting to add
            edtCandidateName.text.clear()
            edtManifesto.text.clear()
            selectedImageUri = null
            imgCandidatePhoto.setImageResource(R.drawable.ic_launcher_foreground)
        }

        btnSelectPhoto.setOnClickListener {
            openImagePicker()
        }

        spinnerPositions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (positionsList.isNotEmpty()) {
                    selectedPositionId = positionsList[position].positionId
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPositionId = null
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val directory = File(requireContext().filesDir, "candidate_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "candidate_${System.currentTimeMillis()}.jpg")

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            null
        }
    }
}