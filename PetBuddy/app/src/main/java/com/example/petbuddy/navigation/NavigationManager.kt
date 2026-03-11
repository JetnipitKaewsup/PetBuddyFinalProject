package com.example.petbuddy.navigation

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.example.petbuddy.R
import com.example.petbuddy.fragment.AllMyPetsFragment
import com.example.petbuddy.fragment.EditPetProfileFragment
import com.example.petbuddy.fragment.HealthDashboardFragment
import com.example.petbuddy.fragment.PetSelectionFragment
import com.example.petbuddy.fragment.ScheduleFragment
import com.example.petbuddy.fragment.FeedingFragment
import com.example.petbuddy.fragments.home.HomeFragment
import com.example.petbuddy.fragments.profile.ProfileFragment
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode

class NavigationManager(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {
    companion object {
        const val TAG_HOME = "HOME"
        const val TAG_FEEDING = "FEEDING"
        const val TAG_HEALTH_DASHBOARD = "HEALTH_DASHBOARD"
        const val TAG_SCHEDULE = "SCHEDULE"
        const val TAG_PROFILE = "PROFILE"
        const val TAG_PET_SELECTION = "PET_SELECTION"
        const val TAG_ALL_MY_PETS = "ALL_MY_PETS"
        const val TAG_EDIT_PET_PROFILE = "EDIT_PET_PROFILE"
        const val TAG_WEIGHT = "WEIGHT"
        const val TAG_VACCINATION = "VACCINATION"
    }

    fun navigateToHome() {
        replaceFragment(HomeFragment(), addToBackStack = false, tag = TAG_HOME)
    }

    fun navigateToFeeding() {
        replaceFragment(FeedingFragment(), addToBackStack = true, tag = TAG_FEEDING)
    }

    fun navigateToHealthDashboard() {
        replaceFragment(HealthDashboardFragment(), addToBackStack = true, tag = TAG_HEALTH_DASHBOARD)
    }

    fun navigateToSchedule() {
        replaceFragment(ScheduleFragment(), addToBackStack = true, tag = TAG_SCHEDULE)
    }

    fun navigateToProfile() {
        replaceFragment(ProfileFragment(), addToBackStack = true, tag = TAG_PROFILE)
    }

    fun navigateToPetSelection(mode: SelectionMode, sourceTag: String) {
        val fragment = PetSelectionFragment().apply {
            arguments = Bundle().apply {
                putSerializable("mode", mode)
                putString("source_tag", sourceTag)
            }
        }
        replaceFragment(fragment, addToBackStack = true, tag = TAG_PET_SELECTION)
    }

    fun navigateToAllMyPets(){
        replaceFragment(AllMyPetsFragment(), addToBackStack = true, tag = TAG_ALL_MY_PETS)
    }

    fun navigateToEditPetProfile(pet : Pet){
        replaceFragment(EditPetProfileFragment(pet),addToBackStack = true,tag = TAG_EDIT_PET_PROFILE)
    }

    fun navigateToRoot() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        navigateToHome()
    }

    fun goBack(): Boolean {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    fun getCurrentTag(): String? {
        val currentFragment = fragmentManager.findFragmentById(containerId)
        return currentFragment?.tag
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment, addToBackStack: Boolean, tag: String) {
        val transaction = fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(containerId, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.commit()
    }
}