package com.example.petbuddy

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.ActivityMainBinding
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.util.Constants

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var navigator: MainNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        // สร้าง Navigator
        navigator = MainNavigator(this)

        init()

        // โหลดข้อมูล user
        loadUserInfo()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            navigator.navigateToHome()
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    handleHomeSelected()
                    true
                }
                R.id.nav_feeding -> {
                    handleFeedingSelected()
                    true
                }
                R.id.nav_health -> {
                    handleHealthSelected()
                    true
                }
                R.id.nav_schedule -> {
                    handleScheduleSelected()
                    true
                }
                R.id.nav_profile -> {
                    handleProfileSelected()
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navigator.navigateToRoot()
                }
            }
        }
    }

    private fun init(){
        binding.userProfileHeader.setOnClickListener {
            navigator.navigateToProfile()
        }
    }

    private fun handleHomeSelected() {
        val currentTag = navigator.getCurrentTag()
        if (currentTag != MainNavigator.TAG_HOME) {
            navigator.navigateToRoot()
        }
    }

    private fun handleFeedingSelected() {
        // ไปที่ FeedingFragment โดยตรง
        navigator.navigateToFeeding()
    }

    private fun handleHealthSelected() {
        if (hasSelectedPet) {
            // มีสัตว์เลี้ยงที่เลือกแล้ว ไปหน้า Health
            navigator.navigateToHealth()
        } else {
            // ยังไม่มี ไปเลือกสัตว์เลี้ยงก่อน
            navigator.navigateToPetSelection(SelectionMode.SINGLE, Constants.TAG_HEALTH_DASHBOARD)
        }
    }

    private fun handleScheduleSelected() {
        navigator.navigateToSchedule()
    }

    private fun handleProfileSelected() {
        navigator.navigateToProfile()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Leave the app")
            .setMessage("Are you sure you want to leave the app?")
            .setPositiveButton("yes") { _, _ ->
                finish()
            }
            .setNegativeButton("no", null)
            .show()
    }

    fun showMessage(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun loadUserInfo() {
        val userId = currentUserId
        if (userId == null) {
            showToast("ไม่พบข้อมูลผู้ใช้")
            return
        }

        db.collection("users")
            .document(userId)
            .collection("userInfo")
            .document("profile")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("username") ?: "Unknown User"
                    binding.tvUserName.text = userName

                    val profileImageUrl = document.getString("profileImage")
                    loadProfileImage(profileImageUrl)
                } else {
                    showToast("ไม่พบข้อมูลโปรไฟล์")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error loading user info", e)
                showToast("ไม่สามารถโหลดข้อมูลผู้ใช้ได้")
            }
    }

    private fun loadProfileImage(imageUrl: String?) {
        val imageView = binding.ivUserProfile

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .circleCrop()
                )
                .into(imageView)
        } else {
            Glide.with(this)
                .load(R.drawable.user_placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)
        }
    }

    fun refreshUserInfo() {
        loadUserInfo()
    }
}