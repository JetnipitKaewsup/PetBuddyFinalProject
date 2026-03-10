package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.RecentVaccineAdapter
import com.example.petbuddy.adapter.VaccinationRecordAdapter
import com.example.petbuddy.databinding.FragmentVaccinationBinding
import com.example.petbuddy.model.VaccinationRecord
import com.example.petbuddy.model.VaccineData
import com.example.petbuddy.navigation.MainNavigator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query

class VaccinationFragment : Fragment() {

    private var _binding: FragmentVaccinationBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator
    private lateinit var adapter: VaccinationRecordAdapter
    private lateinit var recentAdapter: RecentVaccineAdapter

    private var allRecords = mutableListOf<VaccinationRecord>()

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
        _binding = FragmentVaccinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerViews()
        loadVaccinationRecords()
    }

    override fun onResume() {
        super.onResume()
        loadVaccinationRecords()
    }

    private fun setupUI() {
        val currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            showMessage("กรุณาเลือกสัตว์เลี้ยงก่อน")
            parentFragmentManager.popBackStack()
            return
        }

        binding.tvPetName.text = currentPet.petName

        // ปุ่มเพิ่มวัคซีน
        binding.fabAddVaccine.setOnClickListener {
            navigateToAddVaccination()
        }

        // ปุ่มเปลี่ยนสัตว์เลี้ยง
        binding.btnChangePet.setOnClickListener {
            goToPetSelection()
        }

        // ปุ่มดูทั้งหมด
        binding.btnViewAll.setOnClickListener {
            toggleViewMode()
        }

        // ปุ่มกลับ
        /*binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }*/
    }

    private fun setupRecyclerViews() {
        // Recent vaccines adapter (แสดง 3 รายการล่าสุด)
        recentAdapter = RecentVaccineAdapter(emptyList())
        binding.rvRecentVaccines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentVaccines.adapter = recentAdapter

        // All records adapter
        adapter = VaccinationRecordAdapter(
            onItemClick = { record ->
                navigateToAddVaccination(record)
            },
            onItemLongClick = { record ->
                showDeleteConfirmation(record)
            }
        )

        binding.rvAllRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllRecords.adapter = adapter
    }

    private fun loadVaccinationRecords() {
        val currentPet = baseActivity.selectedPet ?: return
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        binding.progressBar.visibility = View.VISIBLE

        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .document(currentPet.petId)
            .collection("records")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                allRecords.clear()
                allRecords.addAll(snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(VaccinationRecord::class.java)?.copy(
                            id = document.id
                        )
                    } catch (e: Exception) {
                        null
                    }
                })

                updateUI()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                showMessage("โหลดข้อมูลไม่สำเร็จ: ${e.message}")
            }
    }

    private fun updateUI() {
        // อัพเดท Next Vaccine
        updateNextVaccine()

        // อัพเดท Latest Vaccine
        updateLatestVaccine()

        // อัพเดท Core Vaccines
        updateCoreVaccines()

        // อัพเดท Recent Vaccines (3 รายการล่าสุด)
        val recentVaccines = allRecords.take(3)
        recentAdapter = RecentVaccineAdapter(recentVaccines)
        binding.rvRecentVaccines.adapter = recentAdapter

        // อัพเดท All Records
        adapter.submitList(allRecords)

        // แสดง/ซ่อน views
        if (allRecords.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvAllRecords.visibility = View.GONE
        } else {
            binding.tvNoData.visibility = View.GONE
            binding.rvAllRecords.visibility = View.VISIBLE
        }
    }

    private fun updateNextVaccine() {
        val nextVaccines = allRecords.filter { it.nextDueDate != null }
            .sortedBy { it.nextDueDate }

        if (nextVaccines.isNotEmpty()) {
            val next = nextVaccines.first()
            binding.layoutNextVaccine.visibility = View.VISIBLE
            binding.tvNextVaccineName.text = next.nextVaccineName ?: next.vaccineName
            binding.tvNextDose.text = "เข็มที่ ${next.nextDose ?: next.dose + 1}"
            binding.tvNextDate.text = next.nextDueDateString ?: ""

            // แสดงจำนวนวันที่เหลือ
            next.daysUntilNext?.let { days ->
                when {
                    days < 0 -> {
                        binding.tvNextDaysLeft.text = "เลยกำหนดมา ${-days} วัน"
                        binding.tvNextDaysLeft.setTextColor(android.graphics.Color.parseColor("#F44336"))
                    }
                    days == 0 -> {
                        binding.tvNextDaysLeft.text = "วันนี้"
                        binding.tvNextDaysLeft.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                    }
                    days <= 7 -> {
                        binding.tvNextDaysLeft.text = "อีก $days วัน"
                        binding.tvNextDaysLeft.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                    }
                    else -> {
                        binding.tvNextDaysLeft.text = "อีก $days วัน"
                        binding.tvNextDaysLeft.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    }
                }
            }

            binding.btnEditNext.setOnClickListener {
                navigateToAddVaccination(next)
            }
        } else {
            binding.layoutNextVaccine.visibility = View.GONE
        }
    }

    private fun updateLatestVaccine() {
        if (allRecords.isNotEmpty()) {
            val latest = allRecords.first()
            binding.layoutLatestVaccine.visibility = View.VISIBLE
            binding.tvLatestVaccineName.text = latest.vaccineName
            binding.tvLatestDose.text = "เข็มที่ ${latest.dose}"
            binding.tvLatestDate.text = latest.dateString
            binding.tvLatestTime.text = latest.timeString
        } else {
            binding.layoutLatestVaccine.visibility = View.GONE
        }
    }

    private fun updateCoreVaccines() {
        val currentPet = baseActivity.selectedPet ?: return
        val coreVaccines = VaccineData.getCoreVaccinesByPetType(currentPet.petType ?: "")

        val vaccinatedCore = allRecords.map { it.vaccineName }.toSet()

        val coreVaccineText = coreVaccines.joinToString("\n") { vaccine ->
            val status = if (vaccinatedCore.contains(vaccine.name)) "✓" else "○"
            "$status ${vaccine.name}"
        }

        binding.tvCoreVaccines.text = coreVaccineText
    }

    private fun toggleViewMode() {
        if (binding.rvRecentVaccines.visibility == View.VISIBLE) {
            // เปลี่ยนเป็นแสดงทั้งหมด
            binding.rvRecentVaccines.visibility = View.GONE
            binding.rvAllRecords.visibility = View.VISIBLE
            binding.btnViewAll.text = "แสดงล่าสุด"
        } else {
            // เปลี่ยนเป็นแสดงล่าสุด
            binding.rvRecentVaccines.visibility = View.VISIBLE
            binding.rvAllRecords.visibility = View.GONE
            binding.btnViewAll.text = "ดูทั้งหมด"
        }
    }

    private fun navigateToAddVaccination(record: VaccinationRecord? = null) {
        navigator.navigateToAddVaccination(record)
//        val fragment = AddVaccinationFragment.newInstance(record)
//
//        parentFragmentManager.beginTransaction()
//            .setCustomAnimations(
//                R.anim.slide_in_right,
//                R.anim.slide_out_left,
//                R.anim.slide_in_left,
//                R.anim.slide_out_right
//            )
//            .replace(R.id.fragment_container, fragment)
//            .addToBackStack("add_vaccination")
//            .commit()
    }

    private fun goToPetSelection() {
        val fragment = PetSelectionFragment()
        val data = Bundle().apply {
            putSerializable("mode", com.example.petbuddy.model.SelectionMode.SINGLE)
            putString("source_tag", com.example.petbuddy.util.Constants.TAG_VACCINATION)
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

    private fun showDeleteConfirmation(record: VaccinationRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("ลบประวัติวัคซีน")
            .setMessage("คุณต้องการลบประวัติวัคซีน ${record.vaccineName} เข็มที่ ${record.dose} วันที่ ${record.dateString} ใช่หรือไม่?")
            .setPositiveButton("ลบ") { _, _ ->
                deleteVaccinationRecord(record)
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun deleteVaccinationRecord(record: VaccinationRecord) {
        val currentPet = baseActivity.selectedPet ?: return
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .document(currentPet.petId)
            .collection("records")
            .document(record.id)
            .delete()
            .addOnSuccessListener {
                showMessage("ลบข้อมูลสำเร็จ")
                loadVaccinationRecords()
            }
            .addOnFailureListener { e ->
                showMessage("ลบไม่สำเร็จ: ${e.message}")
            }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}