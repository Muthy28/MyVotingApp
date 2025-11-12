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

    private lateinit var addBtn: Button
    private lateinit var updateBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var formContainer: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_positions, container, false)

        addBtn = root.findViewById(R.id.btnAddPosition)
        updateBtn = root.findViewById(R.id.btnUpdatePosition)
        deleteBtn = root.findViewById(R.id.btnDeletePosition)
        formContainer = root.findViewById(R.id.formContainer)

        db = AppDatabase.getDatabase(requireContext())

        addBtn.setOnClickListener { showAddForm() }
        updateBtn.setOnClickListener { showUpdateForm() }
        deleteBtn.setOnClickListener { showDeleteForm() }

        return root
    }

    /** ---------------- ADD POSITION ---------------- **/
    private fun showAddForm() {
        formContainer.removeAllViews()

        val editText = EditText(requireContext()).apply {
            hint = "Enter Position Name"
        }

        val submit = Button(requireContext()).apply {
            text = "Submit"
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            setTextColor(resources.getColor(android.R.color.white))
        }

        submit.setOnClickListener {
            val positionName = editText.text.toString().trim()
            if (positionName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a position name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.positionDao().insert(Position(name = positionName))
                Toast.makeText(requireContext(), "Position Added", Toast.LENGTH_SHORT).show()
                editText.text.clear()
            }
        }

        formContainer.addView(editText)
        formContainer.addView(submit)
    }

    /** ---------------- UPDATE POSITION ---------------- **/
    private fun showUpdateForm() {
        formContainer.removeAllViews()

        val editTextId = EditText(requireContext()).apply {
            hint = "Enter Position ID to Update"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val editTextName = EditText(requireContext()).apply {
            hint = "Enter New Position Name"
        }

        val submit = Button(requireContext()).apply {
            text = "Update"
            setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
            setTextColor(resources.getColor(android.R.color.white))
        }

        submit.setOnClickListener {
            val id = editTextId.text.toString().toLongOrNull()
            val newName = editTextName.text.toString().trim()

            if (id == null || newName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter valid ID and name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val position = db.positionDao().getPositionById(id)
                if (position != null) {
                    // ✅ Update only the name and call update()
                    position.name = newName
                    db.positionDao().update(position)
                    Toast.makeText(requireContext(), "Position Updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Position ID not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        formContainer.addView(editTextId)
        formContainer.addView(editTextName)
        formContainer.addView(submit)
    }

    /** ---------------- DELETE POSITION ---------------- **/
    private fun showDeleteForm() {
        formContainer.removeAllViews()

        val editTextId = EditText(requireContext()).apply {
            hint = "Enter Position ID to Delete"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val submit = Button(requireContext()).apply {
            text = "Delete"
            setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            setTextColor(resources.getColor(android.R.color.white))
        }

        submit.setOnClickListener {
            val id = editTextId.text.toString().toLongOrNull()
            if (id == null) {
                Toast.makeText(requireContext(), "Enter a valid Position ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete Position ID $id?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        val position = db.positionDao().getPositionById(id)
                        if (position != null) {
                            db.positionDao().delete(position)
                            Toast.makeText(requireContext(), "Position Deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Position ID not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        formContainer.addView(editTextId)
        formContainer.addView(submit)
    }
}
