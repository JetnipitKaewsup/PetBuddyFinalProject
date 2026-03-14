package com.example.petbuddy.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.petbuddy.MainActivity
import com.example.petbuddy.data.User
import com.example.petbuddy.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        setupLogin()
        setupNavigation()
    }

    private fun setupLogin() {

        binding.btnLogin.setOnClickListener {

            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your email address.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your password.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = mAuth.currentUser!!.uid
                    checkUserProfile(uid)

                }
                .addOnFailureListener { e ->

                    Toast.makeText(
                        this,
                        "Authentication Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e(TAG, "Login failed", e)
                }
        }
    }

    private fun checkUserProfile(uid: String) {

        db.collection("users")
            .document(uid)
            .collection("userInfo")
            .document("profile")
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {

                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }

                val user = document.toObject(User::class.java)

                if (user == null) {

                    Toast.makeText(
                        this,
                        "User data error",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                if (!user.createAccount) {

                    startActivity(Intent(this, RegisterActivity::class.java))
                    Toast.makeText(this,"YAHOO",Toast.LENGTH_LONG).show()
                    finish()

                } else {

                    checkUserPets(uid)
                }
            }
            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    "Failed to load user data",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(TAG, "Firestore error", e)
            }
    }

    private fun checkUserPets(uid: String) {

        db.collection("users")
            .document(uid)
            .collection("pets")
            .limit(1)
            .get()
            .addOnSuccessListener { pets ->

                if (pets.isEmpty) {

                    startActivity(Intent(this, CreatePetProfile::class.java))
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Sign in successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    "Failed to load pets",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(TAG, "Pet check error", e)
            }
    }

    private fun setupNavigation() {

        binding.txtSignup.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.txtForgetPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}