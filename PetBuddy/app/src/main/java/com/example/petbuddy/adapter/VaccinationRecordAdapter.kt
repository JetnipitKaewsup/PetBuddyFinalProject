package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemVaccinationRecordBinding
import com.example.petbuddy.model.VaccinationRecord

class VaccinationRecordAdapter(
    private val onItemClick: (VaccinationRecord) -> Unit,
    private val onItemLongClick: (VaccinationRecord) -> Unit
) : ListAdapter<VaccinationRecord, VaccinationRecordAdapter.VaccinationRecordViewHolder>(VaccinationRecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccinationRecordViewHolder {
        val binding = ItemVaccinationRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VaccinationRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaccinationRecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VaccinationRecordViewHolder(
        private val binding: ItemVaccinationRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: VaccinationRecord) {
            binding.apply {
                // ข้อมูลพื้นฐาน
                tvVaccineName.text = record.vaccineName
                tvDose.text = "Dose ${record.dose}"
                tvDate.text = record.dateString
                tvTime.text = record.timeString

                // สถานที่ (ถ้ามี)
                if (!record.place.isNullOrEmpty()) {
                    tvPlace.text = record.place
                    tvPlace.visibility = android.view.View.VISIBLE
                } else {
                    tvPlace.visibility = android.view.View.GONE
                }

                // ข้อมูลเข็มถัดไป (ถ้ามี)
                if (record.nextDueDate != null) {
                    layoutNextVaccine.visibility = android.view.View.VISIBLE
                    tvNextVaccineName.text = record.nextVaccineName ?: record.vaccineName
                    tvNextDose.text = "Dose ${record.nextDose ?: record.dose + 1}"
                    tvNextDate.text = record.nextDueDateString ?: ""

                    // แสดงจำนวนวันที่เหลือ
                    record.daysUntilNext?.let { days ->
                        when {
                            days < 0 -> {
                                tvDaysLeft.text = " ${-days} days overdue"
                                tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#F44336"))
                            }
                            days == 0 -> {
                                tvDaysLeft.text = "today"
                                tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                            }
                            days <= 7 -> {
                                tvDaysLeft.text = "in $days days"
                                tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                            }
                            else -> {
                                tvDaysLeft.text = "in $days days"
                                tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                            }
                        }
                        tvDaysLeft.visibility = android.view.View.VISIBLE
                    }
                } else {
                    layoutNextVaccine.visibility = android.view.View.GONE
                }

                // จัดการการคลิก
                root.setOnClickListener {
                    onItemClick(record)
                }

                root.setOnLongClickListener {
                    onItemLongClick(record)
                    true
                }
            }
        }
    }

    class VaccinationRecordDiffCallback : DiffUtil.ItemCallback<VaccinationRecord>() {
        override fun areItemsTheSame(oldItem: VaccinationRecord, newItem: VaccinationRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VaccinationRecord, newItem: VaccinationRecord): Boolean {
            return oldItem == newItem
        }
    }
}