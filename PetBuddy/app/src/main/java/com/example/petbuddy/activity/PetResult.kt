package com.example.petbuddy.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.MainActivity
import com.example.petbuddy.adapter.PetResultAdapter
import com.example.petbuddy.data.Pet
import com.example.petbuddy.data.User
import com.example.petbuddy.databinding.ActivityPetResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PetResult : AppCompatActivity() {

    private lateinit var binding: ActivityPetResultBinding
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPetResultBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadPets()
        setupButtons()
    }

    private fun loadPets() {

        val userId = mAuth.uid ?: return
        val pets = mutableListOf<Pet>()

        db.collection("users")
            .document(userId)
            .collection("pets")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {

                    val pet = document.toObject(Pet::class.java)
                    pet.petId = document.id

                    pets.add(pet)
                }

                val adapter = PetResultAdapter(pets)

                binding.showProfile.layoutManager =
                    LinearLayoutManager(this)

                binding.showProfile.adapter = adapter
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error loading pets", it)
            }
    }

    private fun setupButtons() {

        binding.arrowGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.arrowBack.setOnClickListener {
            startActivity(Intent(this, CreatePetProfile::class.java))
        }

        binding.addPet.setOnClickListener {
            startActivity(Intent(this, CreatePetProfile::class.java))
        }
    }
}