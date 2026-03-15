package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.R
import com.example.petbuddy.databinding.ItemEventBinding
import com.example.petbuddy.model.Event
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    // Formatters
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                // ชื่อ event
                tvEventTitle.text = event.title

                // เวลา
                tvEventTime.text = if (event.isAllDay) {
                    "All Day"
                } else {
                    timeFormatter.format(event.startDate.toDate())
                }

                // สถานที่ (ถ้ามี)
                if (!event.place.isNullOrEmpty()) {
                    tvEventPlace.visibility = android.view.View.VISIBLE
                    tvEventPlace.text = event.place
                } else {
                    tvEventPlace.visibility = android.view.View.GONE
                }

                // จำนวนสัตว์เลี้ยงที่เกี่ยวข้อง
                if (event.petIds.isNotEmpty()) {
                    tvPetCount.visibility = android.view.View.VISIBLE
                    tvPetCount.text = "${event.petIds.size} pet${if (event.petIds.size > 1) "s" else ""}"
                } else {
                    tvPetCount.visibility = android.view.View.GONE
                }

                // Tag/Category (เปลี่ยนสีตามประเภท)
                setTagStyle(event.tag)

                // คลิกที่รายการ
                root.setOnClickListener {
                    onEventClick(event)
                }
            }
        }

        private fun setTagStyle(tag: String?) {
            val tagView = binding.tvEventTag
            when (tag) {
                "Vet Visit" -> {
                    tagView.text = "🏥 Vet"
                    //tagView.setBackgroundResource(R.drawable.bg_tag_vet)
                }
                "Grooming" -> {
                    tagView.text = "✂️ Grooming"
                    //tagView.setBackgroundResource(R.drawable.bg_tag_grooming)
                }
                "Medication" -> {
                    tagView.text = "💊 Med"
                    //tagView.setBackgroundResource(R.drawable.bg_tag_med)
                }
                "Play Date" -> {
                    tagView.text = "🎮 Play"
                    //tagView.setBackgroundResource(R.drawable.bg_tag_play)
                }
                "Training" -> {
                    tagView.text = "🎯 Train"
                    //tagView.setBackgroundResource(R.drawable.bg_tag_training)
                }
                else -> {
                    tagView.text = "📅 General"
                    //tagView.setBackgroundResource(R.drawable.bg_tag_general)
                }
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}