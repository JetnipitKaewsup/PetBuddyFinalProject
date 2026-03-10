package com.example.petbuddy.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.petbuddy.R

class FragmentNavigator(private val fragmentManager: FragmentManager) {

    /**
     * เปลี่ยน Fragment แบบมี Animation
     */
    fun navigateTo(
        fragment: Fragment,
        tag: String,
        addToBackStack: Boolean = true,
        containerId: Int = R.id.fragment_container
    ) {
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

    /**
     * เปลี่ยน Fragment แบบไม่มี Animation
     */
    fun navigateToNoAnimation(
        fragment: Fragment,
        tag: String,
        addToBackStack: Boolean = true,
        containerId: Int = R.id.fragment_container
    ) {
        val transaction = fragmentManager.beginTransaction()
            .replace(containerId, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.commit()
    }

    /**
     * เปลี่ยน Fragment และ clear back stack
     */
    fun navigateAndClearBackStack(
        fragment: Fragment,
        tag: String,
        containerId: Int = R.id.fragment_container
    ) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(containerId, fragment, tag)
            .commit()
    }

    /**
     * กลับไปหน้าก่อนหน้า
     */
    fun goBack(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    /**
     * กลับไปยัง Fragment ตาม tag
     */
    fun popToFragment(tag: String, inclusive: Boolean = false) {
        fragmentManager.popBackStack(tag, if (inclusive) 0 else FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    /**
     * ดึง Fragment ปัจจุบัน
     */
    fun getCurrentFragment(containerId: Int = R.id.fragment_container): Fragment? {
        return fragmentManager.findFragmentById(containerId)
    }

    /**
     * ดึง tag ของ Fragment ปัจจุบัน
     */
    fun getCurrentTag(containerId: Int = R.id.fragment_container): String? {
        return fragmentManager.findFragmentById(containerId)?.tag
    }
}