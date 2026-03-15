package com.example.petbuddy.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ActivityCreatePetProfileBinding
import com.example.petbuddy.api.RetrofitClient
import com.example.petbuddy.api.DogResponse
import com.example.petbuddy.api.CatBreed
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CreatePetProfile : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePetProfileBinding
    private lateinit var mAuth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var selectedDate: Timestamp? = null
    private var pathImage: String? = null
    private var currentPetId: String? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { saveProfileImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityCreatePetProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        currentPetId = UUID.randomUUID().toString()

        setupDropdown()
        setupDatePicker()
        setupCreateButton()
        setupImagePicker()
    }

    private fun setupDropdown() {

        val pets = resources.getStringArray(R.array.pet)

        val petAdapter = ArrayAdapter(
            this,
            R.layout.list_item,
            pets
        )

        binding.petDropdown.setAdapter(petAdapter)

        binding.petDropdown.setOnItemClickListener { parent, _, position, _ ->

            val selectedPet = parent.getItemAtPosition(position).toString()

            if (selectedPet == "Dog") {

                loadDogBreeds()

            } else if (selectedPet == "Cat") {

                loadCatBreeds()

            }

        }
    }

    private fun loadDogBreeds() {

        RetrofitClient.dogApi.getDogBreeds()
            .enqueue(object : Callback<DogResponse> {

                override fun onResponse(
                    call: Call<DogResponse>,
                    response: Response<DogResponse>
                ) {

                    if (response.isSuccessful) {

                        val message = response.body()?.message ?: emptyMap()

                        val breeds = message.flatMap { (breed, subBreeds) ->

                            if (subBreeds.isEmpty()) {
                                listOf(breed)
                            } else {
                                subBreeds.map { sub -> "$sub $breed" }
                            }

                        }.map { it.replaceFirstChar { c -> c.uppercase() } }

                        val adapter = ArrayAdapter(
                            this@CreatePetProfile,
                            R.layout.list_item,
                            breeds
                        )

                        binding.breedDropdown.setAdapter(adapter)

                    }

                }

                override fun onFailure(call: Call<DogResponse>, t: Throwable) {

                    Toast.makeText(
                        this@CreatePetProfile,
                        "Failed to load dog breeds",
                        Toast.LENGTH_LONG
                    ).show()

                }
            })
    }

    private fun loadCatBreeds() {

        RetrofitClient.catApi.getCatBreeds()
            .enqueue(object : Callback<List<CatBreed>> {

                override fun onResponse(
                    call: Call<List<CatBreed>>,
                    response: Response<List<CatBreed>>
                ) {

                    if (response.isSuccessful) {

                        val breeds =
                            response.body()?.map { it.name } ?: emptyList()

                        val adapter = ArrayAdapter(
                            this@CreatePetProfile,
                            R.layout.list_item,
                            breeds
                        )

                        binding.breedDropdown.setAdapter(adapter)

                    }

                }

                override fun onFailure(call: Call<List<CatBreed>>, t: Throwable) {

                    Toast.makeText(
                        this@CreatePetProfile,
                        "Failed to load cat breeds",
                        Toast.LENGTH_LONG
                    ).show()

                }
            })
    }

    private fun setupDatePicker() {

        binding.petDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->

                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    selectedDate = Timestamp(selectedCalendar.time)

                    binding.petDate.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }
    }

    private fun setupCreateButton() {

        binding.crePetBtn.setOnClickListener {

            val petName = binding.crePUser.text.toString().trim()
            val breed = binding.breedDropdown.text.toString().trim()
            val petType = binding.petDropdown.text.toString().trim()

            val sex = when (binding.radioGroupSex.checkedRadioButtonId) {
                R.id.radioFemale -> "Female"
                R.id.radioMale -> "Male"
                else -> "Other"
            }

            if (petName.isEmpty()) {
                Toast.makeText(this,"Enter pet name",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            savePetToFirestore(petName, sex, breed, petType)


        }
    }

    private fun setupImagePicker() {

        binding.imageProfilePet.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun savePetToFirestore(
        petName: String,
        sex: String,
        breed: String,
        petType: String
    ) {

        val userId = mAuth.uid ?: return

        val petRef = db.collection("users")
            .document(userId)
            .collection("pets")
            .document(currentPetId!!)

        val petData = mapOf(
            "petId" to currentPetId,
            "petName" to petName,
            "sex" to sex,
            "breed" to breed,
            "petType" to petType,
            "birthDate" to selectedDate,
            "imagePath" to pathImage,
        )

        petRef.set(petData)
            .addOnSuccessListener {

                Toast.makeText(this,"Pet created successfully!",Toast.LENGTH_LONG).show()
                startActivity(Intent(this, PetResult::class.java))
                finish()

            }
            .addOnFailureListener {

                Toast.makeText(this,"Error saving pet",Toast.LENGTH_LONG).show()

            }
    }

    private fun saveProfileImage(uri: Uri) {

        try {

            val inputStream = contentResolver.openInputStream(uri)

            val fileName = "pet_${UUID.randomUUID()}.jpg"
            val file = File(filesDir, fileName)

            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            pathImage = file.absolutePath

            binding.imageProfilePet.setImageURI(uri)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}