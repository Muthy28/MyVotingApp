package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PositionsFragment : Fragment() {

    private val viewModel: PositionsViewModel by viewModels()

    private lateinit var btnAddPosition: Button
    private lateinit var btnUpdatePosition: Button
    private lateinit var btnDeletePosition: Button
    private lateinit var formContainer: LinearLayout
    private lateinit var tvFormTitle: TextView
    private lateinit var tvFormHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_positions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupObservers()
    }

    private fun initializeViews(view: View) {
        btnAddPosition = view.findViewById(R.id.btnAddPosition)
        btnUpdatePosition = view.findViewById(R.id.btnUpdatePosition)
        btnDeletePosition = view.findViewById(R.id.btnDeletePosition)
        formContainer = view.findViewById(R.id.formContainer)
        tvFormTitle = view.findViewById(R.id.tvFormTitle)
        tvFormHint = view.findViewById(R.id.tvFormHint)
    }

    private fun setupClickListeners() {
        btnAddPosition.setOnClickListener {
            showForm("Add Position")
            viewModel.showAddPositionForm()
        }

        btnUpdatePosition.setOnClickListener {
            showForm("Update Position")
            viewModel.showUpdatePositionForm()
        }

        btnDeletePosition.setOnClickListener {
            showForm("Delete Position")
            viewModel.showDeletePositionForm()
        }
    }

    private fun setupObservers() {
        viewModel.formContent.observe(viewLifecycleOwner) { formView ->
            // Clear previous form content (except the title and hint)
            formContainer.removeAllViews()

            // Add the title back
            formContainer.addView(tvFormTitle)

            // Add the new form content
            if (formView != null) {
                formContainer.addView(formView)
                tvFormHint.visibility = View.GONE
            } else {
                tvFormHint.visibility = View.VISIBLE
            }
        }

        // Observe toast messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.onToastMessageShown()
            }
        }
    }

    private fun showForm(title: String) {
        tvFormTitle.text = title
        formContainer.visibility = View.VISIBLE
        tvFormHint.text = "Loading form..."
    }
}