package com.example.petbuddy.fragments.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.MainActivity
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.activity.LoginActivity
import com.example.petbuddy.adapter.MyPetsAdapter
import com.example.petbuddy.databinding.FragmentProfileBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.navigation.NavigationManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator
    private lateinit var adapter: MyPetsAdapter
    private var allPets: List<Pet> = emptyList()
    private var isNotificationEnabled = true
    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListener()
        loadUserPets()
        loadNotificationSetting()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar(){
        binding.toolbar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = MyPetsAdapter { pet ->
            navigator.navigateToEditPetProfile(pet)
            Toast.makeText(requireContext(), "${pet.petName} has click", Toast.LENGTH_SHORT).show()


        }
        binding.rvMyPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyPets.adapter = adapter
        binding.rvMyPets.isNestedScrollingEnabled = false
    }

    private fun loadNotificationSetting() {
        // TODO("Not yet implemented")
        //Toast.makeText(requireContext(), "Notification Setting", Toast.LENGTH_SHORT).show()
    }


    private fun setupClickListener() {
        binding.btnCreatePet.setOnClickListener {
            navigator.navigateToCreateNewPet()
        }
        binding.btntMyProfile.setOnClickListener {
            // navigate to profile edit
            navigator.navigateToEditUserProfile()
            //Toast.makeText(requireContext(), "My profile clicked", Toast.LENGTH_SHORT).show()

        }
        binding.layoutPassword.setOnClickListener {
            //Toast.makeText(requireContext(), "Password clicked", Toast.LENGTH_SHORT).show()
            navigator.navigateToChangePassword()
        }
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            saveNotificationSetting(isChecked)
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun saveNotificationSetting(enable : Boolean){

    }

    private fun loadUserPets() {
        baseActivity.loadAllPets { petList ->
            allPets = petList

            // แสดงเฉพาะ 3 ตัวแรกใน RecyclerView
            val displayPets = if (petList.size > 3) petList.subList(0, 3) else petList
            adapter.submitList(displayPets)

            // ถ้ามีสัตว์เลี้ยงมากกว่า 3 ตัว แสดงปุ่ม "ดูเพิ่มเติม"
            if (petList.size > 3) {
                showViewAllButton()
            }
        }
    }

    private fun showViewAllButton(){
        binding.btnViewAllMyPets.visibility = View.VISIBLE
        binding.btnViewAllMyPets.setOnClickListener { navigator.navigateToAllMyPets() }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        // Clear selected data
        baseActivity.clearSelection()

        // Sign out from Firebase
        baseActivity.mAuth.signOut()

        // Navigate to Login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        requireActivity().finish()
    }
}



