package com.example.petbuddy.navigation

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.petbuddy.fragment.FeedingFragment
//import com.example.petbuddy.fragments.FeedingFragment
import com.example.petbuddy.fragment.HomeFragment
import com.example.petbuddy.fragments.profile.ProfileFragment
import com.example.petbuddy.fragment.ScheduleFragment
import com.example.petbuddy.fragment.HealthDashboardFragment

class MainNavigator(activity: FragmentActivity) : BaseNavigator(activity) {

    companion object {
        const val TAG_HOME = "HOME"
        const val TAG_FEEDING = "FEEDING"
        const val TAG_HEALTH = "HEALTH_DASHBOARD"
        const val TAG_SCHEDULE = "SCHEDULE"
        const val TAG_PROFILE = "PROFILE"
        const val TAG_PET_SELECTION = "PET_SELECTION"
        const val TAG_ALL_MY_PETS = "ALL_MY_PETS"
        const val TAG_EDIT_PET_PROFILE = "EDIT_PET_PROFILE"
        const val TAG_WEIGHT = "WEIGHT"
        const val TAG_VACCINATION = "VACCINATION"
    }

    override fun navigateToHome() {
        val fragment = HomeFragment()
        navigator.navigateTo(fragment, TAG_HOME, addToBackStack = false)
    }

    override fun navigateToFeeding() {
        val fragment = FeedingFragment()
        navigator.navigateTo(fragment, TAG_FEEDING)
    }

    override fun navigateToHealth() {
        val fragment = HealthDashboardFragment()
        navigator.navigateTo(fragment, TAG_HEALTH)
    }

    override fun navigateToSchedule() {
        val fragment = ScheduleFragment()
        navigator.navigateTo(fragment, TAG_SCHEDULE)
    }

    override fun navigateToProfile() {
        val fragment = ProfileFragment()
        navigator.navigateTo(fragment, TAG_PROFILE)
    }

    fun navigateToRoot() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        navigateToHome()
    }
}