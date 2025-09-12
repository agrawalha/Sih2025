package com.example.sih2025

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction

class AutocompletePredictionAdapter(
    private val predictions: List<AutocompletePrediction>,
    private val onItemClick: (AutocompletePrediction) -> Unit
) : RecyclerView.Adapter<AutocompletePredictionAdapter.PredictionViewHolder>() {

    inner class PredictionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val text = view.findViewById<TextView>(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return PredictionViewHolder(view)
    }

    override fun getItemCount(): Int = predictions.size

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.text.text = prediction.getFullText(null).toString()
        holder.itemView.setOnClickListener { onItemClick(prediction) }
    }
}
