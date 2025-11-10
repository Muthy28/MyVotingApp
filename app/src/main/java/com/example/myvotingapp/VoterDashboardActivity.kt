package com.example.myvotingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myvotingapp.databinding.ActivityVoterDashboardBinding

class VoterDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoterDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoterDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPresidential.setOnClickListener {
            // Show presidential candidates
        }

        binding.btnGovernor.setOnClickListener {
            // Show governor candidates
        }

        binding.btnWomenRep.setOnClickListener {
            // Show women rep candidates
        }
    }
}
