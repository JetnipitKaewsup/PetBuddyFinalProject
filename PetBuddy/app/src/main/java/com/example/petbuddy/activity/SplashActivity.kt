package com.example.petbuddy.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // แสดง splash layout
        setContentView(R.layout.activity_splash)

        preloadData()
    }

    private fun preloadData() {

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            startMain()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("userInfo")
            .document("profile")
            .get()
            .addOnSuccessListener {
                startMain()
            }
            .addOnFailureListener {
                startMain()
            }
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}