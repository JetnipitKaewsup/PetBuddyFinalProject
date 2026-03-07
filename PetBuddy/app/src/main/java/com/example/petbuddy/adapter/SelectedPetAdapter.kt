package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petbuddy.databinding.ItemSelectedPetBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.R

class SelectedPetAdapter(
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<SelectedPetAdapter.SelectedPetViewHolder>() {

    private var pets = listOf<Pet>()

    fun submitList(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedPetViewHolder {
        val binding = ItemSelectedPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectedPetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectedPetViewHolder, position: Int) {
        holder.bind(pets[position])
    }

    override fun getItemCount() = pets.size

    inner class SelectedPetViewHolder(
        private val binding: ItemSelectedPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            binding.tvPetName.text = pet.name
            binding.tvPetBreed.text = pet.breed

            Glide.with(binding.ivPet.context)
                .load(pet.imageUrl)
                .placeholder(R.drawable.pet_placeholder)
                .error(R.drawable.pet_placeholder)
                .circleCrop()
                .into(binding.ivPet)

            binding.btnRemove.setOnClickListener {
                onRemoveClick(pet.id)
            }
        }
    }
}