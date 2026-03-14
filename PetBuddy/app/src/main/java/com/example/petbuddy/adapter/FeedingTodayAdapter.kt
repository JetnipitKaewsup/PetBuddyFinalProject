package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemFeedingBinding
import com.example.petbuddy.model.FeedingSchedule
import com.example.petbuddy.model.Pet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FeedingTodayAdapter(
    private var feedingList: List<FeedingSchedule>,
    private var petMap: Map<String, Pet>,
    private val onDoneClick: (FeedingSchedule) -> Unit,
    private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<FeedingTodayAdapter.FeedingViewHolder>() {

    fun submitList(newList: List<FeedingSchedule>) {
        feedingList = newList
        notifyDataSetChanged()
    }

    fun updatePetMap(newMap: Map<String, Pet>) {
        petMap = newMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedingViewHolder {

        val binding = ItemFeedingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FeedingViewHolder(binding)
    }

    override fun getItemCount(): Int = feedingList.size

    override fun onBindViewHolder(holder: FeedingViewHolder, position: Int) {

        holder.bind(feedingList[position])
    }

    inner class FeedingViewHolder(
        private val binding: ItemFeedingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val petAdapter = PetIconAdapter { petId ->
            onPetClick(petId)
        }

        init {

            binding.rvPets.layoutManager =
                LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            binding.rvPets.adapter = petAdapter
        }

        fun bind(schedule: FeedingSchedule) {

            // Food name
            binding.tvFoodName.text = schedule.title

            // Food type
            binding.tvFoodType.text = schedule.type

            // Time
            val time = String.format(
                "%02d:%02d",
                schedule.hour,
                schedule.minute
            )
            binding.tvTime.text = time

            // 🔥 Show today's date
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)

            // Pets
            val pets = schedule.petIds.mapNotNull { petMap[it] }
            petAdapter.submitList(pets)

            // Done button
            binding.btnAction.setOnClickListener {
                onDoneClick(schedule)
            }
        }
    }
}