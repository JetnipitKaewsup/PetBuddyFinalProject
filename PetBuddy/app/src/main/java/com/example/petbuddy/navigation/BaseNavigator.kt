package com.example.petbuddy.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.petbuddy.R
import com.example.petbuddy.fragment.AddEventFragment
import com.example.petbuddy.fragment.EditUserProfileFragment
import com.example.petbuddy.fragment.EventDetailFragment
import com.example.petbuddy.model.Event
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.model.VaccinationRecord
import com.example.petbuddy.model.WeightRecord
import java.time.LocalDate

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

    fun navigateToPetSelectionForEvent(
        mode: SelectionMode,
        sourceTag: String,
        requestKey: String,
        selectedPetIds: List<String> = emptyList()
    ) {
        val fragment = com.example.petbuddy.fragment.PetSelectionFragment().apply {
            arguments = android.os.Bundle().apply {
                putSerializable("mode", mode)
                putString("source_tag", sourceTag)
                putString("request_key", requestKey)
                if (selectedPetIds.isNotEmpty()) {
                    putStringArrayList("selected_pet_ids", ArrayList(selectedPetIds))
                }
            }
        }
        navigator.navigateTo(fragment, "pet_selection")
    }

    fun navigateToEditPetProfile(pet: com.example.petbuddy.model.Pet) {
        val fragment = com.example.petbuddy.fragment.EditPetProfileFragment.newInstance(pet)
        navigator.navigateTo(fragment, "edit_pet_profile")
    }

    fun navigateToEditUserProfile(){
        val fragment = EditUserProfileFragment()
        navigator.navigateTo(fragment,"edit_user_profile")
    }
    fun navigateToAllMyPets() {
        val fragment = com.example.petbuddy.fragment.AllMyPetsFragment()
        navigator.navigateTo(fragment, "all_my_pets")
    }

    fun navigateToAddWeight(record: WeightRecord? = null) {
        val fragment = com.example.petbuddy.fragment.AddWeightFragment.newInstance()
        navigator.navigateTo(fragment, "add_weight")
    }

    fun navigateToAddVaccination(record: VaccinationRecord? = null) {
        val fragment = com.example.petbuddy.fragment.AddVaccinationFragment.newInstance(record)
        navigator.navigateTo(fragment, "add_vaccination")
    }

    fun navigateToAddEvent(selectedDate: LocalDate? = null) {
        val fragment = AddEventFragment.newInstance(
            selectedDate = selectedDate?.let {
                java.util.Date.from(it.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
            }
        )
        navigator.navigateTo(fragment, "add_event")
    }
    fun navigateToEventDetail(event: Event) {
        val fragment = EventDetailFragment.newInstance(event)
        navigator.navigateTo(fragment, "event_detail")
    }

    fun navigateToEditEvent(event: Event) {
        val fragment = AddEventFragment.newInstance(existingEvent = event)
        navigator.navigateTo(fragment, "add_event")
    }
    // Utility methods
    fun goBack(): Boolean = navigator.goBack()

    fun getCurrentTag(): String? = navigator.getCurrentTag()

    fun popToFragment(tag: String, inclusive: Boolean = false) {
        navigator.popToFragment(tag, inclusive)
    }
}