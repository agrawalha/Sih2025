package com.example.sih2025

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
import com.example.sih2025.databinding.ActivityCivilianLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class CivilianLogin : AppCompatActivity() {
    private lateinit var binding: ActivityCivilianLoginBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var googlesignin: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from strings.xml
            .requestEmail()
            .build()
        googlesignin = GoogleSignIn.getClient(this, googleSignInOptions)
        binding = ActivityCivilianLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.loginbut.setOnClickListener {
            email = binding.logininemail.text.toString().trim()
            password = binding.logininpassword.text.toString().trim()
            if(email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please Fill All The Entries", Toast.LENGTH_SHORT).show()
            }
            else {
                createuser()
            }
        }
        binding.loginingooglebut.setOnClickListener {
            Log.d("GOOGLE_SIGNIN", "Google button clicked")
            val googleClient = googlesignin.signInIntent
            launcher.launch(googleClient)
        }
        binding.donthavebut.setOnClickListener {
            val intent = Intent(this,CivilianSignUp::class.java)
            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createuser() {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateui(user)
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
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
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser!=null) {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun updateui(user: FirebaseUser?) {
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}