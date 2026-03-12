package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemFeedingRecordBinding
import com.example.petbuddy.model.FeedingRecord
import com.example.petbuddy.model.Pet

class FeedingRecordAdapter(
    private var petMap: Map<String, Pet>,
    private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<FeedingRecordAdapter.RecordViewHolder>() {

    private var records: List<FeedingRecord> = emptyList()

    fun submitList(newList: List<FeedingRecord>) {
        records = newList
        notifyDataSetChanged()
    }

    fun updatePetMap(newMap: Map<String, Pet>) {
        petMap = newMap
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {

        val binding = ItemFeedingRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    inner class RecordViewHolder(
        private val binding: ItemFeedingRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: FeedingRecord) {

            binding.tvFoodName.text = record.foodName
            binding.tvFoodType.text = "(${record.foodType})"
            binding.tvDateTime.text = record.dateTime

            val pets = record.petIds.mapNotNull { petMap[it] }

            val petAdapter = PetIconAdapter(onPetClick)

            binding.rvPets.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)

            binding.rvPets.adapter = petAdapter

            petAdapter.submitList(pets)
        }
    }
}