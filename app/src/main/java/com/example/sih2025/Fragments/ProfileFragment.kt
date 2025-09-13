package com.example.sih2025.Fragments

import Modals.userModal
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.sih2025.databinding.ActivityProfileFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {
    private lateinit var binding: ActivityProfileFragmentBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
      setUserdata()
        binding = ActivityProfileFragmentBinding.inflate(inflater,container,false)
        binding.profilesavebutton.setOnClickListener {
            val email = binding.profileemail.text.toString()
            val phone = binding.profilephone.text.toString()
            val name = binding.profilename.text.toString()
            val address = binding.profileaddress.text.toString()
            updateuserdata(email,phone,name,address)
        }
        return binding.root
    }

    private fun updateuserdata(email: String, phone: String, name: String, address: String) {
        val userid = auth.currentUser?.uid
        if(userid!=null) {
            val userreference:DatabaseReference = database.reference.child("Civilians").child("user").child(userid)
            val userData = hashMapOf(
                "usernn" to name,
                "phone" to phone,
                "address" to address,
                "emailnn" to email
            )
            userreference.setValue(userData).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(requireContext(), "Error Occured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserdata() {
        val userid = auth.currentUser?.uid
        if(userid!=null) {
            val userreference = database.getReference("Civilians").child("user").child(userid)
            userreference.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val userprofile = snapshot.getValue(userModal::class.java)
                        if(userprofile!=null) {
                            binding.apply {
                                profileaddress.setText(userprofile.address)
                                profileemail.setText(userprofile.emailnn)
                                profilephone.setText(userprofile.phone)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}
