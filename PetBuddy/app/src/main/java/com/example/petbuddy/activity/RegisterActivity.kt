package com.example.petbuddy.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petbuddy.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()

        binding.buttonReg.setOnClickListener {

            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()
            val confirmPassword = binding.editConfPass.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this,"Please enter your email address.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this,"Please enter your password.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this,"Passwords do not match.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->

                    if (!task.isSuccessful) {

                        if (password.length < 6) {
                            binding.editTextTextPassword.error = "Password must be at least 6 characters"
                        } else {
                            Toast.makeText(
                                this,
                                "Authentication Failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {

                        val uid = task.result.user!!.uid
                        val userEmail = task.result.user!!.email ?: ""
                        val username =  binding.editUsername.text.toString()

                        saveUserToFirestore(uid,userEmail,username)

                        Toast.makeText(this,"Create account successfully!", Toast.LENGTH_LONG).show()

//                        startActivity(Intent(this, CreateProfile::class.java))
                        finish()
                    }
                }
        }
    }

    private fun saveUserToFirestore(uid: String,email: String, username: String){

        val userToFirebase = hashMapOf(
            "userId" to uid,
            "email" to email,
            "username" to username
        )

        db.collection("User").document(uid).set(userToFirebase)
    }
}