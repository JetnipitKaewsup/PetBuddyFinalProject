package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ItemExpenseBinding
import com.example.petbuddy.databinding.ItemExpenseHeaderBinding
import com.example.petbuddy.model.ExpenseListItem
import com.example.petbuddy.model.ExpenseRecord

class ExpenseAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private var list: List<ExpenseListItem> = listOf()

    class HeaderViewHolder(
        val binding: ItemExpenseHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class ExpenseViewHolder(
        val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {

        return when (list[position]) {

            is ExpenseListItem.Header -> TYPE_HEADER
            is ExpenseListItem.Expense -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_HEADER) {

            val binding = ItemExpenseHeaderBinding.inflate(
                inflater,
                parent,
                false
            )

            HeaderViewHolder(binding)

        } else {

            val binding = ItemExpenseBinding.inflate(
                inflater,
                parent,
                false
            )

            ExpenseViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (val item = list[position]) {

            is ExpenseListItem.Header -> {

                val h = holder as HeaderViewHolder
                h.binding.textDateHeader.text = item.date
            }

            is ExpenseListItem.Expense -> {

                val e = holder as ExpenseViewHolder
                val record = item.record
                val binding = e.binding

                binding.textPetName.text =
                    record.category.replaceFirstChar { it.uppercase() }

                val currencySymbol =
                    if (record.currency == "THB") "฿" else "$"

                binding.textMoney.text =
                    "$currencySymbol${record.amount}"

                binding.textDate.text = record.date

                val iconRes = when (record.category) {

                    "food" -> R.drawable.pet_bowl
                    "medical" -> R.drawable.vaccine
                    "toy" -> R.drawable.toy
                    "other" -> R.drawable.other

                    else -> R.drawable.other
                }

                binding.iconCategory.setImageResource(iconRes)
            }
        }
    }


    override fun getItemCount(): Int = list.size

    private fun buildSectionList(
        expenses: List<ExpenseRecord>
    ): List<ExpenseListItem> {

        val sorted = expenses.sortedByDescending { it.timestamp }

        val result = mutableListOf<ExpenseListItem>()

        var lastDate: String? = null

        for (expense in sorted) {

            if (expense.date != lastDate) {

                result.add(
                    ExpenseListItem.Header(expense.date)
                )

                lastDate = expense.date
            }

            result.add(
                ExpenseListItem.Expense(expense)
            )
        }

        return result
    }

    fun submitList(expenses: List<ExpenseRecord>) {

        list = buildSectionList(expenses)

        notifyDataSetChanged()
    }
}