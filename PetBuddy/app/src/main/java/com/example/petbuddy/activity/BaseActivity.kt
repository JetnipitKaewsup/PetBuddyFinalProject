package com.example.petbuddy.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var mAuth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    protected var currentUserId: String? = null

    // SharedPreferences
    private val PREFS_NAME = "pet_buddy_prefs"
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    // ข้อมูลสัตว์เลี้ยงที่ถูกเลือก
    private var _selectedPet: Pet? = null
    private var _selectedPets: MutableList<Pet> = mutableListOf()
    private var _currentMode: SelectionMode = SelectionMode.SINGLE

    // Properties สำหรับให้ Fragment อ่าน
    val selectedPet: Pet? get() = _selectedPet
    val selectedPets: List<Pet> get() = _selectedPets.toList()
    val currentMode: SelectionMode get() = _currentMode
    val hasSelectedPet: Boolean
        get() = when (_currentMode) {
            SelectionMode.SINGLE -> _selectedPet != null
            SelectionMode.MULTIPLE -> _selectedPets.isNotEmpty()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.currentUser?.uid

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // โหลดข้อมูลที่บันทึกไว้
        loadSavedData()

        // ตรวจสอบว่ามี userId หรือไม่
        if (currentUserId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }

    // =====  METHODS FOR SINGLE MODE =====

    fun selectPet(pet: Pet) {
        _selectedPet = pet
        _currentMode = SelectionMode.SINGLE

        // บันทึกลง SharedPreferences
        saveSelectedPet(pet)

        // แจ้ง Fragment ที่กำลังฟังอยู่ (ถ้ามี)
        onPetSelected(pet)
    }

    fun getSelectedPetId(): String? {
        return _selectedPet?.petId ?: loadSavedPetId()
    }

    fun getSelectedPetName(): String? {
        return _selectedPet?.petName ?: loadSavedPetName()
    }

    // =====  METHODS FOR MULTIPLE MODE =====

    fun selectPets(pets: List<Pet>) {
        _selectedPets.clear()
        _selectedPets.addAll(pets)
        _currentMode = SelectionMode.MULTIPLE

        // บันทึกลง SharedPreferences (เฉพาะ petId)
        saveSelectedPetIds(pets.map { it.petId })

        // แจ้ง Fragment
        onPetsSelected(pets)
    }

    fun addPetToSelection(pet: Pet) {
        if (!_selectedPets.any { it.petId == pet.petId }) {
            _selectedPets.add(pet)
            _currentMode = SelectionMode.MULTIPLE

            // บันทึก
            saveSelectedPetIds(_selectedPets.map { it.petId })

            // แจ้ง Fragment
            onPetsSelected(_selectedPets)
        }
    }

    fun removePetFromSelection(petId: String) {
        _selectedPets.removeAll { it.petId == petId }

        if (_selectedPets.isEmpty()) {
            // ถ้าไม่มีสัตว์เลี้ยงเหลือ กลับไป SINGLE mode
            _currentMode = SelectionMode.SINGLE
            clearSelectedPetIds()
        } else {
            // บันทึก
            saveSelectedPetIds(_selectedPets.map { it.petId })
        }

        // แจ้ง Fragment
        onPetsSelected(_selectedPets)
    }

    fun getSelectedPetIds(): List<String> {
        return when (_currentMode) {
            SelectionMode.SINGLE -> listOfNotNull(_selectedPet?.petId)
            SelectionMode.MULTIPLE -> _selectedPets.map { it.petId }
        }
    }

    // =====  MODE MANAGEMENT =====

    fun setMode(mode: SelectionMode) {
        _currentMode = mode
        when (mode) {
            SelectionMode.SINGLE -> {
                _selectedPets.clear()
                clearSelectedPetIds()
            }
            SelectionMode.MULTIPLE -> {
                // ไม่ต้องเคลียร์ selectedPet
            }
        }
        onModeChanged(mode)
    }

    // =====  CLEAR DATA =====

    fun clearSelection() {
        _selectedPet = null
        _selectedPets.clear()
        _currentMode = SelectionMode.SINGLE

        // ลบข้อมูลใน SharedPreferences
        clearAllSavedData()
    }

    // =====  LOAD PETS FROM FIRESTORE =====

    fun loadAllPets(callback: (List<Pet>) -> Unit) {
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

    fun loadPetById(petId: String, callback: (Pet?) -> Unit) {
        currentUserId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("pets")
                .document(petId)
                .get()
                .addOnSuccessListener { document ->
                    val pet = document.toObject(Pet::class.java)
                    callback(pet)
                }
                .addOnFailureListener {
                    callback(null)
                }
        } ?: callback(null)
    }

    // =====  SHAREDPREFERENCES METHODS =====

    private fun saveSelectedPet(pet: Pet) {
        val petJson = gson.toJson(pet)
        sharedPreferences.edit().putString("selected_pet", petJson).apply()
    }

    private fun saveSelectedPetIds(petIds: List<String>) {
        val idsJson = gson.toJson(petIds)
        sharedPreferences.edit().putString("selected_pet_ids", idsJson).apply()
    }

    private fun loadSavedData() {
        // โหลด selected pet
        val petJson = sharedPreferences.getString("selected_pet", null)
        _selectedPet = if (petJson != null) {
            try {
                gson.fromJson(petJson, Pet::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        // โหลด selected pet ids
        val idsJson = sharedPreferences.getString("selected_pet_ids", null)
        if (idsJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val petIds: List<String> = gson.fromJson(idsJson, type)

                // ถ้ามี petId ให้โหลดข้อมูล Pet จาก Firestore
                if (petIds.isNotEmpty()) {
                    loadPetsByIds(petIds) { pets ->
                        _selectedPets.clear()
                        _selectedPets.addAll(pets)
                        _currentMode = SelectionMode.MULTIPLE
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun loadPetsByIds(petIds: List<String>, callback: (List<Pet>) -> Unit) {
        val pets = mutableListOf<Pet>()
        var loadedCount = 0

        if (petIds.isEmpty()) {
            callback(emptyList())
            return
        }

        petIds.forEach { petId ->
            loadPetById(petId) { pet ->
                pet?.let { pets.add(it) }
                loadedCount++

                if (loadedCount == petIds.size) {
                    callback(pets)
                }
            }
        }
    }

    private fun loadSavedPetId(): String? {
        return sharedPreferences.getString("selected_pet_id", null)
    }

    private fun loadSavedPetName(): String? {
        return sharedPreferences.getString("selected_pet_name", null)
    }

    private fun clearSelectedPetIds() {
        sharedPreferences.edit().remove("selected_pet_ids").apply()
    }

    private fun clearAllSavedData() {
        sharedPreferences.edit().clear().apply()
    }

    // =====  CALLBACK METHODS (ให้ Fragment Override ได้) =====

    protected open fun onPetSelected(pet: Pet) {
        // ให้ Fragment ที่ต้องการรับ event override
    }

    protected open fun onPetsSelected(pets: List<Pet>) {
        // ให้ Fragment ที่ต้องการรับ event override
    }

    protected open fun onModeChanged(mode: SelectionMode) {
        // ให้ Fragment ที่ต้องการรับ event override
    }

    // =====  HELPER METHODS FOR FRAGMENTS =====

    fun getCurrentUserIdSafe(): String? = currentUserId

    fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}