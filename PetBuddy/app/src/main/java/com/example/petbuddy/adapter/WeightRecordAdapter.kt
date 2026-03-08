package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemWeightRecordBinding
import com.example.petbuddy.model.WeightRecord

class WeightRecordAdapter(
    private val onItemClick: (WeightRecord) -> Unit,
    private val onItemLongClick: (WeightRecord) -> Unit
) : ListAdapter<WeightRecord, WeightRecordAdapter.WeightRecordViewHolder>(WeightRecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightRecordViewHolder {
        val binding = ItemWeightRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeightRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeightRecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WeightRecordViewHolder(
        private val binding: ItemWeightRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: WeightRecord) {
            binding.apply {
                // ใช้ computed properties จาก WeightRecord โดยตรง
                txtWeightDate.text = record.dateString  // ใช้ dateString จาก model
                txtWeightValue.text = String.format("%.1f", record.weight)
                txtWeightTime.text = record.timeString  // ใช้ timeString จาก model (HH:mm)

                // แสดงหมายเหตุ (ถ้ามี)
                if (!record.note.isNullOrEmpty()) {
                    txtNote.visibility = android.view.View.VISIBLE
                    txtNote.text = record.note
                } else {
                    txtNote.visibility = android.view.View.GONE
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

    class WeightRecordDiffCallback : DiffUtil.ItemCallback<WeightRecord>() {
        override fun areItemsTheSame(oldItem: WeightRecord, newItem: WeightRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeightRecord, newItem: WeightRecord): Boolean {
            return oldItem == newItem
        }
    }
}