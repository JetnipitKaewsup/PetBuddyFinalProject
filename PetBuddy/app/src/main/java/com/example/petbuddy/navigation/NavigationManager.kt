package com.example.petbuddy.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.petbuddy.R
import com.example.petbuddy.fragment.HealthDashboardFragment
import com.example.petbuddy.fragment.PetSelectionFragment
import com.example.petbuddy.fragment.ScheduleFragment
import com.example.petbuddy.fragment.VaccinationFragment
import com.example.petbuddy.fragment.WeightFragment
import com.example.petbuddy.fragments.feeding.FeedingFragment
import com.example.petbuddy.fragments.home.HomeFragment
import com.example.petbuddy.fragments.profile.ProfileFragment

import com.example.petbuddy.model.SelectionMode

// ใช้จัดการการนำทางระหว่าง fragment
class NavigationManager(
    private val fragmentManager: FragmentManager,  // FragmentManager จาก Activity
    private val containerId: Int                   // ID ของ container ที่จะวาง Fragment
) {

    companion object {
        const val TAG_HOME = "HOME"
        const val TAG_FEEDING = "FEEDING"
        const val TAG_PET_SELECTION = "PET_SELECTION"
        const val TAG_HEALTH_DASHBOARD = "HEALTH_DASHBOARD"
        const val TAG_WEIGHT = "WEIGHT"
        const val TAG_VACCINATION = "VACCINATION"
        const val TAG_SCHEDULE = "SCHEDULE"
        const val TAG_PROFILE = "PROFILE"
    }
    // เปลี่ยนไปยัง fragment ต่างๆ
    fun navigateTo(
        fragment: Fragment,
        tag: String,
        addToBackStack: Boolean = true,
        data: Bundle? = null
    ) {
        fragment.arguments = data

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


    fun navigateToHome() {
        navigateTo(HomeFragment(), TAG_HOME)
    }


    fun navigateToFeeding() {
        navigateTo(FeedingFragment(), TAG_FEEDING)
    }


    fun navigateToPetSelection(mode: SelectionMode) {
        val fragment = PetSelectionFragment()
        val data = Bundle().apply {
            putSerializable("mode", mode)
        }
        navigateTo(fragment, TAG_PET_SELECTION, data = data)
    }


    fun navigateToHealthDashboard() {
        navigateTo(HealthDashboardFragment(), TAG_HEALTH_DASHBOARD)
    }


    fun navigateToWeight() {
        navigateTo(WeightFragment(), TAG_WEIGHT)
    }


    fun navigateToVaccination() {
        navigateTo(VaccinationFragment(), TAG_VACCINATION)
    }


    fun navigateToSchedule() {
        navigateTo(ScheduleFragment(), TAG_SCHEDULE)
    }


    fun navigateToProfile() {
        navigateTo(ProfileFragment(), TAG_PROFILE)
    }

    /**
     * กลับไปหน้าก่อนหน้า
     * @return true ถ้ากลับได้, false ถ้าไม่มีหน้าให้กลับ
     */
    fun goBack(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    /* กลับไปหน้า home */
    fun navigateToRoot() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        navigateToHome()
    }

    /**
     * รับ Fragment ที่กำลังแสดงอยู่
     */
    fun getCurrentFragment(): Fragment? {
        return fragmentManager.findFragmentById(containerId)
    }

    /**
     * รับ Tag ของ Fragment ปัจจุบัน
     */
    fun getCurrentTag(): String? {
        return getCurrentFragment()?.tag
    }
}