package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.databinding.ItemTodoBinding
import com.example.petbuddy.databinding.ItemUpcomingBinding
import com.example.petbuddy.model.UpcomingActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoAdapter(
    private var list: MutableList<String>
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.binding.todoTitle.text = item
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: MutableList<String>) {
        list = newList
        notifyDataSetChanged()
    }
}

