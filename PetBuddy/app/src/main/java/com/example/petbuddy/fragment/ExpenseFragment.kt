package com.example.petbuddy.fragment

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        baseActivity = activity as BaseActivity

        adapter = ExpenseAdapter(expenseList)

        binding.recyclerExpense.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerExpense.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupToolbar()
        setupRecyclerView()
        setupAddButton()
        loadExpenses()
        showCurrentMonth()
    }

    // -----------------------------
    // Toolbar
    // -----------------------------

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {

            parentFragmentManager.popBackStack()

        }
    }

    // -----------------------------
    // RecyclerView
    // -----------------------------

    private fun setupRecyclerView() {

        adapter = ExpenseAdapter(expenseList)

        binding.recyclerExpense.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerExpense.adapter = adapter
    }

    // -----------------------------
    // Add Expense Button
    // -----------------------------

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

    // -----------------------------
    // Load Expenses
    // -----------------------------

    private fun loadExpenses() {

        baseActivity.loadExpenses { list ->

            expenseList.clear()
            expenseList.addAll(list)

            adapter.notifyDataSetChanged()

            calculateMonthlyTotal()
        }
    }

    // -----------------------------
    // Show Current Month
    // -----------------------------

    private fun showCurrentMonth() {

        val calendar = Calendar.getInstance()

        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        binding.tvMonth.text = format.format(calendar.time)
    }

    // -----------------------------
    // Calculate Monthly Total
    // -----------------------------

    private fun calculateMonthlyTotal() {

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        var total = 0.0

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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

                // ignore parse error

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