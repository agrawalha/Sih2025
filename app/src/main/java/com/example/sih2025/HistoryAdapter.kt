package com.example.sih2025

import Modals.QueryModal
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sih2025.databinding.QuerieshistoryviewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class HistoryAdapter(private val queryList: List<QueryModal>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: QuerieshistoryviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuerieshistoryviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val query = queryList[position]

        holder.binding.pastquery.setText(query.description)

        query.videoUrl?.let { url ->
            val player = ExoPlayer.Builder(holder.itemView.context).build()
            holder.binding.videoView.player = player
            player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            player.prepare()
            player.playWhenReady = false
        }
        // holder.binding.locationTextView.text = query.location
    }

    override fun getItemCount(): Int = queryList.size
}
