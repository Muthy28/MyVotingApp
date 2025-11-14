package com.example.myvotingapp

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PositionsViewModel(application: Application) : AndroidViewModel(application) {

    private val positionDao = AppDatabase.getDatabase(application).positionDao()
    private val candidateDao = AppDatabase.getDatabase(application).candidateDao()

    private val _formContent = MutableLiveData<View?>()
    val formContent = _formContent

    // LiveData for showing toast messages
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage = _toastMessage

    fun showAddPositionForm() {
        viewModelScope.launch {
            try {
                val inflater = LayoutInflater.from(getApplication())
                val formView = createAddPositionForm(inflater)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    fun showUpdatePositionForm() {
        viewModelScope.launch {
            try {
                val positions = positionDao.getAllPositionsFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createUpdatePositionForm(inflater, positions)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    fun showDeletePositionForm() {
        viewModelScope.launch {
            try {
                val positions = positionDao.getAllPositionsFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createDeletePositionForm(inflater, positions)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    private fun createAddPositionForm(inflater: LayoutInflater): View {
        val formView = inflater.inflate(R.layout.form_add_position, null)

        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)
        val etPositionName = formView.findViewById<EditText>(R.id.etPositionName)

        btnSubmit.setOnClickListener {
            val positionName = etPositionName.text.toString().trim()
            if (positionName.isBlank()) {
                showError("Please enter a position name")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    val newPosition = Position(name = positionName)
                    positionDao.insert(newPosition)
                    showSuccess("Position '$positionName' added successfully!")
                    etPositionName.text.clear()
                } catch (e: Exception) {
                    showError("Error adding position: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun createUpdatePositionForm(
        inflater: LayoutInflater,
        positions: List<Position>
    ): View {
        val formView = inflater.inflate(R.layout.form_update_position, null)

        val spinnerPositions = formView.findViewById<Spinner>(R.id.spinnerPositions)
        val etNewName = formView.findViewById<EditText>(R.id.etNewName)
        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)

        // Setup spinner
        val positionNames = positions.map { it.name }
        val adapter =
            ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, positionNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPositions.adapter = adapter

        btnSubmit.setOnClickListener {
            val selectedPosition = positions.getOrNull(spinnerPositions.selectedItemPosition)
            val newName = etNewName.text.toString().trim()

            if (selectedPosition == null) {
                showError("Please select a position to update")
                return@setOnClickListener
            }

            if (newName.isBlank()) {
                showError("Please enter a new position name")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    val updatedPosition = selectedPosition.copy(name = newName)
                    positionDao.update(updatedPosition)
                    showSuccess("Position updated successfully!")
                    etNewName.text.clear()
                } catch (e: Exception) {
                    showError("Error updating position: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun createDeletePositionForm(
        inflater: LayoutInflater,
        positions: List<Position>
    ): View {
        val formView = inflater.inflate(R.layout.form_delete_position, null)
        val positionsContainer = formView.findViewById<LinearLayout>(R.id.positionsContainer)
        val tvNoPositions = formView.findViewById<TextView>(R.id.tvNoPositions)

        // Clear previous content
        positionsContainer.removeAllViews()

        if (positions.isEmpty()) {
            tvNoPositions.visibility = View.VISIBLE
            positionsContainer.visibility = View.GONE
        } else {
            tvNoPositions.visibility = View.GONE
            positionsContainer.visibility = View.VISIBLE

            viewModelScope.launch {
                try {
                    // Get candidate counts for each position
                    val candidates = candidateDao.getAllCandidatesFlow().first()

                    positions.forEach { position ->
                        val candidateCount = candidates.count { it.positionId == position.positionId }
                        val positionItemView = createPositionItemView(inflater, position, candidateCount)
                        positionsContainer.addView(positionItemView)
                    }
                } catch (e: Exception) {
                    showError("Error loading position details: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun createPositionItemView(
        inflater: LayoutInflater,
        position: Position,
        candidateCount: Int
    ): View {
        val positionItemView = inflater.inflate(R.layout.item_position_delete, null)

        val tvPositionName = positionItemView.findViewById<TextView>(R.id.tvPositionName)
        val tvCandidateCount = positionItemView.findViewById<TextView>(R.id.tvCandidateCount)
        val btnRemove = positionItemView.findViewById<Button>(R.id.btnRemove)

        tvPositionName.text = position.name
        tvCandidateCount.text = "$candidateCount candidate(s)"

        btnRemove.setOnClickListener {
            showDeleteConfirmation(position, candidateCount)
        }

        return positionItemView
    }

    private fun showDeleteConfirmation(position: Position, candidateCount: Int) {
        val warningMessage = if (candidateCount > 0) {
            "This will also delete $candidateCount candidate(s) associated with this position."
        } else {
            "This position has no candidates."
        }

        _toastMessage.value = "CONFIRM_DELETE_POSITION:${position.positionId}:${position.name}:$candidateCount"
    }

    fun deletePositionById(positionId: Long) {
        viewModelScope.launch {
            try {
                val position = positionDao.getPositionById(positionId)
                position?.let {
                    positionDao.delete(it)
                    showSuccess("Position '${it.name}' removed successfully!")
                    // Refresh the delete form
                    showDeletePositionForm()
                }
            } catch (e: Exception) {
                showError("Error removing position: ${e.message}")
            }
        }
    }

    // Helper methods
    private fun showError(message: String) {
        _toastMessage.value = "Error: $message"
    }

    private fun showSuccess(message: String) {
        _toastMessage.value = message
    }

    // Call this from your fragment after showing the toast
    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}