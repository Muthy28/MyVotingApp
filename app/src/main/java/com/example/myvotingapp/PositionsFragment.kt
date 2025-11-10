package com.example.myvotingapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PositionsFragment : Fragment() {

    private lateinit var positionDao: PositionDao
    private lateinit var positionListLayout: LinearLayout
    private lateinit var edtPositionName: EditText
    private lateinit var btnAddPosition: Button
    private lateinit var btnUpdatePosition: Button
    private lateinit var btnDeletePosition: Button
    private var selectedPosition: Position? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragments_positions, container, false)

        // Initialize DAO
        positionDao = AppDatabase.getDatabase(requireContext()).positionDao()

        positionListLayout = view.findViewById(R.id.positionListLayout)
        edtPositionName = view.findViewById(R.id.edtPositionName)
        btnAddPosition = view.findViewById(R.id.btnAddPosition)
        btnUpdatePosition = view.findViewById(R.id.btnUpdatePosition)
        btnDeletePosition = view.findViewById(R.id.btnDeletePosition)

        selectedPosition = null

        // Button listeners
        btnAddPosition.setOnClickListener { addPosition() }
        btnUpdatePosition.setOnClickListener { updatePosition() }
        btnDeletePosition.setOnClickListener { confirmDeletePosition() }

        // Observe positions
        loadPositions()

        return view
    }

    private fun loadPositions() {
        viewLifecycleOwner.lifecycleScope.launch {
            positionDao.getAllPositionsFlow().collect { positions ->
                positionListLayout.removeAllViews()
                positions.forEach { position ->
                    val textView = TextView(requireContext())
                    textView.text = "• ${position.name}"
                    textView.textSize = 18f
                    textView.setPadding(8, 8, 8, 8)
                    textView.setOnClickListener {
                        selectedPosition = position
                        edtPositionName.setText(position.name)
                        Toast.makeText(requireContext(), "Selected: ${position.name}", Toast.LENGTH_SHORT).show()
                    }
                    positionListLayout.addView(textView)
                }
            }
        }
    }

    private fun addPosition() {
        val name = edtPositionName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Enter position name", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            positionDao.insert(Position(name = name))
            edtPositionName.text.clear()
            Toast.makeText(requireContext(), "Position added!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePosition() {
        val name = edtPositionName.text.toString().trim()
        val pos = selectedPosition
        if (pos == null) {
            Toast.makeText(requireContext(), "Select a position to update", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Enter new position name", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val updated = pos.copy(name = name)
            positionDao.insert(updated) // Reinsert with same ID (replace)
            Toast.makeText(requireContext(), "Position updated!", Toast.LENGTH_SHORT).show()
            edtPositionName.text.clear()
            selectedPosition = null
        }
    }

    private fun confirmDeletePosition() {
        val pos = selectedPosition
        if (pos == null) {
            Toast.makeText(requireContext(), "Select a position to delete", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Position")
            .setMessage("Do you really want to delete ${pos.name}?")
            .setPositiveButton("Yes") { _, _ ->
                deletePosition(pos)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deletePosition(position: Position) {
        viewLifecycleOwner.lifecycleScope.launch {
            positionDao.delete(position)
            Toast.makeText(requireContext(), "Position deleted", Toast.LENGTH_SHORT).show()
            selectedPosition = null
            edtPositionName.text.clear()
        }
    }
}
