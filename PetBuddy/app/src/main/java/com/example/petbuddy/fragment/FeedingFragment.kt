package com.example.petbuddy.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FeedingFragment : Fragment() {

    private var _binding: FragmentFeedingBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity

    private lateinit var feedingAdapter: FeedingTodayAdapter
    private lateinit var recordAdapter: FeedingRecordAdapter

    private var feedingSchedules: List<FeedingSchedule> = emptyList()
    private var allRecords: List<FeedingRecord> = emptyList()
    private var filteredRecords: List<FeedingRecord> = emptyList()

    private var petMap: Map<String, Pet> = emptyMap()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private val dayNameToIndex = mapOf(
        "sun" to Calendar.SUNDAY, "sunday" to Calendar.SUNDAY,
        "mon" to Calendar.MONDAY, "monday" to Calendar.MONDAY,
        "tue" to Calendar.TUESDAY, "tuesday" to Calendar.TUESDAY,
        "wed" to Calendar.WEDNESDAY, "wednesday" to Calendar.WEDNESDAY,
        "thu" to Calendar.THURSDAY, "thursday" to Calendar.THURSDAY,
        "fri" to Calendar.FRIDAY, "friday" to Calendar.FRIDAY,
        "sat" to Calendar.SATURDAY, "saturday" to Calendar.SATURDAY
    )

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
        setupListeners()
        setUpView()
        loadData()
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setUpView(){
        binding.tvViewAllToday.setOnClickListener {
            val fragment = TodayFeedingFragment()

            parentFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment)
                .addToBackStack(null).commit()
        }
    }

    private fun setupRecyclerView() {

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

        binding.showFeeding.layoutManager =
            LinearLayoutManager(requireContext())

        binding.showFeeding.adapter = feedingAdapter


        recordAdapter = FeedingRecordAdapter(
            emptyMap()
        ) { petId ->
            baseActivity.showToast("Pet clicked: $petId")
        }

        binding.showRecords.layoutManager =
            LinearLayoutManager(requireContext())

        binding.showRecords.adapter = recordAdapter
    }

    private fun setupListeners() {

        binding.cardActiveFeeding.setOnClickListener {

            val fragment = SettingFeedingFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnAddFeeding.setOnClickListener {

            val fragment = FeedingAlarmFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // View All schedules
        binding.tvViewAllToday.setOnClickListener {

            feedingAdapter.submitList(feedingSchedules)
        }

        binding.etSearchFood.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterRecords(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {

        baseActivity.loadAllPets { pets ->

            petMap = pets.associateBy { it.petId }

            feedingAdapter.updatePetMap(petMap)
            recordAdapter.updatePetMap(petMap)

            loadSchedules()
            loadFeedingRecords()
        }
    }

    private fun loadSchedules() {

        baseActivity.loadFeedingSchedules { schedules ->

            val calendar = Calendar.getInstance()

            val todayDate = dateFormat.format(calendar.time)
            val todayDayIndex = calendar.get(Calendar.DAY_OF_WEEK)

            val todaySchedules = schedules.filter { schedule ->

                shouldShowSchedule(schedule, todayDate, todayDayIndex)

            }.sortedWith(compareBy({ it.hour }, { it.minute }))

            feedingSchedules = todaySchedules


            val limitedList =
                if (todaySchedules.size > 3)
                    todaySchedules.take(3)
                else
                    todaySchedules

            feedingAdapter.submitList(limitedList)

            binding.tvViewAllToday.visibility =
                if (todaySchedules.size > 3)
                    View.VISIBLE
                else
                    View.GONE
        }
    }

    private fun loadFeedingRecords() {

        baseActivity.loadFeedingRecords { records ->

            allRecords = records.sortedByDescending { it.fedAt }

            filteredRecords = allRecords

            recordAdapter.submitList(filteredRecords)
        }
    }

    private fun shouldShowSchedule(
        schedule: FeedingSchedule,
        todayDate: String,
        todayDayIndex: Int
    ): Boolean {

        if (!schedule.isActive) return false

        if (schedule.completedDays?.contains(todayDate) == true)
            return false

        return when (schedule.repeatType.lowercase(Locale.ENGLISH)) {

            "once" ->
                schedule.completedDays.isNullOrEmpty()

            "daily", "everyday" ->
                true

            "weekly", "custom" ->
                isTodaySelected(schedule.days, todayDayIndex)

            else -> true
        }
    }

    private fun isTodaySelected(
        selectedDays: List<String>?,
        todayDayIndex: Int
    ): Boolean {

        return selectedDays?.any {

            dayNameToIndex[it.lowercase(Locale.ENGLISH)] == todayDayIndex

        } == true
    }

    private fun onFeedingDone(schedule: FeedingSchedule) {

        val calendar = Calendar.getInstance()

        val todayDate = dateFormat.format(calendar.time)

        val record = FeedingRecord(
            scheduleId = schedule.id,
            foodName = schedule.title,
            foodType = schedule.type,
            petIds = schedule.petIds,
            fedAt = calendar.timeInMillis
        )

        baseActivity.saveFeedingRecord(record)

        baseActivity.markScheduleCompleted(
            schedule.id,
            todayDate
        ) {

            baseActivity.showToast(
                "Feeding recorded for ${schedule.title}"
            )

            loadSchedules()
            loadFeedingRecords()
        }
    }

    private fun filterRecords(query: String) {

        filteredRecords =
            if (query.isEmpty()) {
                allRecords
            } else {
                allRecords.filter {

                    it.foodName.contains(query, true) ||
                            it.foodType.contains(query, true)
                }
            }

        recordAdapter.submitList(filteredRecords)
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}