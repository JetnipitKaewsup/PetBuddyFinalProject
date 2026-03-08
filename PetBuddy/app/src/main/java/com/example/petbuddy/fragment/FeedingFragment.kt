package com.example.petbuddy.fragments.feeding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.databinding.FragmentFeedingBinding
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.SelectedPetAdapter
import com.example.petbuddy.fragment.PetSelectionFragment
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.util.Constants

class FeedingFragment : Fragment() {

    private var _binding: FragmentFeedingBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: SelectedPetAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        updateSelectedPetsDisplay()
    }

    override fun onResume() {
        super.onResume()
        // ทุกครั้งที่กลับมาที่ fragment ให้อัพเดทข้อมูล
        updateSelectedPetsDisplay()
    }

    private fun setupUI() {
        binding.btnSelectPets.setOnClickListener {
            // ไป PetSelectionFragment เพื่อเลือกสัตว์เลี้ยง (MULTIPLE MODE)
            val fragment = PetSelectionFragment()
            val data = Bundle().apply {
                putSerializable("mode", SelectionMode.MULTIPLE)
                putString("source_tag", Constants.TAG_FEEDING)
            }
            fragment.arguments = data

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("pet_selection")
                .commit()
        }

        binding.btnSave.setOnClickListener {
            saveFeedingSchedule()
        }
    }

    private fun setupRecyclerView() {
        adapter = SelectedPetAdapter { petId ->
            // ลบสัตว์เลี้ยงออกจากการเลือก
            baseActivity.removePetFromSelection(petId)
            updateSelectedPetsDisplay()
        }

        binding.rvSelectedPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedPets.adapter = adapter
    }

    private fun updateSelectedPetsDisplay() {
        val selectedPets = baseActivity.selectedPets

        adapter.submitList(selectedPets)

        if (selectedPets.isEmpty()) {
            binding.tvNoPets.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            binding.tvSelectedCount.text = "ยังไม่ได้เลือกสัตว์เลี้ยง"
        } else {
            binding.tvNoPets.visibility = View.GONE
            binding.btnSave.isEnabled = true
            binding.tvSelectedCount.text = "เลือก ${selectedPets.size} ตัว"
        }
    }

    private fun saveFeedingSchedule() {
        val selectedPets = baseActivity.selectedPets
        if (selectedPets.isEmpty()) {
            Toast.makeText(requireContext(), "กรุณาเลือกสัตว์เลี้ยง", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: บันทึกตารางการให้อาหาร
        Toast.makeText(requireContext(), "บันทึกตารางการให้อาหารสำหรับ ${selectedPets.size} ตัว", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}