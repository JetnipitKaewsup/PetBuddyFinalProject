package com.example.petbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.R
import com.example.petbuddy.model.VaccinationRecord

class RecentVaccineAdapter(
    private val vaccines: List<VaccinationRecord>
) : RecyclerView.Adapter<RecentVaccineAdapter.RecentVaccineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentVaccineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_vaccine, parent, false)
        return RecentVaccineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentVaccineViewHolder, position: Int) {
        holder.bind(vaccines[position])
    }

    override fun getItemCount() = vaccines.size

    class RecentVaccineViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        private val tvDose: TextView = itemView.findViewById(R.id.tvDose)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(record: VaccinationRecord) {
            tvVaccineName.text = record.vaccineName
            tvDose.text = "Dose ${record.dose}"
            tvDate.text = record.dateString
        }
    }
}