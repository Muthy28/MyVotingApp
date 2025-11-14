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

class VotersViewModel(application: Application) : AndroidViewModel(application) {

    private val voterDao = AppDatabase.getDatabase(application).voterDao()

    private val _formContent = MutableLiveData<View?>()
    val formContent = _formContent

    // LiveData for showing toast messages
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage = _toastMessage

    fun showUpdateVoterForm() {
        viewModelScope.launch {
            try {
                val voters = voterDao.getAllVotersFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createUpdateVoterForm(inflater, voters)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    fun showDeleteVoterForm() {
        viewModelScope.launch {
            try {
                val voters = voterDao.getAllVotersFlow().first()
                val inflater = LayoutInflater.from(getApplication())
                val formView = createDeleteVoterForm(inflater, voters)
                _formContent.value = formView
            } catch (e: Exception) {
                showError("Error loading form: ${e.message}")
            }
        }
    }

    private fun createUpdateVoterForm(
        inflater: LayoutInflater,
        voters: List<Voter>
    ): View {
        val formView = inflater.inflate(R.layout.form_update_voter, null)

        val spinnerVoters = formView.findViewById<Spinner>(R.id.spinnerVoters)
        val etFirstName = formView.findViewById<EditText>(R.id.etFirstName)
        val etLastName = formView.findViewById<EditText>(R.id.etLastName)
        val etMobile = formView.findViewById<EditText>(R.id.etMobile)
        val btnSubmit = formView.findViewById<Button>(R.id.btnSubmit)

        // Setup spinner
        val voterDisplayNames = voters.map { "${it.firstName} ${it.lastName} (${it.idNumber})" }
        val adapter = ArrayAdapter(getApplication(), android.R.layout.simple_spinner_item, voterDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerVoters.adapter = adapter

        // When voter is selected, populate the form
        spinnerVoters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVoter = voters.getOrNull(position)
                selectedVoter?.let { voter ->
                    etFirstName.setText(voter.firstName)
                    etLastName.setText(voter.lastName)
                    etMobile.setText(voter.mobile)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSubmit.setOnClickListener {
            val selectedVoter = voters.getOrNull(spinnerVoters.selectedItemPosition)
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val mobile = etMobile.text.toString().trim()

            if (selectedVoter == null) {
                showError("Please select a voter to update")
                return@setOnClickListener
            }

            if (firstName.isBlank()) {
                showError("Please enter first name")
                return@setOnClickListener
            }

            if (lastName.isBlank()) {
                showError("Please enter last name")
                return@setOnClickListener
            }

            if (mobile.isBlank()) {
                showError("Please enter mobile number")
                return@setOnClickListener
            }

            viewModelScope.launch {
                try {
                    val updatedVoter = selectedVoter.copy(
                        firstName = firstName,
                        lastName = lastName,
                        mobile = mobile
                    )
                    voterDao.updateVoter(updatedVoter)
                    showSuccess("Voter '${firstName} ${lastName}' updated successfully!")
                } catch (e: Exception) {
                    showError("Error updating voter: ${e.message}")
                }
            }
        }

        return formView
    }

    private fun createDeleteVoterForm(
        inflater: LayoutInflater,
        voters: List<Voter>
    ): View {
        val formView = inflater.inflate(R.layout.form_delete_voter, null)
        val votersContainer = formView.findViewById<LinearLayout>(R.id.votersContainer)
        val tvNoVoters = formView.findViewById<TextView>(R.id.tvNoVoters)

        // Clear previous content
        votersContainer.removeAllViews()

        if (voters.isEmpty()) {
            tvNoVoters.visibility = View.VISIBLE
            votersContainer.visibility = View.GONE
        } else {
            tvNoVoters.visibility = View.GONE
            votersContainer.visibility = View.VISIBLE

            voters.forEach { voter ->
                val voterItemView = createVoterItemView(inflater, voter)
                votersContainer.addView(voterItemView)
            }
        }

        return formView
    }

    private fun createVoterItemView(
        inflater: LayoutInflater,
        voter: Voter
    ): View {
        val voterItemView = inflater.inflate(R.layout.item_voter_delete, null)

        val tvVoterName = voterItemView.findViewById<TextView>(R.id.tvVoterName)
        val tvVoterDetails = voterItemView.findViewById<TextView>(R.id.tvVoterDetails)
        val btnRemove = voterItemView.findViewById<Button>(R.id.btnRemove)

        tvVoterName.text = "${voter.firstName} ${voter.lastName}"
        tvVoterDetails.text = "ID: ${voter.idNumber} | Mobile: ${voter.mobile}"

        btnRemove.setOnClickListener {
            showDeleteConfirmation(voter)
        }

        return voterItemView
    }

    private fun showDeleteConfirmation(voter: Voter) {
        _toastMessage.value = "CONFIRM_DELETE_VOTER|${voter.idNumber}|${voter.firstName} ${voter.lastName}"
        }

    fun deleteVoterById(voterId: String) {
        viewModelScope.launch {
            try {
                val voter = voterDao.getVoterById(voterId)
                voter?.let {
                    voterDao.deleteVoter(it)
                    showSuccess("Voter '${it.firstName} ${it.lastName}' removed successfully!")
                    // Refresh the delete form
                    showDeleteVoterForm()
                }
            } catch (e: Exception) {
                showError("Error removing voter: ${e.message}")
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

    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}