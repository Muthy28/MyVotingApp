package com.example.myvotingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

sealed class ListItem {
    data class Header(val positionName: String) : ListItem()
    data class CandidateItem(val candidate: Candidate) : ListItem()
}

class SectionCandidateAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_section_header, parent, false)
            )
            TYPE_CANDIDATE -> CandidateViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_candidate, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind((getItem(position) as ListItem.Header).positionName)
            is CandidateViewHolder -> holder.bind((getItem(position) as ListItem.CandidateItem).candidate)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.CandidateItem -> TYPE_CANDIDATE
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvSectionHeader)

        fun bind(headerText: String) {
            tvHeader.text = headerText
        }
    }

    class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCandidate: ImageView = itemView.findViewById(R.id.ivCandidate)
        private val tvName: TextView = itemView.findViewById(R.id.tvCandidateName)
        private val tvManifesto: TextView = itemView.findViewById(R.id.tvManifesto)

        fun bind(candidate: Candidate) {
            tvName.text = candidate.name
            tvManifesto.text = candidate.manifesto
            loadCandidateImage(candidate.imageUrl)
        }

        private fun loadCandidateImage(imagePath: String?) {
            if (!imagePath.isNullOrBlank()) {
                try {
                    val file = File(imagePath)
                    if (file.exists()) {
                        // Load image from file path
                        ivCandidate.setImageURI(android.net.Uri.fromFile(file))
                    } else {
                        showPlaceholder()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showPlaceholder()
                }
            } else {
                showPlaceholder()
            }
        }

        private fun showPlaceholder() {
            ivCandidate.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when {
                oldItem is ListItem.Header && newItem is ListItem.Header ->
                    oldItem.positionName == newItem.positionName
                oldItem is ListItem.CandidateItem && newItem is ListItem.CandidateItem ->
                    oldItem.candidate.candidateId == newItem.candidate.candidateId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when {
                oldItem is ListItem.Header && newItem is ListItem.Header ->
                    oldItem.positionName == newItem.positionName
                oldItem is ListItem.CandidateItem && newItem is ListItem.CandidateItem ->
                    oldItem.candidate == newItem.candidate
                else -> false
            }
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CANDIDATE = 1
    }
}