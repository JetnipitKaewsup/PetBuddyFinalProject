package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ItemPetInProfileBinding
import com.example.petbuddy.model.Pet

class MyPetsAdapter(
    private val onPetClick: (Pet) -> Unit
) : RecyclerView.Adapter<MyPetsAdapter.MyPetViewHolder>() {

    private var pets: List<Pet> = emptyList()

    fun submitList(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPetViewHolder {

        val binding = ItemPetInProfileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MyPetViewHolder(binding, onPetClick)
    }

    override fun onBindViewHolder(holder: MyPetViewHolder, position: Int) {
        holder.bind(pets[position])
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    class MyPetViewHolder(
        private val binding: ItemPetInProfileBinding,
        private val onPetClick: (Pet) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {

            binding.apply {

                tvPetName.text = pet.petName
                tvPetBreed.text = pet.breed

                if (!pet.imageUrl.isNullOrEmpty()) {

                    Glide.with(root.context)
                        .load(pet.imageUrl)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.pet_placeholder)
                                .error(R.drawable.pet_placeholder)
                                .circleCrop()
                        )
                        .into(ivPet)

                } else {

                    ivPet.setImageResource(R.drawable.pet_placeholder)

                }

                root.setOnClickListener {
                    onPetClick(pet)
                }
            }
        }
    }
}