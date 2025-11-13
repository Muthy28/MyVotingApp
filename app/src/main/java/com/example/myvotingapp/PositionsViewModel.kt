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

        val spinnerPositions = formView.findViewById<Spinner>(R.id.spinnerPositions)
        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)

        // Setup spinner
        val positionNames = positions.map { it.name }
        val adapter =
            ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, positionNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPositions.adapter = adapter

        btnSubmit.setOnClickListener {
            val selectedPosition = positions.getOrNull(spinnerPositions.selectedItemPosition)

            if (selectedPosition == null) {
                showError("Please select a position to delete")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    positionDao.delete(selectedPosition)
                    showSuccess("Position '${selectedPosition.name}' deleted successfully!")
                } catch (e: Exception) {
                    showError("Error deleting position: ${e.message}")
                }
            }
        }

        return formView
    }

    // Add the missing helper methods
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