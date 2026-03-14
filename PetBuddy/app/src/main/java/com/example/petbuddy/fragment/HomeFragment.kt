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
    private val timeFormatter = SimpleDateFormat("H:mm", Locale.ENGLISH)

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
        _binding?.homeDate?.text = dateFormatter.format(Date())
    }

    private fun setupRecycler() {

        val bindingSafe = _binding ?: return

        // Todo Today
        todoAdapter = TodoAdapter(todoList)

        bindingSafe.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todoAdapter
        }

        // Feeding Today
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

        bindingSafe.recyclerView3.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedingAdapter
        }

        // Upcoming Activities
        upcomingAdapter = UpcomingAdapter(upcomingList)

        bindingSafe.recyclerView2.apply {
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

                val bindingSafe = _binding ?: return@addOnSuccessListener

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

                val bindingSafe = _binding ?: return@addOnSuccessListener

                for (petDoc in pets) {

                    petDoc.reference
                        .collection("records")
                        .get()
                        .addOnSuccessListener { records ->

                            val safe = _binding ?: return@addOnSuccessListener

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

        val calendar = Calendar.getInstance()

        val todayShort =
            SimpleDateFormat("EEE", Locale.getDefault())
                .format(calendar.time)

        val todayFull =
            SimpleDateFormat("EEEE", Locale.getDefault())
                .format(calendar.time)

        val todayDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.time)

        baseActivity.db.collection("users")
            .document(userId)
            .collection("pets")
            .get()
            .addOnSuccessListener { pets ->

                val bindingSafe = _binding ?: return@addOnSuccessListener

                for (doc in pets) {

                    val pet = doc.toObject(Pet::class.java)

                    petMap[doc.id] = pet
                }

                baseActivity.db.collection("users")
                    .document(userId)
                    .collection("feeding_schedules")
                    .get()
                    .addOnSuccessListener { docs ->

                        val safe = _binding ?: return@addOnSuccessListener

                        for (doc in docs) {

                            val schedule =
                                doc.toObject(FeedingSchedule::class.java)

                            if (!schedule.isActive) continue

                            // ⭐ check completed today
                            if (schedule.completedDays?.contains(todayDate) == true) {
                                continue
                            }

                            val repeat = schedule.repeatType.lowercase()

                            val showToday = when (repeat) {

                                "once" -> schedule.completedDays.isNullOrEmpty()

                                "daily", "everyday" -> true

                                "weekly", "custom" -> {

                                    schedule.days.contains(todayShort) ||
                                            schedule.days.contains(todayFull)
                                }

                                else -> true
                            }

                            if (showToday) {
                                feedingList.add(schedule)
                            }
                        }

                        feedingList.sortWith(
                            compareBy({ it.hour }, { it.minute })
                        )

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

                val bindingSafe = _binding ?: return@addOnSuccessListener

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

        baseActivity.loadExpenses { list: List<ExpenseRecord> ->

            val bindingSafe = _binding ?: return@loadExpenses

            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            var total = 0.0

            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            for (expense in list) {

                try {

                    val date = formatter.parse(expense.date)

                    if (date != null) {

                        val cal = Calendar.getInstance()
                        cal.time = date

                        val month = cal.get(Calendar.MONTH)
                        val year = cal.get(Calendar.YEAR)

                        if (month == currentMonth && year == currentYear) {

                            if (expense.currency == "THB") {
                                total += expense.amount
                            }
                        }
                    }

                } catch (e: Exception) {
                }
            }

            bindingSafe.tvExpenseAmount.text = "฿%.2f".format(total)
        }
    }

    private fun setUpButton() {

        _binding?.cardExpense?.setOnClickListener {

            val fragment = ExpenseFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        _binding?.upcomingRow?.setOnClickListener {
            val fragment = ScheduleFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,fragment)
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
