package com.example.myvotingapp

import android.app.Application
import androidx.lifecycle.*
import com.example.myvotingapp.AppDatabase
import com.example.myvotingapp.CandidateDao
import com.example.myvotingapp.PositionDao
import com.example.myvotingapp.Candidate
import com.example.myvotingapp.Position
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Use AndroidViewModel to get access to the application context for the database
class CandidatesViewModel(application: Application) : AndroidViewModel(application) {

    private val candidateDao: CandidateDao
    private val positionDao: PositionDao

    // LiveData to hold the list of positions for the UI to observe
    val allPositions: LiveData<List<Position>>

    // LiveData to communicate events back to the fragment (e.g., show a toast)
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    init {
        val db = AppDatabase.getDatabase(application)
        candidateDao = db.candidateDao()
        positionDao = db.positionDao()
        // Convert the Flow from Room into LiveData
        allPositions = positionDao.getAllPositionsFlow().asLiveData()
    }

    fun addCandidate(name: String, positionId: Long?) {
        if (name.isBlank()) {
            _toastMessage.value = "Please enter a candidate name"
            return
        }
        if (positionId == null) {
            _toastMessage.value = "Please select a position"
            return
        }

        // Use viewModelScope to launch a coroutine that is automatically cancelled when the ViewModel is cleared
        viewModelScope.launch {
            val newCandidate = Candidate(
                name = name,
                positionId = positionId,
                manifesto = "Default manifesto.",
                imageUrl = null
            )
            candidateDao.insertCandidate(newCandidate)
            _toastMessage.value = "Candidate '$name' added successfully!"
        }
    }

    // Call this function after the message has been shown
    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}
