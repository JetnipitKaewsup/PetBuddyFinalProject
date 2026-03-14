package com.example.petbuddy.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ActivityForgotPasswordBinding
import com.example.petbuddy.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        setupToolbar()
        setupListeners()

    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish() // กลับไปหน้า Login
        }
    }

    private fun setupListeners() {
        binding.btnSendResetEmail.setOnClickListener {
            sendPasswordResetEmail()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email required"
            return
        }

        // Disable button
        binding.btnSendResetEmail.isEnabled = false
        binding.btnSendResetEmail.text = "Sending..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ซ่อน input และแสดง success message
                    binding.etEmail.isEnabled = false
                    binding.btnSendResetEmail.visibility = View.GONE
                    binding.tvMessage.visibility = View.VISIBLE
                    binding.tvMessage.text = """
                        Password reset email sent to $email
                        
                        Please check your inbox and follow the instructions to reset your password.
                        
                        You can close this window and return to login.
                    """.trimIndent()

                    // เปลี่ยนปุ่ม navigation back เป็น "Back to Login"
                    binding.btnBackToLogin.visibility = View.VISIBLE
                    binding.btnBackToLogin.setOnClickListener {
                        finish() // กลับไป LoginActivity
                    }

                } else {
                    handleError(task.exception)
                    binding.btnSendResetEmail.isEnabled = true
                    binding.btnSendResetEmail.text = "Send Reset Email"
                }
            }
    }

    private fun handleError(exception: Exception?) {
        val errorMessage = when (exception?.message) {
            "There is no user record corresponding to this identifier. The user may have been deleted." ->
                "No account found with this email address"
            "The email address is badly formatted." ->
                "Invalid email format"
            else ->
                exception?.message ?: "Failed to send reset email"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}