package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.TodoAdapter
import com.example.petbuddy.databinding.FragmentHomeBinding
import com.example.petbuddy.model.Event
import com.example.petbuddy.model.VaccinationRecord
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var todoAdapter: TodoAdapter

    private val todoList = mutableListOf<String>()

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
        loadMonthlyExpense()

        return binding.root
    }

    // ---------------- DATE ----------------

    private fun setupDate() {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.homeDate.text = formatter.format(Date())
    }

    // ---------------- RECYCLER ----------------

    private fun setupRecycler() {

        todoAdapter = TodoAdapter(todoList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todoAdapter
        }
    }

    // ---------------- MAIN LOAD ----------------

    private fun loadTodayData() {

        todoList.clear()

        loadTodayEvents()
        loadTodayVaccinations()
        loadTodayFeeding()
    }

    // ---------------- TIME HELPERS ----------------

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

    // ---------------- EVENTS ----------------

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

                    todoList.add("📅 ${event.title}")
                }

                todoAdapter.notifyDataSetChanged()
            }
    }

    // ---------------- VACCINATIONS ----------------

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

                                    todoList.add("💉 Vaccine: ${record.vaccineName}")
                                }
                            }

                            todoAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    // ---------------- FEEDING ----------------

    private fun loadTodayFeeding() {

        baseActivity.loadFeedingSchedules { schedules ->

            val now = Calendar.getInstance()

            schedules.forEach { schedule ->

                val scheduleCal = Calendar.getInstance()
                scheduleCal.set(Calendar.HOUR_OF_DAY, schedule.hour)
                scheduleCal.set(Calendar.MINUTE, schedule.minute)

                if (scheduleCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {

                    todoList.add("🍖 Feed: ${schedule.title}")
                }
            }

            todoAdapter.notifyDataSetChanged()
        }
    }

    // ---------------- EXPENSE SUMMARY ----------------

    private fun loadMonthlyExpense() {

        baseActivity.loadMonthlyExpense { total ->

            _binding?.tvExpenseAmount?.text = "฿%.2f".format(total)

        }
    }

    // ---------------- DESTROY ----------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}