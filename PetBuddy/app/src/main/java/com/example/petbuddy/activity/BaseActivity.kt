package com.example.petbuddy.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.petbuddy.data.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var mAuth: FirebaseAuth
    protected lateinit var db: FirebaseFirestore
    protected var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.currentUser?.uid

        // ตรวจสอบว่ามี userId หรือไม่
        if (currentUserId == null) {
            // ถ้าไม่มี userId ให้กลับไปหน้า Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }

    protected fun getAllPets(callback: (List<Pet>) -> Unit) {
        currentUserId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("pets")
                .get()
                .addOnSuccessListener { snapshot ->
                    val pets = snapshot.documents.mapNotNull {
                        it.toObject(Pet::class.java)
                    }
                    callback(pets)
                }
                .addOnFailureListener {
                    callback(emptyList())
                }
        } ?: callback(emptyList())
    }


}