package com.example.myvotingapp

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class AdminHomeFragment : Fragment() {

    private val viewModel: AdminHomeViewModel by viewModels()

    private lateinit var btnPositionsInfo: Button
    private lateinit var btnCandidatesInfo: Button
    private lateinit var btnVotersInfo: Button
    private lateinit var btnVotesInfo: Button
    private lateinit var resultsContainer: LinearLayout
    private lateinit var tvResultsTitle: TextView
    private lateinit var tvResultsContent: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupObservers()

        // Set underlined text for buttons
        setUnderlinedButtonText()
    }

    private fun setUnderlinedButtonText() {
        val underlinedText = "<u>More Info</u> →"

        btnPositionsInfo.text = Html.fromHtml(underlinedText, Html.FROM_HTML_MODE_LEGACY)
        btnCandidatesInfo.text = Html.fromHtml(underlinedText, Html.FROM_HTML_MODE_LEGACY)
        btnVotersInfo.text = Html.fromHtml(underlinedText, Html.FROM_HTML_MODE_LEGACY)
        btnVotesInfo.text = Html.fromHtml(underlinedText, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun initializeViews(view: View) {
        btnPositionsInfo = view.findViewById(R.id.btnPositionsInfo)
        btnCandidatesInfo = view.findViewById(R.id.btnCandidatesInfo)
        btnVotersInfo = view.findViewById(R.id.btnVotersInfo)
        btnVotesInfo = view.findViewById(R.id.btnVotesInfo)
        resultsContainer = view.findViewById(R.id.resultsContainer)
        tvResultsTitle = view.findViewById(R.id.tvResultsTitle)
        tvResultsContent = view.findViewById(R.id.tvResultsContent)
    }

    private fun setupClickListeners() {
        btnPositionsInfo.setOnClickListener {
            showResults("Positions Information")
            viewModel.loadPositionsInfo()
        }

        btnCandidatesInfo.setOnClickListener {
            showResults("Candidates Information")
            viewModel.loadCandidatesInfo()
        }

        btnVotersInfo.setOnClickListener {
            showResults("Voters Information")
            viewModel.loadVotersInfo()
        }

        btnVotesInfo.setOnClickListener {
            showResults("Votes Information")
            viewModel.loadVotesInfo()
        }
    }

    private fun setupObservers() {
        viewModel.positionsInfo.observe(viewLifecycleOwner) { info ->
            tvResultsContent.text = Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY)
        }

        viewModel.candidatesInfo.observe(viewLifecycleOwner) { info ->
            tvResultsContent.text = Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY)
        }

        viewModel.votersInfo.observe(viewLifecycleOwner) { info ->
            tvResultsContent.text = Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY)
        }

        viewModel.votesInfo.observe(viewLifecycleOwner) { info ->
            tvResultsContent.text = Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY)
        }
    }

    private fun showResults(title: String) {
        tvResultsTitle.text = title
        resultsContainer.visibility = View.VISIBLE
        tvResultsContent.text = "Loading..."
    }
}