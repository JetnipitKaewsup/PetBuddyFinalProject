package com.example.petbuddy.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petbuddy.data.Pet
import com.example.petbuddy.R

class PetResultAdapter(
    private var items: MutableList<Pet>,
    private val onItemClick: ((Pet) -> Unit)? = null
) : RecyclerView.Adapter<PetResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_profile, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pet = items[position]

        holder.nameTextView.text = pet.petName
        holder.breedTextView.text = pet.breed

        Glide.with(holder.itemView.context)
            .load(pet.imagePath)
            .centerCrop()
            .into(holder.imageView)

        Log.d("PET_IMAGE_URL", pet.imagePath)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(pet)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updatePets(newPets: List<Pet>) {
        items.clear()
        items.addAll(newPets)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nameTextView: TextView = itemView.findViewById(R.id.namePet)
        val breedTextView: TextView = itemView.findViewById(R.id.breedPet)
        val imageView: ImageView = itemView.findViewById(R.id.imagePet)
    }
}