package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petbuddy.databinding.ItemPetBinding
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.R

class PetAdapter(
    private val mode: SelectionMode,
    private val onItemClick: (Pet) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    private var pets = listOf<Pet>()
    private val selectedPets = mutableSetOf<Pet>()

    fun submitList(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }

    fun getSelectedPets(): List<Pet> {
        return selectedPets.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(pets[position])
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    fun setInitialSelection(petIds: List<String>) {
        selectedPets.clear()
        selectedPets.addAll(pets.filter { it.petId in petIds })
        notifyDataSetChanged()
        onSelectionChanged(selectedPets.size)
    }

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            binding.apply {

                tvPetName.text = pet.petName
                tvPetBreed.text = "${pet.petType} • ${pet.breed}"

                Glide.with(ivPet.context)
                    .load(pet.imagePath)
                    .placeholder(R.drawable.pet_placeholder)
                    .error(R.drawable.pet_placeholder)
                    .circleCrop()
                    .into(ivPet)

                when (mode) {

                    SelectionMode.SINGLE -> {

                        checkBox.visibility = View.GONE

                        radioButton.isChecked = selectedPets.contains(pet)

                        root.setOnClickListener {
                            selectedPets.clear()
                            selectedPets.add(pet)
                            notifyDataSetChanged()
                            onItemClick(pet)
                        }
                    }

                    SelectionMode.MULTIPLE -> {

                        checkBox.visibility = View.VISIBLE

                        checkBox.isChecked = selectedPets.contains(pet)

                        root.setOnClickListener {

                            if (selectedPets.contains(pet)) {
                                selectedPets.remove(pet)
                                checkBox.isChecked = false
                            } else {
                                selectedPets.add(pet)
                                checkBox.isChecked = true
                            }

                            onSelectionChanged(selectedPets.size)
                        }

                        checkBox.setOnCheckedChangeListener { _, isChecked ->

                            if (isChecked) {
                                selectedPets.add(pet)
                            } else {
                                selectedPets.remove(pet)
                            }

                            onSelectionChanged(selectedPets.size)
                        }
                    }
                }
            }
        }
    }
}