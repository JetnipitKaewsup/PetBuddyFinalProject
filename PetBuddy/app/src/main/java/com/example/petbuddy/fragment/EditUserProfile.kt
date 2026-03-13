package com.example.petbuddy.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentEditUserProfileBinding
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.supabase.SupabaseClient
import com.google.firebase.firestore.FieldValue
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class EditUserProfileFragment : Fragment() {

    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator

    private var originalUsername: String = ""
    private var originalProfileImage: String? = null
    private var selectedImageUri: Uri? = null
    private var isImageChanged = false
    private var isUsernameChanged = false

    // Contract for picking image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            isImageChanged = true
            loadImage(it.toString())
            checkIfChangesMade()
        }
    }

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
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        loadUserProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // ถ้ามีการเปลี่ยนแปลง ให้ถามก่อนกลับ
            if (isImageChanged || isUsernameChanged) {
                showDiscardChangesDialog()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupListeners() {
        // คลิกที่รูปเพื่อเปลี่ยน
        binding.imageProfile.setOnClickListener {
            openGallery()
        }

        // คลิกที่ข้อความ "Edit profile picture"
        binding.txtEdtProfile.setOnClickListener {
            openGallery()
        }

        // ใช้ TextWatcher แทน OnFocusChangeListener
        binding.edtUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // ไม่ต้องทำอะไร
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // ไม่ต้องทำอะไร
            }

            override fun afterTextChanged(s: Editable?) {
                // ตรวจสอบการเปลี่ยนแปลงทุกครั้งที่พิมพ์
                val currentUsername = s?.toString() ?: ""
                isUsernameChanged = currentUsername != originalUsername
                checkIfChangesMade()
            }
        })

        // ปุ่มบันทึก
        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun loadUserProfile() {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        // แสดง loading
        binding.btnSaveChanges.isEnabled = false

        baseActivity.db.collection("users")
            .document(userId)
            .collection("userInfo")
            .document("profile")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    originalUsername = document.getString("username") ?: ""
                    originalProfileImage = document.getString("profileImage")

                    // แสดงข้อมูล
                    binding.edtUsername.setText(originalUsername)
                    loadImage(originalProfileImage)
                }
                binding.btnSaveChanges.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSaveChanges.isEnabled = true
            }
    }

    private fun loadImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .circleCrop()
                )
                .into(binding.imageProfile)
        } else {
            binding.imageProfile.setImageResource(R.drawable.user_placeholder)
        }
    }

    private fun checkIfChangesMade() {
        binding.btnSaveChanges.isEnabled = isImageChanged || isUsernameChanged
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard changes")
            .setMessage("You have unsaved changes. Are you sure you want to leave?")
            .setPositiveButton("Leave") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun saveChanges() {
        val newUsername = binding.edtUsername.text.toString()

        // ตรวจสอบ username
        if (newUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // ตรวจสอบการเปลี่ยนแปลงอีกครั้ง (กันไว้)
        isUsernameChanged = newUsername != originalUsername

        // ถ้าไม่มีการเปลี่ยนแปลงอะไรเลย
        if (!isUsernameChanged && !isImageChanged) {
            Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        // disable ปุ่มระหว่างบันทึก
        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving..."

        if (isImageChanged && selectedImageUri != null) {
            // มีทั้งเปลี่ยนรูปและ username (หรือเปลี่ยนรูปอย่างเดียว)
            uploadImageAndUpdateProfile(newUsername)
        } else {
            // เปลี่ยนเฉพาะ username
            updateProfileInFirestore(newUsername, originalProfileImage)
        }
    }

    private fun uploadImageAndUpdateProfile(newUsername: String) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        lifecycleScope.launch {
            try {
                // อ่านรูปเป็น bytes
                val bytes = requireContext().contentResolver.openInputStream(selectedImageUri!!)?.use {
                    it.readBytes()
                } ?: throw Exception("Failed to read image")

                // อัปโหลดไปยัง Supabase
                val bucket = SupabaseClient.client.storage.from("picture")
                val fileName = "profile/$userId.jpg"

                bucket.upload(
                    path = fileName,
                    data = bytes,
                    upsert = true  // แทนที่ไฟล์เดิม
                )

                // ได้ public URL
                val imageUrl = bucket.publicUrl(fileName)

                // อัปเดท Firestore
                updateProfileInFirestore(newUsername, imageUrl)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save changes"
            }
        }
    }

    private fun updateProfileInFirestore(newUsername: String, newImageUrl: String?) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        // เตรียมข้อมูลที่จะอัปเดท
        val updates = hashMapOf<String, Any>()

        if (isUsernameChanged) {
            updates["username"] = newUsername
        }

        if (isImageChanged) {
            newImageUrl?.let {
                updates["profileImage"] = it
            }
        }

        // เพิ่ม updatedAt field (optional)
        updates["updatedAt"] = FieldValue.serverTimestamp()

        baseActivity.db.collection("users")
            .document(userId)
            .collection("userInfo")
            .document("profile")
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // อัปเดท header ใน MainActivity
                (requireActivity() as? MainActivity)?.refreshUserInfo()

                // กลับไปหน้าก่อน
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save changes"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}