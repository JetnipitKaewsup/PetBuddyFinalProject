package com.example.petbuddy.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.petbuddy.R
import com.example.petbuddy.databinding.FragmentHealthDashboardBinding
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.util.Constants
import com.example.petbuddy.utils.PetHeaderHelper
import com.example.petbuddy.viewmodel.SharedPetViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HealthDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HealthDashboardFragment : Fragment() {
    private var _binding: FragmentHealthDashboardBinding? = null
    private val binding get() = _binding!!

    // ใช้ activityViewModels() เพื่อแชร์ ViewModel กับ Activity
    private val sharedViewModel: SharedPetViewModel by activityViewModels()
    private var db = FirebaseFirestore.getInstance()



    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDashboardBinding.inflate(inflater, container, false)
        return binding.root}
/*
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HealthDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HealthDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ตรวจสอบว่ามีสัตว์เลี้ยงที่เลือกหรือไม่
        if (!sharedViewModel.hasSelectedPet()) {
            // ยังไม่มี ให้ไปเลือกสัตว์เลี้ยงก่อน
            goToPetSelection()
            return
        }
        setupUI()
        observeViewModel()
        loadHealthData()
        setupClickListeners()
    }

    private fun observeViewModel() {
        // สังเกตการเปลี่ยนแปลงของสัตว์เลี้ยงที่เลือก
        sharedViewModel.selectedPet.observe(viewLifecycleOwner) { pet ->
            pet?.let {
                binding.tvPetName.text = it.name
                loadHealthData() // โหลดข้อมูลใหม่เมื่อเปลี่ยนสัตว์เลี้ยง
            }
        }
    }
    private fun setupUI() {
        // แสดงชื่อสัตว์เลี้ยงจาก SharedPreferences
        val petName = sharedViewModel.getSelectedPetName()
        if (!petName.isNullOrEmpty()) {
            binding.tvPetName.text = petName
        }
    }
    private fun loadHealthData() {
        val petId = sharedViewModel.getSelectedPetId() ?: return
        loadLatestWeight(petId)
        loadVaccinationData(petId)
    }

    private fun loadLatestWeight(petId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petId)
            .collection("weights")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val weight = documents.documents[0].getDouble("weight") ?: 0.0
                    val date = documents.documents[0].getDate("date")

                    binding.tvLatestWeight.text = String.format("%.1f", weight)

                    // คำนวณสถานะ (ตัวอย่าง)
                    binding.tvWeightStatus.text = "น้ำหนักปกติ"
                    binding.tvWeightStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    binding.tvLatestWeight.text = "0.0"
                    binding.tvWeightStatus.text = "ยังไม่มีข้อมูลน้ำหนัก"
                }
            }
    }

    private fun loadVaccinationData(petId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petId)
            .collection("vaccinations")
            .whereGreaterThan("nextDueDate", Date())
            .orderBy("nextDueDate", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val vaccineName = documents.documents[0].getString("name") ?: "วัคซีน"
                    binding.tvNextVaccine.text = "$vaccineName (เร็วๆนี้)"
                } else {
                    binding.tvNextVaccine.text = "ไม่มีวัคซีนที่กำลังจะถึง"
                }
            }
    }




    private fun setupClickListeners() {
        binding.btnChangePet.setOnClickListener {
            goToPetSelection()
        }

        binding.cardWeight.setOnClickListener {
            // ไปหน้า Weight (ส่ง petId ผ่าน ViewModel)
            val fragment = WeightFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("weight")
                .commit()
        }

        binding.cardVaccination.setOnClickListener {
            // ไปหน้า Vaccination
            val fragment = VaccinationFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("vaccination")
                .commit()
        }


    }
    private fun goToPetSelection() {
        val fragment = PetSelectionFragment()
        val data = Bundle().apply {
            putSerializable("mode", SelectionMode.SINGLE)
            putString("source_tag", Constants.TAG_HEALTH_DASHBOARD)
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

    override fun onResume() {
        super.onResume()
        // เมื่อกลับมาจาก PetSelectionFragment ให้โหลดข้อมูลใหม่
        if (sharedViewModel.hasSelectedPet()) {
            loadHealthData()
            setupUI()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}