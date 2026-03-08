package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemRecentVaccineBinding
import com.example.petbuddy.model.VaccinationRecord

class RecentVaccineAdapter(
    private val vaccines: List<VaccinationRecord>
) : RecyclerView.Adapter<RecentVaccineAdapter.RecentVaccineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentVaccineViewHolder {
        val binding = ItemRecentVaccineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentVaccineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentVaccineViewHolder, position: Int) {
        holder.bind(vaccines[position])
    }

    override fun getItemCount() = vaccines.size

    inner class RecentVaccineViewHolder(
        private val binding: ItemRecentVaccineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: VaccinationRecord) {
            binding.apply {
                tvVaccineName.text = record.vaccineName
                tvDose.text = "เข็มที่ ${record.dose}"
                tvDate.text = record.dateString
                tvTime.text = record.timeString
                tvPlace.text = record.place ?: "ไม่ระบุสถานที่"
            }
        }
    }
}