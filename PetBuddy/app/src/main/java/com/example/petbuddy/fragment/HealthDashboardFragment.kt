package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentHealthDashboardBinding
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.util.Constants

class HealthDashboardFragment : Fragment() {

    private var _binding: FragmentHealthDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        checkSelectedPet()
    }

    override fun onResume() {
        super.onResume()
        // ทุกครั้งที่กลับมาที่ fragment ให้ตรวจสอบข้อมูลอีกครั้ง
        checkSelectedPet()
    }

    private fun checkSelectedPet() {
        val currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            // ยังไม่มีสัตว์เลี้ยงที่เลือก ให้ไปเลือกก่อน
            goToPetSelection()
        } else {
            // มีสัตว์เลี้ยงที่เลือกแล้ว อัพเดท UI
            updatePetInfo(currentPet)
            loadHealthData(currentPet.petId)
        }
    }

    private fun setupUI() {
        // แสดงชื่อสัตว์เลี้ยงจาก BaseActivity
        val currentPet = baseActivity.selectedPet
        if (currentPet != null) {
            binding.tvPetName.text = currentPet.petName
        }
    }

    private fun updatePetInfo(pet: com.example.petbuddy.model.Pet) {
        binding.tvPetName.text = pet.petName
        // สามารถอัพเดทข้อมูลอื่นๆ เช่น รูปสัตว์เลี้ยงได้ที่นี่
    }

    private fun loadHealthData(petId: String) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        // โหลดน้ำหนักล่าสุด
        baseActivity.db.collection("users")
            .document(userId)
            .collection("weights")
            .document(petId)
            .collection("records")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val weight = documents.documents[0].getDouble("weight") ?: 0.0
                    binding.tvLatestWeight.text = String.format("%.1f kg", weight)
                    binding.tvWeightStatus.text = "น้ำหนักปกติ"
                    binding.tvWeightStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    binding.tvLatestWeight.text = "- kg"
                    binding.tvWeightStatus.text = "ยังไม่มีข้อมูลน้ำหนัก"
                }
            }
            .addOnFailureListener {
                binding.tvLatestWeight.text = "- kg"
                binding.tvWeightStatus.text = "โหลดไม่สำเร็จ"
            }

        // โหลดวัคซีนที่กำลังจะถึง
        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .document(petId)
            .collection("records")
            .whereGreaterThan("nextDueDate", java.util.Date())
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
            .addOnFailureListener {
                binding.tvNextVaccine.text = "โหลดไม่สำเร็จ"
            }
    }

    private fun setupClickListeners() {
        binding.btnChangePet.setOnClickListener {
            goToPetSelection()
        }

        binding.cardWeight.setOnClickListener {
            // ไปหน้า Weight
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}