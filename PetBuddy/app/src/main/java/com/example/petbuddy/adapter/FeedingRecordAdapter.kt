package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemFeedingRecordBinding
import com.example.petbuddy.model.FeedingRecord
import com.example.petbuddy.model.Pet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedingRecordAdapter(
    private var petMap: Map<String, Pet>,
    private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<FeedingRecordAdapter.ViewHolder>() {

    private var recordList: List<FeedingRecord> = emptyList()

    private val formatter =
        SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())

    fun submitList(newList: List<FeedingRecord>) {

        recordList = newList

        notifyDataSetChanged()
    }

    fun updatePetMap(newMap: Map<String, Pet>) {

        petMap = newMap

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemFeedingRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = recordList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(recordList[position])
    }

    inner class ViewHolder(
        private val binding: ItemFeedingRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: FeedingRecord) {

            binding.tvFoodName.text = record.foodName

            binding.tvFoodType.text = "(${record.foodType})"

            binding.tvDateTime.text =
                formatter.format(Date(record.fedAt))

            val pets = record.petIds.mapNotNull { petMap[it] }

            val petAdapter = PetIconAdapter { petId ->
                onPetClick(petId)
            }

            binding.rvPets.layoutManager =
                LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            binding.rvPets.adapter = petAdapter

            petAdapter.submitList(pets)
        }
    }
}