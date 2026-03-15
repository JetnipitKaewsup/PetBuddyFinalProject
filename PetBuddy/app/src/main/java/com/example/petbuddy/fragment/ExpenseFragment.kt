package com.example.petbuddy.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.ExpenseAdapter
import com.example.petbuddy.databinding.FragmentExpenseBinding
import com.example.petbuddy.model.ExpenseRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseFragment : Fragment() {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: ExpenseAdapter

    private val expenseList = mutableListOf<ExpenseRecord>()

    private var currentFilter = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        baseActivity = activity as BaseActivity

        setupRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupToolbar()
        setupAddButton()
        setupFilterButton()

        loadExpenses()
        showCurrentMonth()
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {

            parentFragmentManager.popBackStack()

        }
    }

    private fun setupRecyclerView() {

        adapter = ExpenseAdapter()

        binding.recyclerExpense.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerExpense.adapter = adapter
    }
    private fun setupFilterButton() {

        binding.btnFilter.setOnClickListener {

            val options = arrayOf(
                "All",
                "This Month",
                "Last Month",
                "Food",
                "Medical",
                "Toy",
                "Other"
            )

            AlertDialog.Builder(requireContext())
                .setTitle("Filter Expenses")
                .setItems(options) { _, which ->

                    currentFilter = options[which]

                    applyFilter()

                }
                .show()
        }
    }

    // -----------------------------
    // Apply Filter
    // -----------------------------

    private fun applyFilter() {

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        val filtered = expenseList.filter { expense ->

            try {

                val date = dateFormat.parse(expense.date)

                val cal = Calendar.getInstance()
                if (date != null) cal.time = date

                val month = cal.get(Calendar.MONTH)
                val year = cal.get(Calendar.YEAR)

                when (currentFilter) {

                    "All" -> true

                    "This Month" ->
                        month == currentMonth && year == currentYear

                    "Last Month" ->
                        month == currentMonth - 1 && year == currentYear

                    "Food" ->
                        expense.category.lowercase() == "food"

                    "Medical" ->
                        expense.category.lowercase() == "medical"

                    "Toy" ->
                        expense.category.lowercase() == "toy"

                    "Other" ->
                        expense.category.lowercase() == "other"

                    else -> true
                }

            } catch (e: Exception) {

                true

            }
        }

        adapter.submitList(filtered)
    }


    private fun setupAddButton() {

        binding.btnAddExpense.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    AddExpenseFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadExpenses() {

        baseActivity.loadExpenses { list ->

            expenseList.clear()
            expenseList.addAll(list)

            adapter.submitList(expenseList)

            calculateMonthlyTotal()
        }
    }


    private fun showCurrentMonth() {

        val calendar = Calendar.getInstance()

        val format = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

        binding.tvMonth.text = format.format(calendar.time)
    }

    private fun calculateMonthlyTotal() {

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        var total = 0.0

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        for (expense in expenseList) {

            try {

                val date = dateFormat.parse(expense.date)

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

        binding.tvTotalExpense.text = "฿%.2f".format(total)
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}