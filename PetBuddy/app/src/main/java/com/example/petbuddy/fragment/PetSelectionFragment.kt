package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.PetAdapter
import com.example.petbuddy.databinding.FragmentPetSelectionBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.util.Constants

class PetSelectionFragment : Fragment() {

    private var _binding: FragmentPetSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var mode: SelectionMode
    private lateinit var adapter: PetAdapter

    private var sourceTag: String? = null

    // ข้อมูลตัวอย่าง (จริงๆ ต้องโหลดจาก Firebase)
    private val mockPets = listOf(
        Pet("1", "ทองต้วน", "dog", "โกลเด้น", "dog", null),
        Pet("2", "สีดา", "cat", "วิเชียรมาศ", "cat", null),
        Pet("3", "ดำ", "dog", "ชิวาวา", "dog", null)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getSerializable("mode", SelectionMode::class.java) ?: SelectionMode.SINGLE
        sourceTag = arguments?.getString("source_tag")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // เก็บ reference ของ BaseActivity
        baseActivity = context as BaseActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        loadPets()
    }

    private fun setupUI() {
        binding.tvSubtitle.text = when (mode) {
            SelectionMode.SINGLE -> "เลือกสัตว์เลี้ยง 1 ตัว"
            SelectionMode.MULTIPLE -> "เลือกสัตว์เลี้ยง 1 ตัวขึ้นไป"
        }

        if (mode == SelectionMode.MULTIPLE) {
            binding.btnConfirm.visibility = View.VISIBLE
            binding.btnConfirm.text = "ยืนยัน (0)"
            binding.btnConfirm.isEnabled = false

            binding.btnConfirm.setOnClickListener {
                onConfirmClick()
            }
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
                    binding.btnConfirm.text = "ยืนยัน ($selectedCount)"
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
                // ใช้ BaseActivity
                baseActivity.selectPet(pet)

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
                // Adapter จะจัดการ selection เอง รอกดปุ่มยืนยัน
                // ไม่ต้องทำอะไรเพิ่ม
            }
        }
    }

    private fun onConfirmClick() {
        val selectedPets = adapter.getSelectedPets()
        if (selectedPets.isNotEmpty()) {
            // ใช้ BaseActivity
            baseActivity.selectPets(selectedPets)

            // ตรวจสอบ sourceTag เพื่อกลับไปหน้าถูกต้อง
            when (sourceTag) {
                Constants.TAG_FEEDING -> {
                    // กลับไป FeedingFragment
//                    parentFragmentManager.popBackStack(Constants.TAG_FEEDING, 0)
//                    val fragment = Feeding
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
                    parentFragmentManager.popBackStack()
                }
            }

        } else {
            Toast.makeText(requireContext(), "กรุณาเลือกสัตว์เลี้ยง", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPets() {
        // โหลดจาก Firebase ผ่าน BaseActivity
        baseActivity.loadAllPets { pets ->
            if (pets.isNotEmpty()) {
                adapter.submitList(pets)
            } else {
                // ถ้าไม่มีข้อมูล ให้ใช้ mock data
                adapter.submitList(mockPets)
                Toast.makeText(requireContext(), "ใช้ข้อมูลตัวอย่าง", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
