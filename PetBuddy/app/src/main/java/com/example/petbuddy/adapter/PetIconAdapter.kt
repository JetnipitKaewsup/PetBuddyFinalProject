package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ItemPetIconBinding
import com.example.petbuddy.model.Pet

class PetIconAdapter(
    private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<PetIconAdapter.PetViewHolder>() {

    private var pets: List<Pet> = emptyList()

    fun submitList(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {

        val binding = ItemPetIconBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(pets[position])
    }

    override fun getItemCount() = pets.size

    inner class PetViewHolder(
        private val binding: ItemPetIconBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {

            Glide.with(binding.root.context)
                .load(pet.imageUrl)
                .placeholder(R.drawable.pet_placeholder)
                .error(R.drawable.pet_placeholder)
                .circleCrop()
                .into(binding.imgPet)

            binding.root.setOnClickListener {
                onPetClick(pet.petId)
            }
        }
    }
}