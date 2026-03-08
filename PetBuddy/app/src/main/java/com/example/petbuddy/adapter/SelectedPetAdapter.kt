package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemSelectedPetBinding
import com.example.petbuddy.model.Pet

class SelectedPetAdapter(
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<SelectedPetAdapter.SelectedPetViewHolder>() {

    private var pets: List<Pet> = emptyList()

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
            binding.apply {
                tvPetName.text = pet.petName
                /*
                tvPetType.text = when (pet.type) {
                    "dog" -> "สุนัข"
                    "cat" -> "แมว"
                    else -> pet.type ?: "ไม่ระบุ"
                }*/

                btnRemove.setOnClickListener {
                    onRemoveClick(pet.petId)
                }
            }
        }
    }
}