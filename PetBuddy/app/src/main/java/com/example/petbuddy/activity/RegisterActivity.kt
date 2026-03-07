package com.example.petbuddy.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.petbuddy.databinding.ActivityRegisterBinding
import com.example.petbuddy.supabase.SupabaseClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private var imageUri: Uri? = null

    // Select image from gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                binding.imageProfile.setImageURI(it)
                Log.d("REGISTER", "Image Selected: $it")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.imageProfile.setOnClickListener {
            openGallery()
        }

        binding.buttonReg.setOnClickListener {

            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()
            val confirmPassword = binding.editConfPass.text.toString().trim()
            val username = binding.editUsername.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this,"Passwords do not match",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            registerUser(email,password,username)
        }
    }

    private fun openGallery(){
        pickImageLauncher.launch("image/*")
    }

    private fun registerUser(email:String,password:String,username:String){

        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->

                if(!task.isSuccessful){

                    Toast.makeText(
                        this,
                        "Authentication Failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()

                }else{

                    val uid = task.result.user!!.uid
                    uploadImageAndSave(uid,email,username)

                }

            }

    }

    private fun uploadImageAndSave(uid:String,email:String,username:String){

        if(imageUri == null){
            saveUserToFirestore(uid,email,username,null)
            return
        }

        lifecycleScope.launch {

            try {

                val bytes = contentResolver.openInputStream(imageUri!!)?.use {
                    it.readBytes()
                } ?: throw Exception("Image read failed")

                val bucket = SupabaseClient.client.storage.from("picture")

                // Upload image
                bucket.upload(
                    path = "profile/$uid.jpg",
                    data = bytes,
                    upsert = true
                )

                // Get public URL
                val imageUrl = bucket.publicUrl("profile/$uid.jpg")

                Log.d("UPLOAD","Image URL: $imageUrl")

                saveUserToFirestore(uid,email,username,imageUrl)

            }catch (e: Exception) {

                e.printStackTrace()

                Log.e("UPLOAD_ERROR", "Upload failed", e)

                Toast.makeText(
                    this@RegisterActivity,
                    "Image upload failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            }

        }

    }

    private fun saveUserToFirestore(
        uid:String,
        email:String,
        username:String,
        imageUrl:String?
    ){

        val userInfo = hashMapOf(
            "userId" to uid,
            "email" to email,
            "username" to username,
            "profileImage" to imageUrl,
            "createAccount" to true
        )

        db.collection("users")
            .document(uid)
            .collection("userInfo")
            .document("profile")
            .set(userInfo)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Account created successfully!",
                    Toast.LENGTH_LONG
                ).show()

                finish()

            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Failed to save user",
                    Toast.LENGTH_LONG
                ).show()

            }

    }

}