package com.example.sih2025.Fragments

import Modals.QueryModal
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.sih2025.CloudinaryUploader
import com.example.sih2025.databinding.ActivityQueryFragmentBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class QueryFragment : Fragment() {

    private lateinit var binding: ActivityQueryFragmentBinding
    private lateinit var player: ExoPlayer
    private lateinit var handler: Handler
    private var selectedVideoUri: Uri? = null

    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedVideoUri = uri
                playSelectedVideo(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityQueryFragmentBinding.inflate(inflater, container, false)
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        handler = Handler(Looper.getMainLooper())

        binding.selectButton.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        binding.button.setOnClickListener {
            submitQuery()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return binding.root
    }

    private fun submitQuery() {
        val description = binding.queryentry.text.toString().trim()
        val location = binding.locationentry.text.toString().trim()

        if (selectedVideoUri == null || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Fill all fields and select video", Toast.LENGTH_SHORT).show()
            return
        }
        binding.button.isEnabled = false
        Toast.makeText(requireContext(), "Processing Your Query", Toast.LENGTH_SHORT).show()

        CloudinaryUploader.uploadVideoToCloudinary(selectedVideoUri!!, requireContext()) { videoUrl ->
            requireActivity().runOnUiThread {
                binding.button.isEnabled = true
                if (videoUrl != null) {
                    Toast.makeText(requireContext(), "Query Processed Successfully", Toast.LENGTH_SHORT).show()
                    saveQueryToFirebase(videoUrl, description, location)
                } else {
                    Toast.makeText(requireContext(), "Video upload failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun saveQueryToFirebase(videoUrl: String, description: String, location: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val queryData = QueryModal(videoUrl, description, location)

        FirebaseDatabase.getInstance().reference
            .child("UserQueries").child(userId).push()
            .setValue(queryData)
            .addOnSuccessListener {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Query saved successfully to Firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to save query: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun playSelectedVideo(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = true
    }
    override fun onStop() {
        super.onStop()
        player.release()
    }
}
