package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.text.SimpleDateFormat
import java.util.*

class VaccinationFragment : Fragment() {

    private var _binding: FragmentVaccinationBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator
    private lateinit var adapter: VaccinationRecordAdapter
    private lateinit var recentAdapter: RecentVaccineAdapter
    private val datePattern = "dd/MM/yyyy"
    private val timePattern = "H:mm"
    private var allRecords = mutableListOf<VaccinationRecord>()
    private var currentPetId: String = ""

    companion object {
        fun newInstance(): VaccinationFragment {
            return VaccinationFragment()
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
        _binding = FragmentVaccinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUI()
        setupRecyclerViews()
        loadVaccinationRecords()
    }

    override fun onResume() {
        super.onResume()
        loadVaccinationRecords()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        val currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            showMessage("Please select a pet first")
            parentFragmentManager.popBackStack()
            return
        }

        currentPetId = currentPet.petId
        binding.tvPetName.text = currentPet.petName

        binding.fabAddVaccine.setOnClickListener {
            navigator.navigateToAddVaccination()
        }

        binding.btnChangePet.setOnClickListener {
            goToPetSelection()
        }

        binding.btnViewAll.setOnClickListener {
            toggleViewMode()
        }

//        binding.btnAddNext.setOnClickListener {
//            if (allRecords.isNotEmpty()) {
//                val next = allRecords.firstOrNull { it.nextDueDate != null }
//                next?.let {
//                    navigator.navigateToAddVaccination(it)
//                } ?: run {
//                    Toast.makeText(requireContext(), "No upcoming vaccine found", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    private fun setupRecyclerViews() {
        recentAdapter = RecentVaccineAdapter(emptyList())
        binding.rvRecentVaccines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentVaccines.adapter = recentAdapter

        adapter = VaccinationRecordAdapter(
            onItemClick = { record ->
                navigator.navigateToAddVaccination(record)
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
                showMessage("Error loading data: ${e.message}")
            }
    }

    private fun updateUI() {
        updateNextVaccine()
        updateLatestVaccine()
        updateVaccineStatus()
        updateRecentVaccines()
        updateAllRecords()
    }

    private fun updateNextVaccine() {
        val nextVaccines = allRecords.filter { it.nextDueDate != null }
            .sortedBy { it.nextDueDate }

        if (nextVaccines.isNotEmpty()) {
            val next = nextVaccines.first()
            binding.layoutNextVaccine.visibility = View.VISIBLE

            // Calculate months and days
            next.daysUntilNext?.let { days ->
                val months = days / 30
                val remainingDays = days % 30
                binding.tvMonths.text = months.toString()
                binding.tvDays.text = remainingDays.toString()
            }

            binding.tvNextVaccineName.text = "Name ${next.nextVaccineName ?: next.vaccineName}"
            binding.tvNextDose.text = "Dose ${next.nextDose ?: next.dose + 1}"

            if (!next.nextPlace.isNullOrEmpty()) {
                binding.tvNextPlace.visibility = View.VISIBLE
                binding.tvNextPlace.text = "Place ${next.nextPlace}"
            } else {
                binding.tvNextPlace.visibility = View.GONE
            }

            val dateFormat = SimpleDateFormat(datePattern, Locale.US)
            val timeFormat = SimpleDateFormat(timePattern, Locale.US)
            val nextDate = Date(next.nextDueDate!!)
            binding.tvNextDateTime.text = "Date ${dateFormat.format(nextDate)} Time ${timeFormat.format(nextDate)}"

            binding.tvEdit.setOnClickListener {
                navigator.navigateToAddVaccination(next)
            }

            binding.btnDone.setOnClickListener {
                markVaccineAsDone(next)
            }
        } else {
            binding.layoutNextVaccine.visibility = View.GONE
        }
    }

    private fun updateLatestVaccine() {
        if (allRecords.isNotEmpty()) {
            val latest = allRecords.first()
            binding.layoutLatestVaccine.visibility = View.VISIBLE

            binding.tvLatestVaccineName.text = "Name ${latest.vaccineName}"
            binding.tvLatestDose.text = "Dose ${latest.dose}"

            val dateFormat = SimpleDateFormat(datePattern, Locale.US)
            val timeFormat = SimpleDateFormat(timePattern, Locale.US)
            binding.tvLatestDateTime.text = "Date ${dateFormat.format(Date(latest.timestamp))} Time ${timeFormat.format(Date(latest.timestamp))}"
        } else {
            binding.layoutLatestVaccine.visibility = View.GONE
        }
    }

    private fun updateVaccineStatus() {
        val currentPet = baseActivity.selectedPet ?: return

        val coreVaccines = VaccineData.getCoreVaccinesByPetType(currentPet.petType ?: "")
        val boosterVaccines = VaccineData.getBoosterVaccinesByPetType(currentPet.petType ?: "")

        val vaccinatedVaccines = allRecords.map { it.vaccineName }.toSet()

        // Core Vaccines
        binding.layoutCoreVaccines.removeAllViews()
        coreVaccines.forEach { vaccine ->
            val statusView = layoutInflater.inflate(R.layout.vaccine_status_item, binding.layoutCoreVaccines, false)
            val tvStatus = statusView.findViewById<TextView>(R.id.tvStatus)
            val tvName = statusView.findViewById<TextView>(R.id.tvVaccineName)

            tvStatus.text = if (vaccinatedVaccines.contains(vaccine.name)) "✓" else "○"
            tvName.text = vaccine.name

            binding.layoutCoreVaccines.addView(statusView)
        }

        // Booster Vaccines
        binding.layoutBoosterVaccines.removeAllViews()
        boosterVaccines.forEach { vaccine ->
            val statusView = layoutInflater.inflate(R.layout.vaccine_status_item, binding.layoutBoosterVaccines, false)
            val tvStatus = statusView.findViewById<TextView>(R.id.tvStatus)
            val tvName = statusView.findViewById<TextView>(R.id.tvVaccineName)

            tvStatus.text = if (vaccinatedVaccines.contains(vaccine.name)) "✓" else "○"
            tvName.text = vaccine.name

            binding.layoutBoosterVaccines.addView(statusView)
        }
    }

    private fun updateRecentVaccines() {
        val recentVaccines = allRecords.take(3)
        binding.layoutRecentVaccines.removeAllViews()

        recentVaccines.forEach { record ->
            val itemView = layoutInflater.inflate(R.layout.item_recent_vaccine, binding.layoutRecentVaccines, false)

            itemView.findViewById<TextView>(R.id.tvVaccineName).text = "Name ${record.vaccineName}"
            itemView.findViewById<TextView>(R.id.tvDose).text = "Dose ${record.dose}"

            if (!record.place.isNullOrEmpty()) {
                itemView.findViewById<TextView>(R.id.tvPlace).text = "Place ${record.place}"
            } else {
                itemView.findViewById<TextView>(R.id.tvPlace).visibility = View.GONE
            }

            val timeFormat = SimpleDateFormat(timePattern, Locale.US)
            itemView.findViewById<TextView>(R.id.tvTime).text = "Time ${timeFormat.format(Date(record.timestamp))}"

            binding.layoutRecentVaccines.addView(itemView)
        }
    }

    private fun updateAllRecords() {
        adapter.submitList(allRecords)

        if (allRecords.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvAllRecords.visibility = View.GONE
        } else {
            binding.tvNoData.visibility = View.GONE
            binding.rvAllRecords.visibility = View.VISIBLE
        }
    }

    private fun toggleViewMode() {
        if (binding.rvRecentVaccines.visibility == View.VISIBLE) {
            binding.rvRecentVaccines.visibility = View.GONE
            binding.rvAllRecords.visibility = View.VISIBLE
            binding.btnViewAll.text = "Show Recent"
        } else {
            binding.rvRecentVaccines.visibility = View.VISIBLE
            binding.rvAllRecords.visibility = View.GONE
            binding.btnViewAll.text = "View All"
        }
    }

    private fun markVaccineAsDone(record: VaccinationRecord) {
        // TODO: Implement mark as done logic
        Toast.makeText(requireContext(), "Marked as done", Toast.LENGTH_SHORT).show()
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
            .setTitle("Delete Vaccination Record")
            .setMessage("Are you sure you want to delete ${record.vaccineName} Dose ${record.dose} from ${record.dateString}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteVaccinationRecord(record)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteVaccinationRecord(record: VaccinationRecord) {
        val currentPet = baseActivity.selectedPet ?: return
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        // Delete associated event
        deleteAssociatedEvent(record.id)

        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .document(currentPet.petId)
            .collection("records")
            .document(record.id)
            .delete()
            .addOnSuccessListener {
                showMessage("Record deleted")
                loadVaccinationRecords()
            }
            .addOnFailureListener { e ->
                showMessage("Error: ${e.message}")
            }
    }

    private fun deleteAssociatedEvent(vaccinationId: String) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .whereEqualTo("sourceId", vaccinationId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    doc.reference.delete()
                }
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