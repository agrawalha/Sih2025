package com.example.sih2025.Fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.sih2025.R
import com.example.sih2025.databinding.ActivityHomeFragmentBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class HomeFragment : Fragment() {

    private var _binding: ActivityHomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var player: ExoPlayer
    private lateinit var handler: Handler

    private val hideSystemUiRunnable = Runnable {
        hideSystemUi()
    }

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { playSelectedVideo(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeFragmentBinding.inflate(inflater, container, false)

        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        handler = Handler(Looper.getMainLooper())

        // Detect touch anywhere on the entire card view, not just player controls
        binding.videosection.setOnClickListener {
            showSystemUi()
            resetUiAutoHideTimer()
        }

        binding.selectButton.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hideSystemUi()    // Start hidden
        resetUiAutoHideTimer()

        return binding.root
    }

    private fun playSelectedVideo(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = true
    }

    private fun showSystemUi() {
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_VISIBLE
    }

    private fun hideSystemUi() {
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun resetUiAutoHideTimer() {
        handler.removeCallbacks(hideSystemUiRunnable)
        handler.postDelayed(hideSystemUiRunnable, 5000)  // Auto-hide after 5 seconds
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(hideSystemUiRunnable)
        _binding = null
    }
}


