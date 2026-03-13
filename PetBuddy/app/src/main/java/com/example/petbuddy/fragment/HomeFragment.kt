package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.FeedingTodayAdapter
import com.example.petbuddy.adapter.TodoAdapter
import com.example.petbuddy.adapter.UpcomingAdapter
import com.example.petbuddy.databinding.FragmentHomeBinding
import com.example.petbuddy.model.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity

    private lateinit var todoAdapter: TodoAdapter
    private lateinit var feedingAdapter: FeedingTodayAdapter
    private lateinit var upcomingAdapter: UpcomingAdapter

    private val todoList = mutableListOf<String>()
    private val upcomingList = mutableListOf<UpcomingActivity>()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        baseActivity = activity as BaseActivity

        setupDate()
        setupRecycler()

        loadTodayData()
        loadUpcomingActivities()
        loadMonthlyExpense()

        setUpButton()

        return binding.root
    }

    private fun setupDate() {
        binding.homeDate.text = dateFormatter.format(Date())
    }

    private fun setupRecycler() {

        todoAdapter = TodoAdapter(todoList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todoAdapter
        }

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

        binding.recyclerView3.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedingAdapter
        }

        // Upcoming Activities
        upcomingAdapter = UpcomingAdapter(emptyList())

        binding.recyclerView2.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingAdapter
        }
    }

    private fun loadTodayData() {

        todoList.clear()

        loadTodayEvents()
        loadTodayVaccinations()
        loadTodayFeeding()
    }

    private fun getStartOfDay(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun getEndOfDay(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        return cal.time
    }

    private fun loadTodayEvents() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .whereGreaterThanOrEqualTo("startDate", getStartOfDay())
            .whereLessThanOrEqualTo("startDate", getEndOfDay())
            .get()
            .addOnSuccessListener { docs ->

                for (doc in docs) {

                    val event = doc.toObject(Event::class.java)

                    val time = event.startDate.toDate().let {
                        timeFormatter.format(it)
                    }

                    todoList.add("$time  ${event.title}")
                }

                todoAdapter.notifyDataSetChanged()
            }
    }

    private fun loadTodayVaccinations() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .get()
            .addOnSuccessListener { pets ->

                for (petDoc in pets) {

                    petDoc.reference
                        .collection("records")
                        .get()
                        .addOnSuccessListener { records ->

                            for (doc in records) {

                                val record = doc.toObject(VaccinationRecord::class.java)

                                val date = Date(record.timestamp)

                                if (date.after(getStartOfDay()) && date.before(getEndOfDay())) {

                                    val time = timeFormatter.format(date)

                                    todoList.add("💉 $time  ${record.vaccineName}")
                                }
                            }

                            todoAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun loadTodayFeeding() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        val feedingList = mutableListOf<FeedingSchedule>()
        val petMap = mutableMapOf<String, Pet>()

        baseActivity.db.collection("users")
            .document(userId)
            .collection("pets")
            .get()
            .addOnSuccessListener { pets ->

                for (doc in pets) {

                    val pet = doc.toObject(Pet::class.java)
                    petMap[doc.id] = pet
                }

                baseActivity.db.collection("users")
                    .document(userId)
                    .collection("feeding_schedules")
                    .get()
                    .addOnSuccessListener { docs ->

                        for (doc in docs) {

                            val schedule = doc.toObject(FeedingSchedule::class.java)

                            if (schedule.isActive) {
                                feedingList.add(schedule)
                            }
                        }

                        feedingAdapter.updatePetMap(petMap)
                        feedingAdapter.submitList(feedingList)
                    }
            }
    }

    private fun loadUpcomingActivities() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        val now = System.currentTimeMillis()

        val activities = mutableListOf<UpcomingActivity>()

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .get()
            .addOnSuccessListener { docs ->

                for (doc in docs) {

                    val event = doc.toObject(Event::class.java)

                    val time = event.startDate.toDate().time

                    if (time > now) {

                        activities.add(
                            UpcomingActivity(
                                title = event.title,
                                type = "Event",
                                time = time
                            )
                        )
                    }
                }

                upcomingAdapter.submitList(activities)
            }
    }

    private fun loadMonthlyExpense() {

        baseActivity.loadMonthlyExpense { total ->

            _binding?.tvExpenseAmount?.text = "฿%.2f".format(total)
        }
    }

    private fun setUpButton() {

        binding.cardExpense.setOnClickListener {

            val fragment = ExpenseFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun onFeedingDone(schedule: FeedingSchedule) {

        baseActivity.showToast("Feeding done: ${schedule.title}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}