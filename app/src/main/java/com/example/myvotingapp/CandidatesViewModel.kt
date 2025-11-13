package com.example.myvotingapp

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CandidatesViewModel(application: Application) : AndroidViewModel(application) {

    private val candidateDao = AppDatabase.getDatabase(application).candidateDao()
    private val positionDao = AppDatabase.getDatabase(application).positionDao()

    private val _formContent = MutableLiveData<View?>()
    val formContent = _formContent

    // LiveData for showing toast messages
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage = _toastMessage

    // Store selected image URI
    private var selectedImageUri: Uri? = null

    // Keep this for any existing functionality that might need it
    val allPositions = positionDao.getAllPositionsFlow().asLiveData()

    fun showAddCandidateForm() {
        viewModelScope.launch {
            try {
                val positions = positionDao.getAllPositionsFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createAddCandidateForm(inflater, positions)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    fun showUpdateCandidateForm() {
        viewModelScope.launch {
            try {
                val candidates = candidateDao.getAllCandidatesFlow().first()
                val positions = positionDao.getAllPositionsFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createUpdateCandidateForm(inflater, candidates, positions)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    fun showDeleteCandidateForm() {
        viewModelScope.launch {
            try {
                val candidates = candidateDao.getAllCandidatesFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createDeleteCandidateForm(inflater, candidates)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    private fun createAddCandidateForm(inflater: LayoutInflater, positions: List<Position>): View {
        val formView = inflater.inflate(R.layout.form_add_candidate, null)

        val etCandidateName = formView.findViewById<EditText>(R.id.etCandidateName)
        val spinnerPositions = formView.findViewById<Spinner>(R.id.spinnerPositions)
        val etManifesto = formView.findViewById<EditText>(R.id.etManifesto)
        val imgCandidatePhoto = formView.findViewById<ImageView>(R.id.imgCandidatePhoto)
        val btnSelectPhoto = formView.findViewById<Button>(R.id.btnSelectPhoto)
        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)

        // Reset selected image
        selectedImageUri = null

        // Setup positions spinner
        val positionNames = positions.map { it.name }
        val adapter = ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, positionNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPositions.adapter = adapter

        // Photo selection
        btnSelectPhoto.setOnClickListener {
            // This will be handled by the fragment
            _toastMessage.value = "SELECT_PHOTO_ADD"
        }

        btnSubmit.setOnClickListener {
            val candidateName = etCandidateName.text.toString().trim()
            val manifesto = etManifesto.text.toString().trim()
            val selectedPosition = positions.getOrNull(spinnerPositions.selectedItemPosition)

            if (candidateName.isBlank()) {
                showError("Please enter candidate name")
                return@setOnClickListener
            }

            if (selectedPosition == null) {
                showError("Please select a position")
                return@setOnClickListener
            }

            if (manifesto.isBlank()) {
                showError("Please enter candidate manifesto")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    // Copy image to internal storage and get file path
                    val imagePath = selectedImageUri?.let { uri ->
                        copyImageToInternalStorage(uri)
                    }

                    val newCandidate = Candidate(
                        name = candidateName,
                        positionId = selectedPosition.positionId,
                        manifesto = manifesto,
                        imageUrl = imagePath
                    )
                    candidateDao.insertCandidate(newCandidate)
                    showSuccess("Candidate '$candidateName' added successfully!")
                    // Clear form
                    etCandidateName.text.clear()
                    etManifesto.text.clear()
                    selectedImageUri = null
                    imgCandidatePhoto.setImageResource(R.drawable.ic_profile_placeholder)
                } catch (e: Exception) {
                    showError("Error adding candidate: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun createUpdateCandidateForm(
        inflater: LayoutInflater,
        candidates: List<Candidate>,
        positions: List<Position>
    ): View {
        val formView = inflater.inflate(R.layout.form_update_candidate, null)

        val spinnerCandidates = formView.findViewById<Spinner>(R.id.spinnerCandidates)
        val etCandidateName = formView.findViewById<EditText>(R.id.etCandidateName)
        val spinnerPositions = formView.findViewById<Spinner>(R.id.spinnerPositions)
        val etManifesto = formView.findViewById<EditText>(R.id.etManifesto)
        val imgCandidatePhoto = formView.findViewById<ImageView>(R.id.imgCandidatePhoto)
        val btnSelectPhoto = formView.findViewById<Button>(R.id.btnSelectPhoto)
        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)

        // Reset selected image
        selectedImageUri = null

        // Setup candidates spinner
        val candidateNames = candidates.map { it.name }
        val candidateAdapter = ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, candidateNames)
        candidateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCandidates.adapter = candidateAdapter

        // Setup positions spinner
        val positionNames = positions.map { it.name }
        val positionAdapter = ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, positionNames)
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPositions.adapter = positionAdapter

        // When candidate is selected, populate the form
        spinnerCandidates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCandidate = candidates.getOrNull(position)
                selectedCandidate?.let { candidate ->
                    etCandidateName.setText(candidate.name)
                    etManifesto.setText(candidate.manifesto)

                    // Select the correct position in spinner
                    val candidatePositionIndex = positions.indexOfFirst { it.positionId == candidate.positionId }
                    if (candidatePositionIndex != -1) {
                        spinnerPositions.setSelection(candidatePositionIndex)
                    }

                    // Load candidate image if exists
                    candidate.imageUrl?.let { imagePath ->
                        val file = File(imagePath)
                        if (file.exists()) {
                            imgCandidatePhoto.setImageURI(Uri.fromFile(file))
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Photo selection
        btnSelectPhoto.setOnClickListener {
            // This will be handled by the fragment
            _toastMessage.value = "SELECT_PHOTO_UPDATE"
        }

        btnSubmit.setOnClickListener {
            val selectedCandidate = candidates.getOrNull(spinnerCandidates.selectedItemPosition)
            val candidateName = etCandidateName.text.toString().trim()
            val manifesto = etManifesto.text.toString().trim()
            val selectedPosition = positions.getOrNull(spinnerPositions.selectedItemPosition)

            if (selectedCandidate == null) {
                showError("Please select a candidate to update")
                return@setOnClickListener
            }

            if (candidateName.isBlank()) {
                showError("Please enter candidate name")
                return@setOnClickListener
            }

            if (selectedPosition == null) {
                showError("Please select a position")
                return@setOnClickListener
            }

            if (manifesto.isBlank()) {
                showError("Please enter candidate manifesto")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    // If new image is selected, copy it to internal storage
                    val imagePath = selectedImageUri?.let { uri ->
                        copyImageToInternalStorage(uri)
                    } ?: selectedCandidate.imageUrl // Keep existing image if no new one selected

                    val updatedCandidate = selectedCandidate.copy(
                        name = candidateName,
                        positionId = selectedPosition.positionId,
                        manifesto = manifesto,
                        imageUrl = imagePath
                    )
                    candidateDao.updateCandidate(updatedCandidate)
                    showSuccess("Candidate '$candidateName' updated successfully!")
                    selectedImageUri = null
                } catch (e: Exception) {
                    showError("Error updating candidate: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun createDeleteCandidateForm(
        inflater: LayoutInflater,
        candidates: List<Candidate>
    ): View {
        val formView = inflater.inflate(R.layout.form_delete_candidate, null)

        val spinnerCandidates = formView.findViewById<Spinner>(R.id.spinnerCandidates)
        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)

        // Setup candidates spinner
        val candidateNames = candidates.map { it.name }
        val adapter = ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, candidateNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCandidates.adapter = adapter

        btnSubmit.setOnClickListener {
            val selectedCandidate = candidates.getOrNull(spinnerCandidates.selectedItemPosition)

            if (selectedCandidate == null) {
                showError("Please select a candidate to delete")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    candidateDao.deleteCandidate(selectedCandidate)
                    showSuccess("Candidate '${selectedCandidate.name}' deleted successfully!")
                } catch (e: Exception) {
                    showError("Error deleting candidate: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
            val directory = File(getApplication<Application>().filesDir, "candidate_images")
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
            null
        }
    }

    // Method to set selected image URI and update ImageView
    fun setSelectedImage(uri: Uri, imageView: ImageView?) {
        selectedImageUri = uri
        imageView?.setImageURI(uri)
    }

    // Helper methods
    private fun showError(message: String) {
        _toastMessage.value = "Error: $message"
    }

    private fun showSuccess(message: String) {
        _toastMessage.value = message
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }

    fun triggerAddPhotoSelection() {
        _toastMessage.value = "SELECT_PHOTO_ADD"
    }

    fun triggerUpdatePhotoSelection() {
        _toastMessage.value = "SELECT_PHOTO_UPDATE"
    }
}