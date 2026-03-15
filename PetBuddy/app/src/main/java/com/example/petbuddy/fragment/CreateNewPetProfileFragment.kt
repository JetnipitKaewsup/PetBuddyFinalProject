package com.example.petbuddy.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.api.CatBreed
import com.example.petbuddy.api.DogResponse
import com.example.petbuddy.api.RetrofitClient
import com.example.petbuddy.databinding.FragmentCreateNewPetProfileBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.supabase.SupabaseClient
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CreateNewPetProfileFragment : Fragment() {

    private var _binding: FragmentCreateNewPetProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator
    private lateinit var mAuth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var selectedDate: Timestamp? = null
    private var selectedImageUri: Uri? = null
    private var currentPetId: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.imageProfilePet.setImageURI(it)
            // Preview image
            Glide.with(this)
                .load(it)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.pet_placeholder)
                        .error(R.drawable.pet_placeholder)
                        .circleCrop()
                )
                .into(binding.imageProfilePet)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNewPetProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPetId = UUID.randomUUID().toString()

        setupToolbar()
        setupDropdown()
        setupDatePicker()
        setupCreateButton()
        setupImagePicker()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupDropdown() {
        val pets = resources.getStringArray(R.array.pet)
        val petAdapter = ArrayAdapter(requireContext(), R.layout.list_item, pets)
        binding.petDropdown.setAdapter(petAdapter)

        binding.petDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedPet = parent.getItemAtPosition(position).toString()
            when (selectedPet) {
                "Dog" -> loadDogBreeds()
                "Cat" -> loadCatBreeds()
            }
        }
    }

    private fun loadDogBreeds() {
        RetrofitClient.dogApi.getDogBreeds()
            .enqueue(object : Callback<DogResponse> {
                override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                    if (isAdded) {
                        if (response.isSuccessful) {
                            val breeds = response.body()?.message?.keys?.toList() ?: emptyList()
                            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, breeds)
                            binding.breedDropdown.setAdapter(adapter)
                        } else {
                            Toast.makeText(requireContext(), "Failed to load dog breeds", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to load dog breeds", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun loadCatBreeds() {
        RetrofitClient.catApi.getCatBreeds()
            .enqueue(object : Callback<List<CatBreed>> {
                override fun onResponse(call: Call<List<CatBreed>>, response: Response<List<CatBreed>>) {
                    if (isAdded) {
                        if (response.isSuccessful) {
                            val breeds = response.body()?.map { it.name } ?: emptyList()
                            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, breeds)
                            binding.breedDropdown.setAdapter(adapter)
                        } else {
                            Toast.makeText(requireContext(), "Failed to load cat breeds", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<CatBreed>>, t: Throwable) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to load cat breeds", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun setupDatePicker() {
        binding.petDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
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
            ).show()
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
                Toast.makeText(requireContext(), "Enter pet name", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (petType.isEmpty()) {
                Toast.makeText(requireContext(), "Select pet type", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (sex == "Other") {
                Toast.makeText(requireContext(), "Select gender", Toast.LENGTH_LONG).show()
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
        val userId = baseActivity.getCurrentUserIdSafe() ?: run {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_LONG).show()
            return
        }

        // Disable button while saving
        binding.crePetBtn.isEnabled = false
        binding.crePetBtn.text = "Saving..."

        if (selectedImageUri != null) {
            // มีรูป -> อัปโหลดก่อน
            uploadImageAndSave(userId, petName, sex, breed, petType)
        } else {
            // ไม่มีรูป -> บันทึกอย่างเดียว
            savePetData(userId, petName, sex, breed, petType, null)
        }
    }

    private fun uploadImageAndSave(
        userId: String,
        petName: String,
        sex: String,
        breed: String,
        petType: String
    ) {
        lifecycleScope.launch {
            try {
                val bytes = requireContext().contentResolver.openInputStream(selectedImageUri!!)?.use {
                    it.readBytes()
                } ?: throw Exception("Image read failed")

                val bucket = SupabaseClient.client.storage.from("picture")
                val filePath = "pet/${currentPetId}.jpg"

                bucket.upload(
                    path = filePath,
                    data = bytes,
                    upsert = true
                )

                val imageUrl = bucket.publicUrl(filePath)

                savePetData(userId, petName, sex, breed, petType, imageUrl)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Image upload failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.crePetBtn.isEnabled = true
                binding.crePetBtn.text = "Create Pet"
            }
        }
    }

    private fun savePetData(
        userId: String,
        petName: String,
        sex: String,
        breed: String,
        petType: String,
        imageUrl: String?
    ) {
        val pet = Pet(
            petId = currentPetId ?: UUID.randomUUID().toString(),
            petName = petName,
            sex = sex,
            breed = breed,
            petType = petType,
            birthDate = selectedDate,
            imageUrl = imageUrl
        )

        db.collection("users")
            .document(userId)
            .collection("pets")
            .document(pet.petId)
            .set(pet)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pet created successfully!", Toast.LENGTH_LONG).show()

                // Select this pet automatically
                // baseActivity.selectPet(pet)

                // Navigate back
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error saving pet: ${e.message}", Toast.LENGTH_LONG).show()
                binding.crePetBtn.isEnabled = true
                binding.crePetBtn.text = "Create Pet"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}