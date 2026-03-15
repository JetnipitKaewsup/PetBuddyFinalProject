package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.FeedingTodayAdapter
import com.example.petbuddy.databinding.FragmentTodayFeedingBinding
import com.example.petbuddy.model.*
import java.text.SimpleDateFormat
import java.util.*

class TodayFeedingFragment : Fragment() {

    private var _binding: FragmentTodayFeedingBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: FeedingTodayAdapter

    private val dateFormatter =
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentTodayFeedingBinding.inflate(
                inflater,
                container,
                false
            )

        baseActivity = activity as BaseActivity

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        setupRecycler()

        loadTodayFeeding()

        binding.toolbar.setNavigationOnClickListener {

            parentFragmentManager.popBackStack()

        }
    }

    private fun setupRecycler() {

        adapter = FeedingTodayAdapter(
            feedingList = emptyList(),
            petMap = emptyMap(),
            onDoneClick = { schedule ->
                onFeedingDone(schedule)
            },
            onPetClick = { }
        )

        binding.recyclerTodayFeeding.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerTodayFeeding.adapter = adapter
    }

    private fun loadTodayFeeding() {

        val userId =
            baseActivity.getCurrentUserIdSafe()
                ?: return

        val todayDate =
            dateFormatter.format(Date())

        val feedingList =
            mutableListOf<FeedingSchedule>()

        val petMap =
            mutableMapOf<String, Pet>()

        val calendar =
            Calendar.getInstance()

        val todayDayName =
            calendar.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.LONG,
                Locale.ENGLISH
            ) ?: ""

        baseActivity.db.collection("users")
            .document(userId)
            .collection("pets")
            .get()
            .addOnSuccessListener { pets ->

                for (doc in pets) {

                    petMap[doc.id] =
                        doc.toObject(Pet::class.java)
                }

                baseActivity.db.collection("users")
                    .document(userId)
                    .collection("feeding_schedules")
                    .get()
                    .addOnSuccessListener { docs ->

                        val safe = _binding ?: return@addOnSuccessListener

                        for (doc in docs) {

                            val schedule =
                                doc.toObject(
                                    FeedingSchedule::class.java
                                ).copy(id = doc.id)

                            if (!schedule.isActive) continue

                            if (schedule.completedDays?.contains(todayDate) == true)
                                continue

                            val repeat =
                                schedule.repeatType
                                    .trim()
                                    .lowercase(Locale.ENGLISH)

                            when (repeat) {

                                "daily", "everyday" -> {

                                    feedingList.add(schedule)
                                }

                                "weekly", "custom" -> {

                                    val days =
                                        schedule.days ?: emptyList()

                                    if (days.any {

                                            it.trim()
                                                .equals(
                                                    todayDayName,
                                                    ignoreCase = true
                                                )

                                        }) {

                                        feedingList.add(schedule)
                                    }
                                }

                                "once" -> {

                                    schedule.createdAt?.let {

                                        val date =
                                            dateFormatter.format(
                                                it.toDate()
                                            )

                                        if (date == todayDate) {

                                            feedingList.add(schedule)
                                        }
                                    }
                                }
                            }
                        }

                        val sortedList =
                            feedingList.sortedWith(
                                compareBy(
                                    { it.hour },
                                    { it.minute }
                                )
                            )

                        adapter.updatePetMap(petMap)

                        adapter.submitList(sortedList)

                        safe.tvNoFeeding.visibility =
                            if (sortedList.isEmpty())
                                View.VISIBLE
                            else
                                View.GONE
                    }
            }
    }

    private fun onFeedingDone(
        schedule: FeedingSchedule
    ) {

        val calendar = Calendar.getInstance()

        val todayDate =
            dateFormatter.format(calendar.time)

        val record =
            FeedingRecord(
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

            loadTodayFeeding()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}