package com.example.petbuddy.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.petbuddy.R

abstract class BaseNavigator(protected val activity: FragmentActivity) {

    protected val fragmentManager = activity.supportFragmentManager
    protected val navigator = FragmentNavigator(fragmentManager)

    // Abstract methods ที่ต้อง implement ใน subclass
    abstract fun navigateToHome()
    abstract fun navigateToFeeding()
    abstract fun navigateToHealth()
    abstract fun navigateToSchedule()
    abstract fun navigateToProfile()

    // Common navigation methods
    fun navigateToWeight() {
        val fragment = com.example.petbuddy.fragment.WeightFragment()
        navigator.navigateTo(fragment, "weight")
    }

    fun navigateToVaccination() {
        val fragment = com.example.petbuddy.fragment.VaccinationFragment()
        navigator.navigateTo(fragment, "vaccination")
    }

    fun navigateToPetSelection(mode: com.example.petbuddy.model.SelectionMode, sourceTag: String) {
        val fragment = com.example.petbuddy.fragment.PetSelectionFragment().apply {
            arguments = android.os.Bundle().apply {
                putSerializable("mode", mode)
                putString("source_tag", sourceTag)
            }
        }
        navigator.navigateTo(fragment, "pet_selection")
    }

    fun navigateToEditPetProfile(pet: com.example.petbuddy.model.Pet) {
        val fragment = com.example.petbuddy.fragment.EditPetProfileFragment(pet)
        navigator.navigateTo(fragment, "edit_pet")
    }

    fun navigateToAllMyPets() {
        val fragment = com.example.petbuddy.fragment.AllMyPetsFragment()
        navigator.navigateTo(fragment, "all_my_pets")
    }

    fun navigateToAddWeight() {
        val fragment = com.example.petbuddy.fragment.AddWeightFragment.newInstance()
        navigator.navigateTo(fragment, "add_weight")
    }

    fun navigateToAddVaccination(record: com.example.petbuddy.model.VaccinationRecord? = null) {
        val fragment = com.example.petbuddy.fragment.AddVaccinationFragment.newInstance(record)
        navigator.navigateTo(fragment, "add_vaccination")
    }

    // Utility methods
    fun goBack(): Boolean = navigator.goBack()

    fun getCurrentTag(): String? = navigator.getCurrentTag()

    fun popToFragment(tag: String, inclusive: Boolean = false) {
        navigator.popToFragment(tag, inclusive)
    }
}