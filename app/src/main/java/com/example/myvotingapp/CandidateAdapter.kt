package com.example.myvotingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CandidateAdapter : ListAdapter<Candidate, CandidateAdapter.CandidateViewHolder>(CandidateDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCandidate: ImageView = itemView.findViewById(R.id.ivCandidate)
        private val tvName: TextView = itemView.findViewById(R.id.tvCandidateName)
        private val tvManifesto: TextView = itemView.findViewById(R.id.tvManifesto)

        fun bind(candidate: Candidate) {
            // Using placeholder image - you can replace with actual image loading later
            ivCandidate.setImageResource(android.R.drawable.ic_menu_gallery)
            tvName.text = candidate.name
            tvManifesto.text = candidate.manifesto
        }
    }

    class CandidateDiffCallback : DiffUtil.ItemCallback<Candidate>() {
        override fun areItemsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem.candidateId == newItem.candidateId
        }

        override fun areContentsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem == newItem
        }
    }
}