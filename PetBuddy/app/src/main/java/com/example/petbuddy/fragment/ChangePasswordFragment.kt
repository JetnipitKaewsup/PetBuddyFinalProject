package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentChangePasswordBinding
import com.example.petbuddy.navigation.MainNavigator
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator
    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validate inputs
        if (currentPassword.isEmpty()) {
            binding.etCurrentPassword.error = "Current password required"
            return
        }

        if (newPassword.isEmpty()) {
            binding.etNewPassword.error = "New password required"
            return
        }

        if (newPassword.length < 6) {
            binding.etNewPassword.error = "Password must be at least 6 characters"
            return
        }

        if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        // Disable button while processing
        binding.btnChangePassword.isEnabled = false
        binding.btnChangePassword.text = "Updating..."

        val user = auth.currentUser
        if (user != null && user.email != null) {
            reauthenticateAndChangePassword(user, currentPassword, newPassword)
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            binding.btnChangePassword.isEnabled = true
            binding.btnChangePassword.text = "Change Password"
        }
    }

    private fun reauthenticateAndChangePassword(
        user: FirebaseUser,
        currentPassword: String,
        newPassword: String
    ) {
        // Create credential with email and current password
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        // Re-authenticate user
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Re-authentication successful, now change password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Password updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                parentFragmentManager.popBackStack()
                            } else {
                                handleError(updateTask.exception, "Failed to update password")
                            }
                            binding.btnChangePassword.isEnabled = true
                            binding.btnChangePassword.text = "Change Password"
                        }
                } else {
                    handleError(reauthTask.exception, "Current password is incorrect")
                    binding.btnChangePassword.isEnabled = true
                    binding.btnChangePassword.text = "Change Password"
                }
            }
    }

    private fun handleError(exception: Exception?, defaultMessage: String) {
        val errorMessage = when (exception?.message) {
            "The password is invalid or the user does not have a password." -> "Current password is incorrect"
            "We have blocked all requests from this device due to unusual activity. Try again later." -> "Too many attempts. Please try again later"
            else -> exception?.message ?: defaultMessage
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}