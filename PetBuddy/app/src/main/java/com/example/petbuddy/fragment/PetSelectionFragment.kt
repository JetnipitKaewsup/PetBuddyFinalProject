package com.example.petbuddy.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.adapter.PetAdapter
import com.example.petbuddy.databinding.FragmentPetSelectionBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.util.Constants
import com.example.petbuddy.viewmodel.SharedPetViewModel


class PetSelectionFragment : Fragment() {
    private var _binding: FragmentPetSelectionBinding? = null
    private val binding get() = _binding!!

    // ใช้ activityViewModels() เพื่อแชร์ ViewModel กับ Activity
    private val sharedViewModel: SharedPetViewModel by activityViewModels()

    private lateinit var mode: SelectionMode //เก็บโหมดการเลือก ซึ่งถูกส่งมาจาก Acitivity
    private val selectedPets = mutableListOf<Pet>()
    private lateinit var adapter: PetAdapter // จัดการส่วนแสดงสัตว์เลี้ยงใน recyclerView

    private var sourceTag: String? = null


    // ข้อมูลตัวอย่าง (จริงๆ ต้องโหลดจาก Firebase)
    private val mockPets = listOf(
        Pet("1", "ทองต้วน", "dog", "โกลเด้น", null, ""),
        Pet("2", "สีดา", "cat", "วิเชียรมาศ", null, ""),
        Pet("3", "ดำ", "dog", "ชิวาวา", null, "")
    )




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getSerializable("mode", SelectionMode::class.java) ?: SelectionMode.SINGLE
        sourceTag = arguments?.getString("source_tag")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetSelectionBinding.inflate(inflater, container, false)
        return binding.root}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        loadPets()
    }

    private fun setupUI() {
        binding.tvSubtitle.text = when (mode) {
            SelectionMode.SINGLE -> "Choose a pet"
            SelectionMode.MULTIPLE -> "Select one or more pets (at least 1)"
        }

        if (mode == SelectionMode.MULTIPLE) {
            binding.btnConfirm.visibility = View.VISIBLE
            binding.btnConfirm.text = "confirm (0)"
            binding.btnConfirm.isEnabled = false

            binding.btnConfirm.setOnClickListener {
                onConfirmClick()
            }
        }else if (mode == SelectionMode.SINGLE) {

        }
    }

    private fun setupRecyclerView() {
        adapter = PetAdapter(
            mode = mode,
            onItemClick = { pet ->
                handlePetClick(pet)
            },
            onSelectionChanged = { selectedCount ->
                if (mode == SelectionMode.MULTIPLE) {
                    binding.btnConfirm.text = "confirm ($selectedCount)"
                    binding.btnConfirm.isEnabled = selectedCount > 0
                }
            }
        )

        binding.rvPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPets.adapter = adapter
    }

    private fun handlePetClick(pet: Pet) {
        when (mode) {
            SelectionMode.SINGLE -> {
                // เลือกตัวเดียว → ส่งข้อมูลไป ViewModel ทันที
                sharedViewModel.selectPet(pet)
                // กลับไปหน้าก่อนตาม sourceTag
                when (sourceTag) {
                    Constants.TAG_HEALTH_DASHBOARD -> {
                        // กลับไป HealthDashboard โดยตรง
                        parentFragmentManager.popBackStack(Constants.TAG_HEALTH_DASHBOARD, 0)
                    }
                    else -> {
                        parentFragmentManager.popBackStack()
                    }
                }
            }
            SelectionMode.MULTIPLE -> {
                // เลือกหลายตัว → เก็บไว้ก่อน รอกดปุ่มยืนยัน
                // Adapter จะจัดการ selection เอง
            }
        }
    }

    private fun onConfirmClick() {
        val selectedPets = adapter.getSelectedPets()
        if (selectedPets.isNotEmpty()) {
            sharedViewModel.selectPets(selectedPets)
            // ตรวจสอบ sourceTag เพื่อกลับไปหน้าถูกต้อง
            when (sourceTag) {
                Constants.TAG_FEEDING -> {
                    // กลับไป FeedingFragment
                    parentFragmentManager.popBackStack(Constants.TAG_FEEDING, 0)
                }
                Constants.TAG_SCHEDULE -> {
                    // กลับไป ScheduleFragment
                    parentFragmentManager.popBackStack(Constants.TAG_SCHEDULE, 0)
                }
                Constants.TAG_HEALTH_DASHBOARD -> {
                    // กลับไป HealthDashboard
                    parentFragmentManager.popBackStack(Constants.TAG_HEALTH_DASHBOARD, 0)
                }
                else -> {
                    // ถ้าไม่รู้จัก ก็กลับไปหน้าก่อน
                    parentFragmentManager.popBackStack()
                }
            }

        } else {
            Toast.makeText(requireContext(), "Please select a pet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPets() {
        // TODO: โหลดจาก Firebase
        adapter.submitList(mockPets)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}