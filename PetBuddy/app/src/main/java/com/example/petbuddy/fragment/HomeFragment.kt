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

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

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

    private fun setUpButton() {
        binding.cardExpense.setOnClickListener {
            val fragment = ExpenseFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.feedingRow.setOnClickListener {
            val fragment = FeedingFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.upcomingRow.setOnClickListener {
            val fragment = ScheduleFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupDate() {
        binding.homeDate.text = dateFormatter.format(Date())
    }

    private fun setupRecycler() {

        todoAdapter = TodoAdapter(todoList)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = todoAdapter

        feedingAdapter = FeedingTodayAdapter(
            feedingList = emptyList(),
            petMap = emptyMap(),
            onDoneClick = { schedule -> onFeedingDone(schedule) },
            onPetClick = { }
        )

        binding.recyclerView3.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView3.adapter = feedingAdapter

        upcomingAdapter = UpcomingAdapter(upcomingList)

        binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView2.adapter = upcomingAdapter
    }

    private fun loadTodayData() {
        todoList.clear()
        loadTodayEvents()
        loadTodayFeeding()
    }

    private fun loadTodayEvents() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        val today = dateFormatter.format(Date())

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .get()
            .addOnSuccessListener { docs ->

                val safe = _binding ?: return@addOnSuccessListener

                for (doc in docs) {

                    val event = doc.toObject(Event::class.java)

                    val eventDate = dateFormatter.format(event.startDate.toDate())

                    if (eventDate == today) {
                        todoList.add(event.title)
                    }
                }

                todoAdapter.notifyDataSetChanged()

                safe.tvNoTodo.visibility =
                    if (todoList.isEmpty()) View.VISIBLE else View.GONE
            }
    }


    private fun loadTodayFeeding() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        val feedingList = mutableListOf<FeedingSchedule>()
        val petMap = mutableMapOf<String, Pet>()

        val todayDate = dateFormatter.format(Date())

        val calendar = Calendar.getInstance()
        val todayDayName =
            calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH) ?: ""

        baseActivity.db.collection("users")
            .document(userId)
            .collection("pets")
            .get()
            .addOnSuccessListener { pets ->

                for (doc in pets) {
                    petMap[doc.id] = doc.toObject(Pet::class.java)
                }

                baseActivity.db.collection("users")
                    .document(userId)
                    .collection("feeding_schedules")
                    .get()
                    .addOnSuccessListener { docs ->

                        val safe = _binding ?: return@addOnSuccessListener

                        feedingList.clear()

                        for (doc in docs) {

                            val schedule = doc.toObject(FeedingSchedule::class.java)
                                .copy(id = doc.id)

                            if (!schedule.isActive) continue

                            val repeat = schedule.repeatType
                                .trim()
                                .lowercase(Locale.ENGLISH)

                            when (repeat) {

                                // DAILY
                                "daily", "everyday" -> {
                                    feedingList.add(schedule)
                                }

                                // WEEKLY / CUSTOM
                                "weekly", "custom" -> {

                                    val days = schedule.days ?: emptyList()

                                    if (days.any {
                                            it.trim()
                                                .equals(todayDayName, ignoreCase = true)
                                        }) {

                                        feedingList.add(schedule)
                                    }
                                }

                                // ONCE
                                "once" -> {

                                    schedule.createdAt?.let {

                                        val date =
                                            dateFormatter.format(it.toDate())

                                        if (date == todayDate) {
                                            feedingList.add(schedule)
                                        }
                                    }
                                }
                            }
                        }

                        feedingAdapter.updatePetMap(petMap)
                        feedingAdapter.submitList(feedingList)

                        safe.tvNoFeeding.visibility =
                            if (feedingList.isEmpty()) View.VISIBLE else View.GONE
                    }
            }
    }
    private fun loadUpcomingActivities() {

        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .get()
            .addOnSuccessListener { docs ->

                val safe = _binding ?: return@addOnSuccessListener

                val activities = mutableListOf<UpcomingActivity>()

                for (doc in docs) {

                    val event = doc.toObject(Event::class.java)

                    activities.add(
                        UpcomingActivity(
                            title = event.title,
                            type = "Event",
                            time = event.startDate.toDate().time
                        )
                    )
                }

                upcomingAdapter.submitList(activities)

                safe.tvNoUpcoming.visibility =
                    if (activities.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun loadMonthlyExpense() {

        baseActivity.loadExpenses { list ->

            val safe = _binding ?: return@loadExpenses

            val now = Calendar.getInstance()
            val currentMonth = now.get(Calendar.MONTH)
            val currentYear = now.get(Calendar.YEAR)

            var total = 0.0

            for (expense in list) {

                val cal = Calendar.getInstance()
                cal.timeInMillis = expense.timestamp

                val month = cal.get(Calendar.MONTH)
                val year = cal.get(Calendar.YEAR)

                if (month == currentMonth && year == currentYear) {

                    if (expense.currency == "THB") {
                        total += expense.amount
                    }
                }
            }

            safe.tvExpenseAmount.text = "฿%.2f".format(total)
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
