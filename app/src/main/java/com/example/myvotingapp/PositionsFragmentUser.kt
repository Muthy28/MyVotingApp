package com.example.myvotingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PositionsFragmentUser : Fragment() {

    private lateinit var voterId: String
    private lateinit var positionsContainer: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voterId = arguments?.getString("voterId") ?: ""
        db = AppDatabase.getDatabase(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_positions_user, container, false)
        positionsContainer = root.findViewById(R.id.positionsContainer)
        loadPositions()
        return root
    }

    private fun loadPositions() {
        lifecycleScope.launch {
            db.positionDao().getAllPositionsFlow().collectLatest { positions ->
                positionsContainer.removeAllViews()
                positions.forEach { position ->
                    val tv = TextView(requireContext()).apply {
                        text = position.name
                        textSize = 18f
                        setPadding(20,20,20,20)
                        setBackgroundColor(0xFFBBDEFB.toInt())
                        setTextColor(0xFF0D47A1.toInt())
                    }
                    tv.setOnClickListener {
                        parentFragmentManager.commit {
                            replace(
                                R.id.userFragmentContainer,
                                CandidatesFragmentUser().apply {
                                    arguments = Bundle().apply {
                                        putString("voterId", voterId)
                                        putLong("positionId", position.positionId)
                                        putString("positionName", position.name)
                                    }
                                }
                            )
                            addToBackStack(null)
                        }
                    }
                    positionsContainer.addView(tv)
                }
            }
        }
    }
}
