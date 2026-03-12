package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemFeedingSettingBinding
import com.example.petbuddy.model.FeedingSchedule
import com.example.petbuddy.model.Pet
import java.text.SimpleDateFormat
import java.util.Locale

class FeedingSettingAdapter(
    private var feedingList: List<FeedingSchedule>,
    private var petMap: Map<String, Pet>,
    private val onEditClick: (FeedingSchedule) -> Unit,
    private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<FeedingSettingAdapter.ViewHolder>() {

    fun submitList(newList: List<FeedingSchedule>) {
        feedingList = newList
        notifyDataSetChanged()
    }

    fun updatePetMap(newMap: Map<String, Pet>) {
        petMap = newMap
    }

    fun getItem(position: Int): FeedingSchedule {
        return feedingList[position]
    }

    fun removeItem(position: Int) {
        val mutableList = feedingList.toMutableList()
        mutableList.removeAt(position)
        feedingList = mutableList
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemFeedingSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = feedingList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(feedingList[position])
    }

    inner class ViewHolder(
        private val binding: ItemFeedingSettingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: FeedingSchedule) {

            binding.tvFoodName.text = schedule.title
            binding.tvFoodType.text = schedule.type

            val time = String.format("%02d:%02d", schedule.hour, schedule.minute)
            binding.tvTime.text = time

            schedule.createdAt?.toDate()?.let {

                val formatter = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )

                binding.tvDate.text = formatter.format(it)
            }

            val pets = schedule.petIds.mapNotNull { petMap[it] }

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

            binding.ivEdit.setOnClickListener {
                onEditClick(schedule)
            }
        }
    }
}