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
import com.example.petbuddy.model.FeedingSchedule
import com.example.petbuddy.model.Pet
import java.text.SimpleDateFormat
import java.util.*

class TodayFeedingFragment : Fragment() {

    private var _binding: FragmentTodayFeedingBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: FeedingTodayAdapter

    private var petMap: Map<String, Pet> = emptyMap()

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    // 🔥 map Mon → Calendar
    private val dayMap = mapOf(
        "sun" to Calendar.SUNDAY,
        "mon" to Calendar.MONDAY,
        "tue" to Calendar.TUESDAY,
        "wed" to Calendar.WEDNESDAY,
        "thu" to Calendar.THURSDAY,
        "fri" to Calendar.FRIDAY,
        "sat" to Calendar.SATURDAY
    )

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

        baseActivity = requireActivity() as BaseActivity

        setupToolbar()
        setupRecycler()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {

        adapter = FeedingTodayAdapter(
            feedingList = emptyList(),
            petMap = petMap,
            onDoneClick = { schedule ->
                onFeedingDone(schedule)
            },
            onPetClick = { }
        )

        binding.recyclerTodayFeeding.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerTodayFeeding.adapter = adapter
    }

    private fun loadData() {

        baseActivity.loadAllPets { pets ->

            petMap = pets.associateBy { it.petId }
            adapter.updatePetMap(petMap)

            baseActivity.loadFeedingSchedules { schedules ->

                val calendar = Calendar.getInstance()

                val todayDate = dateFormat.format(calendar.time)
                val todayIndex = calendar.get(Calendar.DAY_OF_WEEK)

                val todaySchedules = schedules.filter { schedule ->

                    if (!schedule.isActive) return@filter false

                    val repeat =
                        schedule.repeatType
                            ?.trim()
                            ?.lowercase(Locale.ENGLISH)
                            ?: ""

                    when (repeat) {

                        "daily", "everyday" -> true

                        "weekly", "custom" -> {

                            val days = schedule.days ?: emptyList()

                            days.any {
                                val key =
                                    it.trim().lowercase(Locale.ENGLISH)
                                dayMap[key] == todayIndex
                            }
                        }

                        "once" -> {

                            schedule.createdAt?.let {
                                val date =
                                    dateFormat.format(it.toDate())
                                date == todayDate
                            } ?: false
                        }

                        else -> false
                    }
                }.sortedWith(compareBy({ it.hour }, { it.minute }))

                adapter.submitList(todaySchedules)

                binding.tvNoFeeding.visibility =
                    if (todaySchedules.isEmpty())
                        View.VISIBLE
                    else
                        View.GONE
            }
        }
    }

    private fun onFeedingDone(schedule: FeedingSchedule) {

        val calendar = Calendar.getInstance()

        val todayDate = dateFormat.format(calendar.time)

        baseActivity.markScheduleCompleted(
            schedule.id,
            todayDate
        ) {

            baseActivity.showToast(
                "Feeding recorded for ${schedule.title}"
            )

            loadData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}