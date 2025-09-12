package com.example.sih2025.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sih2025.AutocompletePredictionAdapter
import com.example.sih2025.R
import com.example.sih2025.databinding.ActivityMapViewFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapViewFragment : Fragment(), OnMapReadyCallback {

    private var _binding: ActivityMapViewFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var placesClient: PlacesClient
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMapViewFragmentBinding.inflate(inflater, container, false)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }

        placesClient = Places.createClient(requireContext())

        setupSearchBar()

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        return binding.root
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener { editable ->
            val query = editable.toString()
            if (query.isEmpty()) {
                binding.predictionsRecycler.visibility = View.GONE
                return@addTextChangedListener
            }

            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions = response.autocompletePredictions
                    binding.predictionsRecycler.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = AutocompletePredictionAdapter(predictions) { prediction ->
                            val placeText = prediction.getFullText(null).toString()

                            // Search for placeText in the map and add a marker
                            searchPlaceOnMap(placeText)

                            Toast.makeText(
                                requireContext(),
                                "Selected: $placeText",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Autocomplete error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun searchPlaceOnMap(placeName: String) {
        // Simple geocoding example (replace with real geocoding logic)
        // For simplicity, let's assume it resolves to a static location:
        val sampleLatLng = LatLng(28.6139, 77.2090)  // Example: Delhi coordinates

        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(sampleLatLng).title(placeName))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleLatLng, 15f))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
