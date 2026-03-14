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
    private var requestKey: String? = null
    private var initialSelectedIds: List<String> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("mode", SelectionMode::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("mode") as? SelectionMode
        } ?: SelectionMode.SINGLE

        sourceTag = arguments?.getString("source_tag")
        requestKey = arguments?.getString("request_key")

        // รับรายการสัตว์ที่เลือกไว้แล้ว (ถ้ามี)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            initialSelectedIds = arguments?.getStringArrayList("selected_pet_ids") ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            initialSelectedIds = arguments?.getStringArrayList("selected_pet_ids") ?: emptyList()
        }
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
            SelectionMode.SINGLE -> "Select 1 pet"
            SelectionMode.MULTIPLE -> "Select one or more pets"
        }

        if (mode == SelectionMode.MULTIPLE) {
            binding.btnConfirm.visibility = View.VISIBLE
            binding.btnConfirm.text = "Confirm (0)"
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
                    binding.btnConfirm.text = "Confirm ($selectedCount)"
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
                Constants.TAG_FEEDING_ALARM -> {
                    parentFragmentManager.popBackStack()
                }
                Constants.TAG_SCHEDULE -> {
                    // กลับไป ScheduleFragment
                    //parentFragmentManager.popBackStack(Constants.TAG_SCHEDULE, 0)
                    val selectedPets = adapter.getSelectedPets()
                    if (selectedPets.isNotEmpty()) {
                        // ตรวจสอบ sourceTag
                        when (sourceTag) {
                            Constants.TAG_SCHEDULE -> {
                                // ส่งข้อมูลกลับ via FragmentResult
                                val result = Bundle().apply {
                                    putStringArrayList("selected_pet_ids", ArrayList(selectedPets.map { it.petId }))
                                }
                                parentFragmentManager.setFragmentResult(requestKey ?: "pets_selected", result)
                                parentFragmentManager.popBackStack()
                            }
                            Constants.TAG_FEEDING_ALARM -> {
                                baseActivity.selectPets(selectedPets)
                                parentFragmentManager.popBackStack()
                            }
                            else -> {
                                baseActivity.selectPets(selectedPets)
                                parentFragmentManager.popBackStack()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Please select a pet", Toast.LENGTH_SHORT).show()
                    }

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
            Toast.makeText(requireContext(), "Please select a pet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPets() {
        // โหลดจาก Firebase ผ่าน BaseActivity
        baseActivity.loadAllPets { pets ->
            if (pets.isNotEmpty()) {
                adapter.submitList(pets)
                if (initialSelectedIds.isNotEmpty()) {
                    adapter.setInitialSelection(initialSelectedIds)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


