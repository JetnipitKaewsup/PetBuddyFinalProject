package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentFeedingAlarmBinding
import com.example.petbuddy.notifications.ReminderManager

class FeedingAlarmFragment : Fragment(R.layout.fragment_feeding_alarm) {

    private var _binding: FragmentFeedingAlarmBinding? = null
    private val binding get() = _binding!!

    private var isAm = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFeedingAlarmBinding.bind(view)

        setupAmPmButtons()
        setupSaveButton()
        loadSelectedPets()
    }

    private fun setupAmPmButtons() {

        binding.btnAm.setOnClickListener {
            isAm = true
            binding.btnAm.isEnabled = false
            binding.btnPm.isEnabled = true
        }

        binding.btnPm.setOnClickListener {
            isAm = false
            binding.btnAm.isEnabled = true
            binding.btnPm.isEnabled = false
        }
    }

    private fun setupSaveButton() {

        binding.btnSave.setOnClickListener {

            val hourText = binding.etHour.text.toString()
            val minuteText = binding.etMinute.text.toString()

            if (hourText.isEmpty() || minuteText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var hour = hourText.toInt()
            val minute = minuteText.toInt()

            if (isAm) {
                if (hour == 12) hour = 0
            } else {
                if (hour != 12) hour += 12
            }

            scheduleAlarm(hour, minute)
        }
    }

    private fun scheduleAlarm(hour: Int, minute: Int) {

        val baseActivity = activity as BaseActivity
        val pets = baseActivity.selectedPets

        if (pets.isEmpty()) {
            Toast.makeText(requireContext(), "No pet selected", Toast.LENGTH_SHORT).show()
            return
        }

        pets.forEach { pet ->

            ReminderManager.scheduleFeedingReminder(
                requireContext(),
                pet.petId,
                pet.petName,
                hour,
                minute
            )
        }

        Toast.makeText(requireContext(), "Feeding reminder set", Toast.LENGTH_SHORT).show()
    }

    private fun loadSelectedPets() {

        val baseActivity = activity as BaseActivity
        val pets = baseActivity.selectedPets

        if (pets.isEmpty()) return

        println("Selected pets: ${pets.size}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}