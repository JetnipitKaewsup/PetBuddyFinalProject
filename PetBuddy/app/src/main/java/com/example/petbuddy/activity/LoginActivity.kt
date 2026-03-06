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

            val email = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this,"Please enter your email address.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this,"Please enter your password.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->

                    if (!task.isSuccessful) {

                        if (password.length < 6) {
                            binding.editPassword.error = "Please check your password."
                        } else {

                            Toast.makeText(
                                this,
                                "Authentication Failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            Log.d(TAG,"Authentication Failed: ${task.exception?.message}")
                        }

                    } else {

                        val uid = mAuth.currentUser!!.uid

                        db.collection("User").document(uid)
                            .get()
                            .addOnSuccessListener { document ->

                                if (document.exists()) {

                                    val user = document.toObject(User::class.java)
                                    if (user == null) return@addOnSuccessListener

                                    if (!user.createAccount) {

//                                        startActivity(Intent(this, CreateProfile::class.java))

                                    } else if (user.userId.isEmpty()) {

                                        Toast.makeText(this,"This email has not been sign up",
                                            Toast.LENGTH_LONG).show()

                                    } else if (user.petIds.isEmpty()) {

//                                        startActivity(Intent(this, CreatePetProfile::class.java))

                                    } else {

                                        Toast.makeText(this,"Sign in successfully!", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                }
                            }
                    }
                }
        }
    }

    private fun setupNavigation(){

        binding.txtSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.txtForgetPassword.setOnClickListener {
//            startActivity(Intent(this, ForgetPassword::class.java))
        }
    }
}