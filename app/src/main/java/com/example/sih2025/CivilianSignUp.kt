package com.example.sih2025

import Modals.userModal
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sih2025.databinding.ActivityCivilianSignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class CivilianSignUp : AppCompatActivity() {
    private lateinit var binding: ActivityCivilianSignUpBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var database:DatabaseReference
    private lateinit var googlesignin: GoogleSignInClient
    private lateinit var username:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from strings.xml
            .requestEmail()
            .build()
        googlesignin = GoogleSignIn.getClient(this, googleSignInOptions)
        binding = ActivityCivilianSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.getReference()
        binding.createaccountbut.setOnClickListener {
            email = binding.emailenter.text.toString().trim()
            password = binding.passwordenter.text.toString().trim()
            username = binding.usernameenter.text.toString()
            if(email.isBlank()||password.isBlank()||username.isBlank()) {
                Toast.makeText(this, "Enter All The Details", Toast.LENGTH_SHORT).show()
            } else {
                createuser(email,password)
            }
        }
        binding.googlebut.setOnClickListener {
            Log.d("GOOGLE_SIGNIN", "Google button clicked")
            val googleClient = googlesignin.signInIntent
            launcher.launch(googleClient)
        }
        binding.alreadyhavebut.setOnClickListener {
            val intent = Intent(this,CivilianLogin::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createuser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if(task.isSuccessful) {
                val user = auth.currentUser
                // Send the verification email
                user?.sendEmailVerification()
                    ?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Log.d("SIGNUP", "Email verification sent.")
                            Toast.makeText(
                                baseContext,
                                "Account created. Verification email sent to ${user.email}.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.e("SIGNUP", "sendEmailVerification failed", verificationTask.exception)
                            Toast.makeText(
                                baseContext,
                                "Account created, but failed to send verification email.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                saveuserdata()
                // Sign out the user to ensure they log in after verifying
                auth.signOut()
                startActivity(Intent(this,CivilianLogin::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error While Creating the Account", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createaccount: Failure",task.exception)
            }
        }
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("GOOGLE_SIGNIN", "Launcher invoked. Result code: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                val idToken = account?.idToken

                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Google Sign-In Success", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Auth Failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.e("GOOGLE_SIGNIN", "Auth error", authTask.exception)
                        }
                    }
                } else {
                    Toast.makeText(this, "ID Token is null", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("GOOGLE_SIGNIN", "Exception in getting account", e)
            }
        } else {
            Toast.makeText(this, "Google Sign-In canceled or no data", Toast.LENGTH_SHORT).show()
            Log.w("GOOGLE_SIGNIN", "Result not OK or data is null")
        }
    }



    private fun saveuserdata() {
        username = binding.usernameenter.text.toString()
        email = binding.emailenter.text.toString().trim()
        password = binding.passwordenter.text.toString().trim()
        val user = userModal(username,email,password)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.child("Civilians").child("user").child(userId).setValue(user)
        }
    }
}