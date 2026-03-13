package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ItemExpenseBinding
import com.example.petbuddy.model.ExpenseRecord

class ExpenseAdapter(
    private var list: List<ExpenseRecord>
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        val binding = holder.binding

        binding.textPetName.text = item.category.replaceFirstChar {
            it.uppercase()
        }

        val currencySymbol = if (item.currency == "THB") "฿" else "$"
        binding.textMoney.text = "$currencySymbol${item.amount}"

        binding.textDate.text = item.date

        val iconRes = when (item.category) {

            "food" -> R.drawable.pet_bowl
            "medical" -> R.drawable.vaccine
            "toy" -> R.drawable.toy
            "other" -> R.drawable.other

            else -> R.drawable.other
        }

        binding.iconCategory.setImageResource(iconRes)
    }

    fun submitList(newList: List<ExpenseRecord>) {
        list = newList
        notifyDataSetChanged()
    }
}
