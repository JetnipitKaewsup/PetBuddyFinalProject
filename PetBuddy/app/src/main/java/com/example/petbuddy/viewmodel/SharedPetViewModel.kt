package com.example.petbuddy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.PetDataManager
import com.example.petbuddy.model.SelectionMode


class SharedPetViewModel (application: Application): AndroidViewModel(application) {
        private val petDataManager = PetDataManager(application)

        // ----- Select Pet - SINGLE MODE ------
        private val _selectedPet = MutableLiveData<Pet?>()
        val selectedPet: LiveData<Pet?> = _selectedPet

        // ----- Select Pet - MULTIPLE MODE ------
        private val _selectedPets = MutableLiveData<List<Pet>>(emptyList())
        val selectedPets: LiveData<List<Pet>> = _selectedPets

        // ------ โหมดการเลือกปัจจุบัน --------
        private val _currentMode = MutableLiveData<SelectionMode>(SelectionMode.SINGLE)
        val currentMode: LiveData<SelectionMode> = _currentMode

        init {
            // โหลดข้อมูลจาก SharedPreferences เมื่อเริ่ม ViewModel
            loadSavedPet()
        }

        /**
         * โหลดข้อมูลสัตว์เลี้ยงที่บันทึกไว้
         */
        private fun loadSavedPet() {
            val savedPet = petDataManager.getCurrentPet()
            _selectedPet.value = savedPet
        }

        // SINGLE MODE
        fun selectPet(pet: Pet) {
            _selectedPet.value = pet
            // บันทึกลง SharedPreferences
            petDataManager.saveCurrentPet(pet)
        }

        fun getSelectedPetId(): String? {
            return _selectedPet.value?.id ?: petDataManager.getCurrentPetId()
        }

        fun getSelectedPetName(): String? {
            return _selectedPet.value?.name ?: petDataManager.getCurrentPetName()
        }

        // MULTIPLE MODE
        fun selectPets(pets: List<Pet>) {
            _selectedPets.value = pets
        }

        fun getSelectedPetIds(): List<String> {
            return _selectedPets.value?.map { it.id } ?: emptyList()
        }

        fun addPetToSelection(pet: Pet) {
            val currentList = _selectedPets.value?.toMutableList() ?: mutableListOf()
            if (!currentList.any { it.id == pet.id }) {
                currentList.add(pet)
                _selectedPets.value = currentList
            }
        }

        fun removePetFromSelection(petId: String) {
            val currentList = _selectedPets.value?.toMutableList() ?: return
            currentList.removeAll { it.id == petId }
            _selectedPets.value = currentList
        }

        fun setMode(mode: SelectionMode) {
            _currentMode.value = mode
            when (mode) {
                SelectionMode.SINGLE -> {
                    _selectedPets.value = emptyList()
                }
                SelectionMode.MULTIPLE -> {
                    // ไม่เคลียร์ selectedPet ใน MULTIPLE mode
                }
            }
        }

        fun clearSelection() {
            _selectedPet.value = null
            _selectedPets.value = emptyList()
            petDataManager.clearCurrentPet()
        }

        fun hasSelectedPet(): Boolean {
            return when (_currentMode.value) {
                SelectionMode.SINGLE -> _selectedPet.value != null || petDataManager.hasCurrentPet()
                SelectionMode.MULTIPLE -> !_selectedPets.value.isNullOrEmpty()
                else -> false
            }
        }

}