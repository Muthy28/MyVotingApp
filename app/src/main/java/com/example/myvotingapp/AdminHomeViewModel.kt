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
                val positions = positionDao.getAllPositionsFlow().first()
                val candidates = candidateDao.getAllCandidatesFlow().first()

                val result = StringBuilder()
                result.append("<b>Total Positions:${getTabSpacing("Total Positions:")}${positions.size}</b><br><br>")

                if (positions.isNotEmpty()) {
                    result.append("<b>Candidates per Position:</b><br>")
                    positions.forEachIndexed { index, position ->
                        val candidateCount = candidates.count { it.positionId == position.positionId }
                        val positionText = "${index + 1}. ${position.name}:"
                        result.append("<b>${positionText}${getTabSpacing(positionText)}$candidateCount candidate(s)</b><br>")
                    }
                } else {
                    result.append("<b>No positions registered yet.</b>")
                }

                _positionsInfo.value = result.toString()
            } catch (e: Exception) {
                _positionsInfo.value = "<b>Error loading positions data: ${e.message}</b>"
            }
        }
    }

    fun loadCandidatesInfo() {
        viewModelScope.launch {
            try {
                val candidates = candidateDao.getAllCandidatesFlow().first()
                val positions = positionDao.getAllPositionsFlow().first()

                val result = StringBuilder()
                result.append("<b>Total Candidates:${getTabSpacing("Total Candidates:")}${candidates.size}</b><br><br>")

                if (candidates.isNotEmpty()) {
                    result.append("<b>Candidate Details:</b><br>")
                    candidates.forEachIndexed { index, candidate ->
                        val positionName = positions.find { it.positionId == candidate.positionId }?.name ?: "Unknown Position"
                        val candidateText = "${index + 1}. ${candidate.name}"
                        result.append("<b>${candidateText}${getTabSpacing(candidateText)}$positionName</b><br>")
                    }
                } else {
                    result.append("<b>No candidates registered yet.</b>")
                }

                _candidatesInfo.value = result.toString()
            } catch (e: Exception) {
                _candidatesInfo.value = "<b>Error loading candidates data: ${e.message}</b>"
            }
        }
    }

    fun loadVotersInfo() {
        viewModelScope.launch {
            try {
                val voters = voterDao.getAllVotersFlow().first()

                val result = StringBuilder()
                result.append("<b>Total Voters:${getTabSpacing("Total Voters:")}${voters.size}</b><br><br>")

                if (voters.isNotEmpty()) {
                    result.append("<b>Voter List:</b><br>")
                    voters.take(10).forEachIndexed { index, voter ->
                        val fullName = "${voter.firstName} ${voter.lastName}"
                        val voterText = "${index + 1}. $fullName"
                        result.append("<b>${voterText}${getTabSpacing(voterText)}(${voter.mobile})</b><br>")
                    }
                    if (voters.size > 10) {
                        result.append("<b>... and ${voters.size - 10} more voters</b>")
                    }
                } else {
                    result.append("<b>No voters registered yet.</b>")
                }

                _votersInfo.value = result.toString()
            } catch (e: Exception) {
                _votersInfo.value = "<b>Error loading voters data: ${e.message}</b>"
            }
        }
    }

    fun loadVotesInfo() {
        viewModelScope.launch {
            try {
                val candidates = candidateDao.getAllCandidatesFlow().first()
                val positions = positionDao.getAllPositionsFlow().first()
                val totalVotes = candidates.sumOf { it.voteCount }

                val result = StringBuilder()
                result.append("<b>Total Votes Cast:${getTabSpacing("Total Votes Cast:")}$totalVotes vote(s)</b><br><br>")

                if (candidates.isNotEmpty() && positions.isNotEmpty()) {
                    val positionsWithCandidates = positions.map { position ->
                        val positionCandidates = candidates
                            .filter { it.positionId == position.positionId }
                            .sortedByDescending { it.voteCount }
                        position to positionCandidates
                    }.filter { it.second.isNotEmpty() }

                    positionsWithCandidates.forEach { (position, positionCandidates) ->
                        val positionTotalVotes = positionCandidates.sumOf { it.voteCount }

                        result.append("<b><font color='#1976D2'>${position.name.toUpperCase()}</font></b><br>")
                        result.append("<b>Total Votes for ${position.name}:${getTabSpacing("Total Votes for ${position.name}:")}$positionTotalVotes vote(s)</b><br><br>")

                        result.append("<b>Candidate${getTabSpacing("Candidate", 25)}Votes${getTabSpacing("Votes", 10)}Percentage</b><br>")

                        positionCandidates.forEachIndexed { index, candidate ->
                            val percentage = if (positionTotalVotes > 0) {
                                String.format("%.1f", (candidate.voteCount.toDouble() / positionTotalVotes) * 100)
                            } else "0.0"

                            val candidateText = "${index + 1}. ${candidate.name}"
                            val votesText = "${candidate.voteCount}"
                            val percentageText = "$percentage%"

                            result.append("<b>${candidateText}${getTabSpacing(candidateText, 25)}${votesText}${getTabSpacing(votesText, 15)}$percentageText</b><br>")
                        }

                        // Bar chart visualization
                        result.append("<br><b>Vote Distribution Chart:</b><br>")
                        positionCandidates.forEachIndexed { index, candidate ->
                            val percentage = if (positionTotalVotes > 0) {
                                (candidate.voteCount.toDouble() / positionTotalVotes) * 100
                            } else 0.0

                            val barLength = (percentage / 3).toInt()
                            val bar = "█".repeat(maxOf(1, barLength))
                            val candidateText = "${index + 1}. ${candidate.name}"
                            val percentageDisplay = String.format("%.1f%%", percentage)

                            result.append("<b>$candidateText</b><br>")
                            result.append("<b><font color='#4CAF50'>$bar</font> $percentageDisplay (${candidate.voteCount} vote(s))</b><br>")
                        }

                        result.append("<br>")
                    }
                } else {
                    result.append("<b>No votes cast yet.</b>")
                }

                _votesInfo.value = result.toString()
            } catch (e: Exception) {
                _votesInfo.value = "<b>Error loading votes data: ${e.message}</b>"
            }
        }
    }

    // Helper function to create consistent tab spacing
    private fun getTabSpacing(text: String, targetLength: Int = 25): String {
        val currentLength = text.length
        val spacesNeeded = maxOf(0, targetLength - currentLength)
        return "&nbsp;".repeat(spacesNeeded)
    }
}