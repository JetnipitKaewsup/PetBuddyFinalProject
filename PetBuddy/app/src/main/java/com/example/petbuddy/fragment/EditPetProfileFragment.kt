package com.example.petbuddy.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.petbuddy.databinding.FragmentEditPetProfileBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.supabase.SupabaseClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

class EditPetProfileFragment : Fragment() {

    private var _binding: FragmentEditPetProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator

    private var pet: Pet? = null
    private var selectedDate: Timestamp? = null
    private var selectedImageUri: Uri? = null
    private var isImageChanged = false
    private var isDataChanged = false

    // ค่าเดิมสำหรับตรวจสอบการเปลี่ยนแปลง
    private var originalPetName: String = ""
    private var originalPetType: String = ""
    private var originalBreed: String = ""
    private var originalSex: String = ""
    private var originalBirthDate: Timestamp? = null
    private var originalImagePath: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            isImageChanged = true
            isDataChanged = true
            binding.imageProfile.setImageURI(it)
            checkIfChangesMade()
        }
    }

    companion object {
        private const val ARG_PET = "pet"

        fun newInstance(pet: Pet): EditPetProfileFragment {
            return EditPetProfileFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PET, pet)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pet = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_PET, Pet::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_PET) as? Pet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPetProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDropdowns()
        setupDatePicker()
        setupImagePicker()
        setupTextWatchers()
        loadPetData()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            if (isDataChanged) {
                showDiscardChangesDialog()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
        binding.toolbar.title = "Edit Pet Profile"
    }

    private fun setupDropdowns() {
        // Pet Type Dropdown
        val pets = resources.getStringArray(R.array.pet)
        val petAdapter = ArrayAdapter(requireContext(), R.layout.list_item, pets)
        binding.petDropdown.setAdapter(petAdapter)

        binding.petDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedPet = pets[position]
            if (selectedPet == "Dog") {
                loadDogBreeds()
            } else if (selectedPet == "Cat") {
                loadCatBreeds()
            }
            checkDataChanged()
        }

        // Breed Dropdown (จะโหลดหลังจากเลือก pet type)
        binding.breedDropdown.setOnItemClickListener { _, _, _, _ ->
            checkDataChanged()
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

                        }.map { breed ->
                            breed.replaceFirstChar { it.uppercase() }
                        }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.list_item,
                            breeds
                        )

                        binding.breedDropdown.setAdapter(adapter)

                        // restore ค่าเดิม
                        if (originalPetType == "Dog" && originalBreed.isNotEmpty()) {
                            binding.breedDropdown.setText(originalBreed, false)
                        }
                    }
                }

                override fun onFailure(call: Call<DogResponse>, t: Throwable) {

                    Toast.makeText(
                        requireContext(),
                        "Failed to load dog breeds",
                        Toast.LENGTH_LONG
                    ).show()

                }
            })
    }

    private fun loadCatBreeds() {
        RetrofitClient.catApi.getCatBreeds()
            .enqueue(object : Callback<List<CatBreed>> {
                override fun onResponse(call: Call<List<CatBreed>>, response: Response<List<CatBreed>>) {
                    if (response.isSuccessful) {
                        val breeds = response.body()?.map { it.name } ?: emptyList()
                        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, breeds)
                        binding.breedDropdown.setAdapter(adapter)

                        // ถ้ามี breed เดิม ให้เลือกไว้
                        if (originalPetType == "Cat" && originalBreed.isNotEmpty()) {
                            binding.breedDropdown.setText(originalBreed, false)
                        }
                    }
                }

                override fun onFailure(call: Call<List<CatBreed>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to load cat breeds", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setupDatePicker() {
        binding.petDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (selectedDate != null) {
                calendar.time = selectedDate!!.toDate()
            }

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedDate = Timestamp(selectedCalendar.time)
                    binding.petDate.setText("$day/${month + 1}/$year")
                    checkDataChanged()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupImagePicker() {
        binding.imageProfile.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.txtEdtProfile.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun setupTextWatchers() {
        // Pet Name TextWatcher
        binding.crePUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkDataChanged()
            }
        })

        // Gender RadioGroup listener
        binding.radioGroupSex.setOnCheckedChangeListener { _, _ ->
            checkDataChanged()
        }
    }

    private fun loadPetData() {
        pet?.let { currentPet ->
            // เก็บค่าเดิม
            originalPetName = currentPet.petName
            originalPetType = currentPet.petType
            originalBreed = currentPet.breed
            originalSex = currentPet.sex
            originalBirthDate = currentPet.birthDate
            originalImagePath = currentPet.imagePath

            // แสดงข้อมูล
            binding.crePUser.setText(originalPetName)
            binding.petDropdown.setText(originalPetType, false)

            // แสดงวันเกิด
            originalBirthDate?.let {
                val date = it.toDate()
                val calendar = Calendar.getInstance().apply { time = date }
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                binding.petDate.setText("$day/$month/$year")
                selectedDate = it
            }

            // เลือกเพศ
            when (originalSex.lowercase()) {
                "female" -> binding.radioFemale.isChecked = true
                "male" -> binding.radioMale.isChecked = true
                else -> {
                    // ไม่เลือกอะไรเลย
                }
            }

            // โหลดรูป
            if (!originalImagePath.isNullOrEmpty()) {
                Glide.with(this)
                    .load(originalImagePath)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.pet_placeholder)
                            .error(R.drawable.pet_placeholder)
                            .circleCrop()
                    )
                    .into(binding.imageProfile)
            }

            // โหลด breeds ตาม pet type
            if (originalPetType == "Dog") {
                loadDogBreeds()
                // จะมีการ set breed หลังจากโหลดเสร็จใน callback
            } else if (originalPetType == "Cat") {
                loadCatBreeds()
                // จะมีการ set breed หลังจากโหลดเสร็จใน callback
            } else {
                // ถ้าไม่ใช่ Dog หรือ Cat ให้ set breed ทันที
                binding.breedDropdown.setText(originalBreed, false)
            }
        }
    }

    private fun checkDataChanged(): Boolean {
        val currentName = binding.crePUser.text.toString()
        val currentType = binding.petDropdown.text.toString()
        val currentBreed = binding.breedDropdown.text.toString()
        val currentSex = when (binding.radioGroupSex.checkedRadioButtonId) {
            R.id.radioFemale -> "Female"
            R.id.radioMale -> "Male"
            else -> ""
        }

        isDataChanged = currentName != originalPetName ||
                currentType != originalPetType ||
                currentBreed != originalBreed ||
                currentSex != originalSex ||
                selectedDate != originalBirthDate ||
                isImageChanged

        binding.btnSaveChanges.isEnabled = isDataChanged
        return isDataChanged
    }

    private fun checkIfChangesMade() {
        binding.btnSaveChanges.isEnabled = isDataChanged
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard changes")
            .setMessage("You have unsaved changes. Are you sure you want to leave?")
            .setPositiveButton("Leave") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun setupSaveButton() {
        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val petName = binding.crePUser.text.toString()
        val petType = binding.petDropdown.text.toString()
        val breed = binding.breedDropdown.text.toString()
        val sex = when (binding.radioGroupSex.checkedRadioButtonId) {
            R.id.radioFemale -> "Female"
            R.id.radioMale -> "Male"
            else -> "Other"
        }

        // ตรวจสอบข้อมูล
        if (petName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter pet name", Toast.LENGTH_SHORT).show()
            return
        }
        if (petType.isEmpty()) {
            Toast.makeText(requireContext(), "Please select pet type", Toast.LENGTH_SHORT).show()
            return
        }
        if (sex == "Other") {
            Toast.makeText(requireContext(), "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        // disable ปุ่มระหว่างบันทึก
        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving..."

        if (isImageChanged && selectedImageUri != null) {
            // อัปโหลดรูปใหม่ก่อน แล้วค่อยอัปเดตข้อมูล
            uploadImageAndUpdateProfile(petName, sex, breed, petType)
        } else {
            // อัปเดตเฉพาะข้อมูล
            updatePetInFirestore(petName, sex, breed, petType, originalImagePath)
        }
    }

    private fun uploadImageAndUpdateProfile(
        petName: String,
        sex: String,
        breed: String,
        petType: String
    ) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return
        val petId = pet?.petId ?: return

        lifecycleScope.launch {
            try {
                // อ่านรูปเป็น bytes
                val bytes = requireContext().contentResolver.openInputStream(selectedImageUri!!)?.use {
                    it.readBytes()
                } ?: throw Exception("Failed to read image")

                // อัปโหลดไปยัง Supabase
                val bucket = SupabaseClient.client.storage.from("picture")
                val fileName = "pet_$petId.jpg"

                bucket.upload(
                    path = fileName,
                    data = bytes,
                    upsert = true  // แทนที่ไฟล์เดิม
                )

                // ได้ public URL
                val imagePath = bucket.publicUrl(fileName)

                // อัปเดท Firestore
                updatePetInFirestore(petName, sex, breed, petType, imagePath)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save changes"
            }
        }
    }

    private fun updatePetInFirestore(
        petName: String,
        sex: String,
        breed: String,
        petType: String,
        imagePath: String?
    ) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return
        val petId = pet?.petId ?: return

        // เตรียมข้อมูลที่จะอัปเดท
        val updates = hashMapOf<String, Any>(
            "petName" to petName,
            "sex" to sex,
            "breed" to breed,
            "petType" to petType,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (selectedDate != null) {
            updates["birthDate"] = selectedDate!!
        }

        if (imagePath != null) {
            updates["imagePath"] = imagePath
        }

        baseActivity.db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pet updated successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save changes"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}