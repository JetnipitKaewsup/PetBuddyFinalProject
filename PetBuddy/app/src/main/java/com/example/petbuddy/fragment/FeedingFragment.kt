package com.example.petbuddy.fragments.feeding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.databinding.FragmentFeedingBinding
import com.example.petbuddy.R
import com.example.petbuddy.adapter.SelectedPetAdapter
import com.example.petbuddy.fragment.PetSelectionFragment
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.util.Constants
import com.example.petbuddy.viewmodel.SharedPetViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedingFragment : Fragment() {
    private var _binding: FragmentFeedingBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedPetViewModel by activityViewModels()
    private lateinit var adapter: SelectedPetAdapter
    private var db =  FirebaseFirestore.getInstance()
    private  var auth = FirebaseAuth.getInstance()

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
        observeViewModel()
    }

    private fun setupUI() {
        // ปุ่มเลือกสัตว์เลี้ยง
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

        // ปุ่มบันทึก
        binding.btnSave.setOnClickListener {
            saveFeedingSchedule()
        }
    }

    private fun setupRecyclerView() {
        adapter = SelectedPetAdapter { petId ->
            sharedViewModel.removePetFromSelection(petId)
        }

        binding.rvSelectedPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedPets.adapter = adapter
    }

    private fun observeViewModel() {
        sharedViewModel.selectedPets.observe(viewLifecycleOwner) { pets ->
            adapter.submitList(pets)

            if (pets.isEmpty()) {
                binding.tvNoPets.visibility = View.VISIBLE
                binding.btnSave.isEnabled = false
            } else {
                binding.tvNoPets.visibility = View.GONE
                binding.btnSave.isEnabled = true
                binding.tvSelectedCount.text = "เลือก ${pets.size} ตัว"
            }
        }
    }

    private fun saveFeedingSchedule() {
        val selectedPets = sharedViewModel.selectedPets.value
        if (selectedPets.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "กรุณาเลือกสัตว์เลี้ยง", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: บันทึกตารางการให้อาหารลง Firebase
        Toast.makeText(requireContext(), "บันทึกตารางการให้อาหารสำหรับ ${selectedPets.size} ตัว", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}