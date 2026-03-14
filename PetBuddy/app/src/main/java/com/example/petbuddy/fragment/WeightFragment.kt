package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.WeightRecordAdapter
import com.example.petbuddy.databinding.FragmentWeightBinding
import com.example.petbuddy.databinding.ItemWeightRecordBinding
import com.example.petbuddy.model.WeightRecord
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.navigation.NavigationManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: WeightRecordAdapter
    private lateinit var navigator: MainNavigator
    private var weightRecords = mutableListOf<WeightRecord>()
    private var isDescending = true

    companion object {
        private const val TAG = "WeightFragment"
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
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupUI()
        setupRecyclerView()
        loadWeightRecords()
    }

    override fun onResume() {
        super.onResume()
        loadWeightRecords()
    }

    private fun setupUI() {
        val currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            showMessage("Please select a pet first")
            parentFragmentManager.popBackStack()
            return
        }

        binding.tvPetName.text = currentPet.petName

        binding.fabAddWeight.setOnClickListener {
            navigateToAddWeight()
        }

        binding.btnSortRecord.setOnClickListener {
            toggleSortOrder()
        }

    }
    private fun setupToolbar(){
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    private fun setupRecyclerView() {
        adapter = WeightRecordAdapter(
            onItemClick = { record ->
                showEditDeleteDialog(record)
            },
            onItemLongClick = { record ->
                showDeleteConfirmation(record)
            }
        )

        binding.rvContainer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContainer.adapter = adapter
        binding.rvContainer.setHasFixedSize(true)
    }

    private fun loadWeightRecords() {
        val currentPet = baseActivity.selectedPet ?: return
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        binding.progressBar.visibility = View.VISIBLE

        val query = baseActivity.db.collection("users")
            .document(userId)
            .collection("weights")
            .document(currentPet.petId)
            .collection("records")
            .orderBy("timestamp", if (isDescending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)

        query.get()
            .addOnSuccessListener { snapshot ->
                weightRecords.clear()
                weightRecords.addAll(snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(WeightRecord::class.java)?.copy(
                            id = document.id
                        )
                    } catch (e: Exception) {
                        null
                    }
                })

                adapter.submitList(weightRecords.toList())
                updateWeightStatus()

                binding.progressBar.visibility = View.GONE

                if (weightRecords.isEmpty()) {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.rvContainer.visibility = View.GONE
                } else {
                    binding.tvNoData.visibility = View.GONE
                    binding.rvContainer.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                showMessage("Failed to load data: ${e.message}")
            }
    }

    private fun updateWeightStatus() {
        if (weightRecords.size < 2) {
            binding.tvWeightStatus.text = "Record weight at least 2 times to view trends"
            binding.tvWeightStatus.setTextColor(android.graphics.Color.GRAY)
            return
        }

        // ดึง 5 รายการล่าสุด
        val recentRecords = weightRecords
            .sortedByDescending { it.timestamp }
            .take(5)

        if (recentRecords.size < 2) {
            binding.tvWeightStatus.text = "Insufficient data"
            return
        }

        val firstWeight = recentRecords.last().weight
        val lastWeight = recentRecords.first().weight
        val difference = lastWeight - firstWeight
        val percentChange = (difference / firstWeight) * 100

        val statusText = when {
            Math.abs(difference) < 0.1 -> "Weight stable"
            difference > 0 -> "Weight increased ${String.format("%.1f", difference)} kg (${String.format("%.1f", percentChange)}%)"
            else -> "Weight decreased ${String.format("%.1f", Math.abs(difference))} kg (${String.format("%.1f", Math.abs(percentChange))}%)"
        }

        binding.tvWeightStatus.text = statusText
        binding.tvWeightStatus.setTextColor(
            when {
                Math.abs(difference) < 0.1 -> android.graphics.Color.parseColor("#757575")
                difference > 0 -> android.graphics.Color.parseColor("#F44336")
                else -> android.graphics.Color.parseColor("#4CAF50")
            }
        )
    }

    private fun toggleSortOrder() {
        isDescending = !isDescending
        binding.btnSortRecord.setImageResource(
            if (isDescending) R.drawable.ic_sort_descending
            else R.drawable.ic_sort_ascending
        )
        loadWeightRecords()

//        Snackbar.make(binding.root,
//            if (isDescending) "Sort by latest first" else "Sort by oldest first",
//            Snackbar.LENGTH_SHORT
//        ).show()
    }

    private fun navigateToAddWeight(record: WeightRecord? = null) {
        navigator.navigateToAddWeight(record)
    }

    private fun showEditDeleteDialog(record: WeightRecord) {
        val options = arrayOf("แก้ไข", "ลบ")

        AlertDialog.Builder(requireContext())
            .setTitle("จัดการข้อมูลน้ำหนัก")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToAddWeight(record)
                    1 -> showDeleteConfirmation(record)
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun showDeleteConfirmation(record: WeightRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete weight record")
            .setMessage("Are you sure you want to delete the weight record for ${record.dateString} ?")
            .setPositiveButton("Delete") { _, _ ->
                deleteWeightRecord(record)
            }
            .setNegativeButton("Cancle", null)
            .show()
    }

    private fun deleteWeightRecord(record: WeightRecord) {
        val currentPet = baseActivity.selectedPet ?: return
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("weights")
            .document(currentPet.petId)
            .collection("records")
            .document(record.id)
            .delete()
            .addOnSuccessListener {
                showMessage("Record deleted successfully")
                loadWeightRecords()
            }
            .addOnFailureListener { e ->
                showMessage("Failed to delete record : ${e.message}")
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