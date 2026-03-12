package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.R
import com.example.petbuddy.model.Pet

class PetPreviewAdapter(
    private val pets: List<Pet>,
    private val onRemoveClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetPreviewAdapter.PetPreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetPreviewViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(80, 80)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(4, 4, 4, 4)
        }
        return PetPreviewViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PetPreviewViewHolder, position: Int) {
        val pet = pets[position]
        holder.bind(pet)
    }

    override fun getItemCount() = pets.size

    inner class PetPreviewViewHolder(private val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        fun bind(pet: Pet) {
            // โหลดรูปสัตว์เลี้ยง
            if (!pet.imagePath.isNullOrEmpty()) {
                Glide.with(imageView.context)
                    .load(pet.imagePath)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.pet_placeholder)
                            .error(R.drawable.pet_placeholder)
                            .circleCrop()
                    )
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.pet_placeholder)
            }

            // คลิกเพื่อลบออก
            imageView.setOnClickListener {
                onRemoveClick(pet)
            }
        }
    }
}