package com.example.myvotingapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdminHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val positionDao = AppDatabase.getDatabase(application).positionDao()
    private val candidateDao = AppDatabase.getDatabase(application).candidateDao()
    private val voterDao = AppDatabase.getDatabase(application).voterDao()

    private val _positionsInfo = MutableLiveData<String>()
    val positionsInfo: LiveData<String> = _positionsInfo

    private val _candidatesInfo = MutableLiveData<String>()
    val candidatesInfo: LiveData<String> = _candidatesInfo

    private val _votersInfo = MutableLiveData<String>()
    val votersInfo: LiveData<String> = _votersInfo

    private val _votesInfo = MutableLiveData<String>()
    val votesInfo: LiveData<String> = _votesInfo

    fun loadPositionsInfo() {
        viewModelScope.launch {
            try {
                // Use .first() to get the current value from the Flow
                val positions = positionDao.getAllPositionsFlow().first()
                val candidates = candidateDao.getAllCandidatesFlow().first()

                val result = StringBuilder()
                result.append("Total Positions: ${positions.size}\n\n")

                if (positions.isNotEmpty()) {
                    result.append("Candidates per Position:\n")
                    positions.forEach { position ->
                        val candidateCount = candidates.count { it.positionId == position.positionId }
                        result.append("• ${position.name}: $candidateCount candidate(s)\n")
                    }
                } else {
                    result.append("No positions registered yet.")
                }

                _positionsInfo.value = result.toString()
            } catch (e: Exception) {
                _positionsInfo.value = "Error loading positions data: ${e.message}"
            }
        }
    }

    fun loadCandidatesInfo() {
        viewModelScope.launch {
            try {
                // Use .first() to get the current value from the Flow
                val candidates = candidateDao.getAllCandidatesFlow().first()
                val positions = positionDao.getAllPositionsFlow().first()

                val result = StringBuilder()
                result.append("Total Candidates: ${candidates.size}\n\n")

                if (candidates.isNotEmpty()) {
                    result.append("Candidate Details:\n")
                    candidates.forEach { candidate ->
                        val positionName = positions.find { it.positionId == candidate.positionId }?.name ?: "Unknown Position"
                        result.append("• ${candidate.name} - $positionName\n")
                    }
                } else {
                    result.append("No candidates registered yet.")
                }

                _candidatesInfo.value = result.toString()
            } catch (e: Exception) {
                _candidatesInfo.value = "Error loading candidates data: ${e.message}"
            }
        }
    }

    fun loadVotersInfo() {
        viewModelScope.launch {
            try {
                // Use .first() to get the current value from the Flow
                val voters = voterDao.getAllVotersFlow().first()

                val result = StringBuilder()
                result.append("Total Voters: ${voters.size}\n\n")

                if (voters.isNotEmpty()) {
                    result.append("Voter List:\n")
                    voters.take(10).forEach { voter -> // Show first 10 voters
                        result.append("• ${voter.firstName} ${voter.lastName} (${voter.mobile})\n")
                    }
                    if (voters.size > 10) {
                        result.append("... and ${voters.size - 10} more voters")
                    }
                } else {
                    result.append("No voters registered yet.")
                }

                _votersInfo.value = result.toString()
            } catch (e: Exception) {
                _votersInfo.value = "Error loading voters data: ${e.message}"
            }
        }
    }

    fun loadVotesInfo() {
        viewModelScope.launch {
            try {
                // Use .first() to get the current value from the Flow
                val candidates = candidateDao.getAllCandidatesFlow().first()
                val totalVotes = candidates.sumOf { it.voteCount }

                val result = StringBuilder()
                result.append("Total Votes Cast: $totalVotes\n\n")

                if (candidates.isNotEmpty()) {
                    result.append("Votes per Candidate:\n")
                    candidates.forEach { candidate ->
                        result.append("• ${candidate.name}: ${candidate.voteCount} vote(s)\n")
                    }
                } else {
                    result.append("No votes cast yet.")
                }

                _votesInfo.value = result.toString()
            } catch (e: Exception) {
                _votesInfo.value = "Error loading votes data: ${e.message}"
            }
        }
    }
}