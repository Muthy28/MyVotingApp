package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.viewModels
import com.example.myvotingapp.CandidatesViewModel
import com.example.myvotingapp.Position
import com.example.myvotingapp.R

class CandidatesFragment : Fragment() {

    // Initialize ViewModel using the recommended 'by viewModels()' delegate
    private val viewModel: CandidatesViewModel by viewModels()

    // Declare views
    private lateinit var edtCandidateName: EditText
    private lateinit var spinnerPositions: Spinner
    private lateinit var btnAddCandidate: Button

    // Local cache for the positions list to easily find the ID of the selected item
    private var positionsList = listOf<Position>()
    private var selectedPositionId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_candidates, container, false)

        // Initialize Views using the inflated view
        edtCandidateName = view.findViewById(R.id.edtCandidateName)
        spinnerPositions = view.findViewById(R.id.spinnerPositions)
        btnAddCandidate = view.findViewById(R.id.btnAddCandidate)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Centralize the setup of UI observers and event listeners
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // Observer for the list of electoral positions.
        // This will automatically update the spinner whenever the data changes in the database.
        viewModel.allPositions.observe(viewLifecycleOwner) { dbPositions ->
            // Update local cache
            positionsList = dbPositions
            // Extract just the names for display in the spinner
            val positionNames = dbPositions.map { it.name }

            // Create and set the adapter for the spinner
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positionNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPositions.adapter = adapter
        }

        // Observer for toast messages from the ViewModel.
        // This allows the ViewModel to request a toast message (e.g., for success or error)
        // without holding a reference to the UI context.
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                // Inform the ViewModel that the message has been shown to prevent it from re-appearing on screen rotation.
                viewModel.onToastMessageShown()
            }
        }
    }

    private fun setupListeners() {
        // Listener for the "Add Candidate" button.
        btnAddCandidate.setOnClickListener {
            val candidateName = edtCandidateName.text.toString().trim()

            // Delegate the logic of adding a candidate to the ViewModel
            viewModel.addCandidate(candidateName, selectedPositionId)

            // Clear the input field after attempting to add
            edtCandidateName.text.clear()
        }

        // Listener for spinner item selections.
        spinnerPositions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // When an item is selected, find the corresponding Position object
                // from our local list and store its ID.
                if (positionsList.isNotEmpty()) {
                    selectedPositionId = positionsList[position].positionId
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // If nothing is selected, ensure the ID is null.
                selectedPositionId = null
            }
        }
    }
}
