package com.example.petbuddy

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.ActivityMainBinding
import com.example.petbuddy.navigation.NavigationManager
import com.example.petbuddy.viewmodel.SharedPetViewModel
import com.example.petbuddy.model.SelectionMode
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var navigationManager: NavigationManager
    private lateinit var sharedViewModel: SharedPetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // สร้าง shared viewmodel
        sharedViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SharedPetViewModel::class.java]
        // สร้าง Navigation Manager
        navigationManager = NavigationManager(
            supportFragmentManager,
            R.id.fragment_container
        )
        // โหลดข้อมูล user
        loadUserInfo()
        setupBottomNavigation()
        observeViewModel()

        if (savedInstanceState == null) {
            navigationManager.navigateToHome()
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }



    }

    /**
     * ตั้งค่า Bottom Navigation
     */
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

        // เมื่อกดซ้ำที่เมนูเดิม
        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navigationManager.navigateToRoot()
                }
                R.id.nav_health -> {
                    handleHealthReselected()
                }
            }
        }
    }

    /**
     * สังเกตการเปลี่ยนแปลงใน ViewModel
     */
    private fun observeViewModel() {
        // เมื่อมีการเลือกสัตว์เลี้ยงตัวเดียว
        sharedViewModel.selectedPet.observe(this) { pet ->
            pet?.let {
                // มีสัตว์เลี้ยงถูกเลือก → ไป Health Dashboard
                navigationManager.navigateToHealthDashboard()
            }
        }

        // เมื่อมีการเลือกสัตว์เลี้ยงหลายตัว
        sharedViewModel.selectedPets.observe(this) { pets ->
            if (pets.isNotEmpty()) {
                // มีสัตว์เลี้ยงถูกเลือกหลายตัว
                // ตรวจสอบว่าเป็นเมนูอะไร
                when (sharedViewModel.currentMode.value) {
                    SelectionMode.MULTIPLE -> {
                        // TODO: ไป Feeding หรือ Schedule
                        showMessage("เลือกสัตว์เลี้ยง ${pets.size} ตัว")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleHomeSelected() {
        val currentTag = navigationManager.getCurrentTag()
        if (currentTag != NavigationManager.TAG_HOME) {
            navigationManager.navigateToRoot()
        }
    }

    private fun handleFeedingSelected() {
        navigationManager.navigateToFeeding() //ไปยังหน้า feeding
        sharedViewModel.setMode(SelectionMode.MULTIPLE)
        /*
        if (sharedViewModel.hasSelectedPet()) {
            // มีข้อมูลเก่า ไปหน้า Feeding เลย
            navigationManager.navigateToFeeding()
        } else {
            // ไม่มีข้อมูล ไปเลือกสัตว์เลี้ยงก่อน
            navigationManager.navigateToPetSelection(SelectionMode.MULTIPLE)
        }*/
    }

    private fun handleHealthSelected() {
        if (sharedViewModel.hasSelectedPet()) {
            // มีข้อมูลเก่าอยู่จึง ไปหน้า Health เลย
            navigationManager.navigateToHealthDashboard()
        } else {
            // ไม่มีข้อมูล ไปเลือกสัตว์เลี้ยงก่อน
            navigationManager.navigateToPetSelection(SelectionMode.SINGLE)
        }

    }

    private fun handleScheduleSelected() {
        sharedViewModel.setMode(SelectionMode.MULTIPLE)


        if (sharedViewModel.hasSelectedPet()) {
            navigationManager.navigateToSchedule()
        } else {
            navigationManager.navigateToPetSelection(SelectionMode.MULTIPLE)
        }
    }

    private fun handleProfileSelected() {
        navigationManager.navigateToProfile()
    }

    private fun handleHealthReselected() {
        val currentTag = navigationManager.getCurrentTag()

        if (currentTag != NavigationManager.TAG_PET_SELECTION &&
            (currentTag == NavigationManager.TAG_HEALTH_DASHBOARD ||
                    currentTag == NavigationManager.TAG_WEIGHT ||
                    currentTag == NavigationManager.TAG_VACCINATION)) {

            supportFragmentManager.popBackStack(
                NavigationManager.TAG_PET_SELECTION,
                0
            )
        }
    }

    override fun onBackPressed() {
        if (!navigationManager.goBack()) {
            showExitConfirmation()
        }
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("ออกจากแอป")
            .setMessage("คุณต้องการออกจากแอปหรือไม่?")
            .setPositiveButton("ใช่") { _, _ ->
                finish()
            }
            .setNegativeButton("ไม่", null)
            .show()
    }

    fun showMessage(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * โหลดข้อมูลผู้ใช้จาก Firestore
     */
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
                    // ดึงชื่อผู้ใช้
                    val userName = document.getString("username") ?: "Unknown User"
                    binding.tvUserName.text = userName

                    // ดึง URL รูปโปรไฟล์
                    val profileImageUrl = document.getString("profileImage")

                    // โหลดรูปด้วย Glide
                    loadProfileImage(profileImageUrl)

                    // เก็บข้อมูลอื่นๆ ใน ViewModel ถ้าต้องการ
                    //sharedViewModel.setCurrentUserInfo(document.data)
                } else {
                    showToast("ไม่พบข้อมูลโปรไฟล์")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user info", e)
                showToast("ไม่สามารถโหลดข้อมูลผู้ใช้ได้")
            }
    }

    /**
     * โหลดรูปโปรไฟล์ด้วย Glide
     */
    private fun loadProfileImage(imageUrl: String?) {
        val imageView = binding.ivUserProfile

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.user_placeholder) // รอโหลด
                        .error(R.drawable.user_placeholder) // โหลดไม่สำเร็จ
                        .circleCrop() // ทำให้เป็นวงกลม
                )
                .into(imageView)
        } else {
            // ถ้าไม่มีรูป ใช้รูปเริ่มต้น
            Glide.with(this)
                .load(R.drawable.user_placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)
        }
    }

    /**
     * รีเฟรชข้อมูลผู้ใช้ (เรียกเมื่อมีการแก้ไขโปรไฟล์)
     */
    fun refreshUserInfo() {
        loadUserInfo()
    }
}
