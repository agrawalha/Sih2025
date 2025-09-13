package com.example.sih2025.Fragments

import Modals.QueryModal
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sih2025.HistoryAdapter
import com.example.sih2025.databinding.ActivityHistoryFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private lateinit var binding: ActivityHistoryFragmentBinding
    private lateinit var queryList: MutableList<QueryModal>
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityHistoryFragmentBinding.inflate(inflater, container, false)

        queryList = mutableListOf()
        adapter = HistoryAdapter(queryList)

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadUserQueries()

        return binding.root
    }

    private fun loadUserQueries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reference = FirebaseDatabase.getInstance().reference.child("UserQueries").child(userId)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                queryList.clear()
                for (dataSnap in snapshot.children) {
                    val query = dataSnap.getValue(QueryModal::class.java)
                    if (query != null) queryList.add(query)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
