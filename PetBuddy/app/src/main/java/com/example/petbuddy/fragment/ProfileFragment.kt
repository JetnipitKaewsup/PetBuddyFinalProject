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
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.activity.LoginActivity
import com.example.petbuddy.databinding.FragmentProfileBinding
import com.example.petbuddy.model.Pet

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private var pets: List<Pet> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
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
        setUpClickListener()
        loadUserPets()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setUpClickListener(){
        binding.btntMyProfile.setOnClickListener {
            // navigate to profile edit
            Toast.makeText(requireContext(), "My profile clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserPets() {
        //
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


