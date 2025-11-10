package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.foundation.text2.input.insert
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CandidatesFragment : Fragment() {

    private lateinit var candidateDao: CandidateDao
    private lateinit var positionDao: PositionDao

    private lateinit var edtCandidateName: EditText
    private lateinit var spinnerPositions: Spinner
    private lateinit var btnAddCandidate: Button

    private var positionsList = listOf<Position>()
    private var selectedPositionId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_candidates, container, false)

        // Initialize DAOs
        val db = AppDatabase.getDatabase(requireContext())
        candidateDao = db.candidateDao()
        positionDao = db.positionDao()

        // Initialize Views
        edtCandidateName = view.findViewById(R.id.edtCandidateName)
        spinnerPositions = view.findViewById(R.id.spinnerPositions)
        btnAddCandidate = view.findViewById(R.id.btnAddCandidate)

        // Load positions into the spinner
        loadPositions()

        // Set button listener
        btnAddCandidate.setOnClickListener { addCandidate() }

        // TODO: Implement image selection logic for btnSelectPhoto and imgCandidatePhoto

        return view
    }

    private fun loadPositions() {
        viewLifecycleOwner.lifecycleScope.launch {
            positionDao.getAllPositionsFlow().collect { dbPositions ->
                positionsList = dbPositions
                val positionNames = dbPositions.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positionNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPositions.adapter = adapter

                spinnerPositions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        // Get the ID of the selected position
                        selectedPositionId = positionsList[position].id
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedPositionId = null
                    }
                }
            }
        }
    }

    private fun addCandidate() {
        val name = edtCandidateName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a candidate name", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPositionId == null) {
            Toast.makeText(requireContext(), "Please select a position", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val newCandidate = Candidate(
                name = name,
                positionId = selectedPositionId!!,
                manifesto = "Default manifesto.", // Placeholder
                imageUrl = null // Placeholder for photo URI
            )
            candidateDao.insertCandidate(newCandidate)

            Toast.makeText(requireContext(), "Candidate '$name' added successfully!", Toast.LENGTH_LONG).show()
            edtCandidateName.text.clear()
            spinnerPositions.setSelection(0)
        }
    }
}
