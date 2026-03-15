package com.example.petbuddy.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentAddExpenseBinding
import com.example.petbuddy.model.ExpenseRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity

    private var selectedCategory = "food"
    private var selectedDate = ""

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        baseActivity = activity as BaseActivity

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupCategorySelection()
        setupDatePicker()
        setupSaveButton()
        setupBackButton()

        highlightCategory(binding.petBowl)

        updateDate()
    }


    private fun setupCategorySelection() {

        binding.petBowl.setOnClickListener {
            selectedCategory = "food"
            highlightCategory(binding.petBowl)
        }

        binding.vaccine.setOnClickListener {
            selectedCategory = "medical"
            highlightCategory(binding.vaccine)
        }

        binding.toy.setOnClickListener {
            selectedCategory = "toy"
            highlightCategory(binding.toy)
        }

        binding.other.setOnClickListener {
            selectedCategory = "other"
            highlightCategory(binding.other)
        }
    }

    private fun highlightCategory(selected: View) {

        val views = listOf(
            binding.petBowl,
            binding.vaccine,
            binding.toy,
            binding.other
        )

        views.forEach {
            it.alpha = 0.4f
        }

        selected.alpha = 1f
    }


    private fun setupDatePicker() {

        binding.dateRow.setOnClickListener {

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    calendar.set(year, month, day)
                    updateDate()

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDate() {

        val format = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        selectedDate = format.format(calendar.time)

        binding.expenseDate.setText(selectedDate)
    }

    private fun setupSaveButton() {

        binding.finishBtn.setOnClickListener {

            val amountText = binding.inputMoney.text.toString()

            if (amountText.isEmpty()) {
                baseActivity.showToast("Enter amount")
                return@setOnClickListener
            }

            val amount = amountText.toDouble()

            val record = ExpenseRecord(
                category = selectedCategory,
                amount = amount,
                currency = "THB",
                date = selectedDate,
                timestamp = System.currentTimeMillis()
            )

            baseActivity.saveExpenseRecord(record)

            baseActivity.showToast("Expense saved")

            parentFragmentManager.popBackStack()
        }
    }


    private fun setupBackButton() {

        binding.homeArrow3.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
