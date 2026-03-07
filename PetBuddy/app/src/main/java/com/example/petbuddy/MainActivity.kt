package com.example.petbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.petbuddy.databinding.ActivityMainBinding
import com.example.petbuddy.navigation.NavigationManager
import com.example.petbuddy.viewmodel.SharedPetViewModel
import com.example.petbuddy.model.SelectionMode
class MainActivity : AppCompatActivity() {
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

        setupBottomNavigation()
        //observeViewModel()

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
}
