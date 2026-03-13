package com.example.petbuddy.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.FeedingRecordAdapter
import com.example.petbuddy.adapter.FeedingTodayAdapter
import com.example.petbuddy.databinding.FragmentFeedingBinding
import com.example.petbuddy.model.FeedingRecord
import com.example.petbuddy.model.FeedingSchedule
import com.example.petbuddy.model.Pet

class FeedingFragment : Fragment() {

    private var _binding: FragmentFeedingBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity

    private lateinit var feedingAdapter: FeedingTodayAdapter
    private lateinit var recordAdapter: FeedingRecordAdapter

    private var feedingSchedules: List<FeedingSchedule> = emptyList()
    private var petMap: Map<String, Pet> = emptyMap()

    // All records for filtering
    private var allRecords: List<FeedingRecord> = emptyList()

    // Filter state
    private var currentQuery: String = ""
    private var dateFilter: String = "ALL"

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

        baseActivity = activity as BaseActivity

        setupToolbar()
        setupRecyclerView()
        setupButtons()
        setupSearch()

        loadData()
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            baseActivity.onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {

        // Today's feeding schedules
        feedingAdapter = FeedingTodayAdapter(
            feedingList = emptyList(),
            petMap = emptyMap(),

            onDoneClick = { schedule ->
                onFeedingDone(schedule)
            },

            onPetClick = { petId ->
                baseActivity.showToast("Pet clicked: $petId")
            }
        )

        binding.showFeeding.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = feedingAdapter
        }

        // Feeding history
        recordAdapter = FeedingRecordAdapter(
            petMap
        ) { petId ->
            baseActivity.showToast("Pet clicked: $petId")
        }

        binding.showRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordAdapter
        }
    }

    private fun setupButtons() {

        binding.btnAddFeeding.setOnClickListener {

            val fragment = FeedingAlarmFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.showFeedingSchedules.setOnClickListener {

            val fragment = SettingFeedingFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Filter button
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    // Search filter
    private fun setupSearch() {

        binding.etSearchFood.addTextChangedListener { text ->

            currentQuery = text.toString().lowercase()

            applyFilters()
        }
    }

    // Filter dialog
    private fun showFilterDialog() {

        val options = arrayOf(
            "All",
            "Today",
            "This Week"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Date")
            .setItems(options) { _, which ->

                dateFilter = when (which) {
                    1 -> "TODAY"
                    2 -> "WEEK"
                    else -> "ALL"
                }

                applyFilters()
            }
            .show()
    }

    // Apply search + date filters
    private fun applyFilters() {

        var filtered = allRecords

        // Search filter
        if (currentQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.foodName.lowercase().contains(currentQuery)
            }
        }

        // Date filter
        val now = System.currentTimeMillis()

        filtered = when (dateFilter) {

            "TODAY" -> {
                val startOfDay = now - (24 * 60 * 60 * 1000)
                filtered.filter { it.fedAt >= startOfDay }
            }

            "WEEK" -> {
                val startOfWeek = now - (7 * 24 * 60 * 60 * 1000)
                filtered.filter { it.fedAt >= startOfWeek }
            }

            else -> filtered
        }

        recordAdapter.submitList(filtered)
    }

    private fun loadData() {

        baseActivity.loadAllPets { pets ->

            petMap = pets.associateBy { it.petId }

            feedingAdapter.updatePetMap(petMap)
            recordAdapter.updatePetMap(petMap)

            loadSchedules()
        }
    }

    private fun loadSchedules() {

        baseActivity.loadFeedingSchedules { schedules ->

            schedules.forEach {
                println("Schedule ${it.title} active=${it.isActive}")
            }

            feedingSchedules = schedules.filter { it.isActive }

            feedingAdapter.submitList(feedingSchedules)

            loadFeedingRecords()
        }
    }

    private fun loadFeedingRecords() {

        baseActivity.loadFeedingRecords { records ->

            allRecords = records

            applyFilters()
        }
    }

    private fun onFeedingDone(schedule: FeedingSchedule) {

        val record = FeedingRecord(
            scheduleId = schedule.id,
            foodName = schedule.title,
            foodType = schedule.type,
            petIds = schedule.petIds,
            fedAt = System.currentTimeMillis()
        )

        val userId = baseActivity.mAuth.currentUser?.uid ?: return

        // Save feeding record
        baseActivity.saveFeedingRecord(record)

        // Update schedule
        baseActivity.db.collection("users")
            .document(userId)
            .collection("feeding_schedules")
            .document(schedule.id)
            .update("isActive", false)
            .addOnSuccessListener {

                baseActivity.showToast("Feeding recorded")

                feedingSchedules =
                    feedingSchedules.filter { it.id != schedule.id }

                feedingAdapter.submitList(feedingSchedules)

                loadFeedingRecords()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}