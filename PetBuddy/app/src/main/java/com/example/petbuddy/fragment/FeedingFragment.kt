package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        loadData()
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            baseActivity.onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {

        // Feeding schedules (today)
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

            // Debug log
            schedules.forEach {
                println("Schedule ${it.title} active=${it.isActive}")
            }

            // Show only active schedules
            feedingSchedules = schedules.filter { it.isActive }

            feedingAdapter.submitList(feedingSchedules)

            loadFeedingRecords()
        }
    }

    private fun loadFeedingRecords() {

        baseActivity.loadFeedingRecords { records ->

            recordAdapter.submitList(records)
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

        // Update schedule in Firestore
        baseActivity.db.collection("users")
            .document(userId)
            .collection("feeding_schedules")
            .document(schedule.id)
            .update("isActive", false)
            .addOnSuccessListener {

                baseActivity.showToast("Feeding recorded")

                // Remove schedule from UI immediately
                feedingSchedules =
                    feedingSchedules.filter { it.id != schedule.id }

                feedingAdapter.submitList(feedingSchedules)

                // Reload history
                loadFeedingRecords()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}