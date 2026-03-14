package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemUpcomingBinding
import com.example.petbuddy.model.UpcomingActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpcomingAdapter(
    private var list: List<UpcomingActivity>
) : RecyclerView.Adapter<UpcomingAdapter.ViewHolder>() {

    private val dayFormatter = SimpleDateFormat("EE", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("dd", Locale.ENGLISH)
    private val timeFormatter = SimpleDateFormat("H:mm", Locale.ENGLISH)

    fun submitList(newList: List<UpcomingActivity>) {
        list = newList.sortedBy { it.time }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemUpcomingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(
        private val binding: ItemUpcomingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: UpcomingActivity) {

            val date = Date(activity.time)

            binding.tvTitle.text = activity.title

            binding.tvDay.text = dayFormatter.format(date)
            binding.tvDate.text = dateFormatter.format(date)

            binding.tvTime.text =
                "Time ${timeFormatter.format(date)}"
        }
    }
}