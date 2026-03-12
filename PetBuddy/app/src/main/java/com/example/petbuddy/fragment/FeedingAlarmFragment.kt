package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.PetIconAdapter
import com.example.petbuddy.adapter.SelectedPetAdapter
import com.example.petbuddy.databinding.FragmentFeedingAlarmBinding
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.notifications.ReminderManager
import com.example.petbuddy.util.Constants
import com.google.firebase.firestore.FieldValue

class FeedingAlarmFragment : Fragment(R.layout.fragment_feeding_alarm) {

    private var _binding: FragmentFeedingAlarmBinding? = null
    private val binding get() = _binding!!

    private val baseActivity get() = requireActivity() as BaseActivity

    private var repeatType: String = "once"
    private val selectedDays = mutableListOf<String>()

    private var isAm = true

    private lateinit var petAdapter: PetIconAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFeedingAlarmBinding.bind(view)

        setupAmPmButtons()
        setupRepeatButtons()
        setupDayChips()
        setupSpinner()
        setupPetSelection()
        setupPetRecycler()
        setupSaveButton()

        loadSelectedPets()
    }

    override fun onResume() {
        super.onResume()

        if (::petAdapter.isInitialized) {
            petAdapter.submitList(baseActivity.selectedPets)
        }
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

    private fun setupRepeatButtons() {

        binding.btnOnce.setOnClickListener {

            repeatType = "once"
            binding.chipDays.visibility = View.GONE
            selectedDays.clear()
            clearDayChips()
        }

        binding.btnEveryday.setOnClickListener {

            repeatType = "everyday"
            binding.chipDays.visibility = View.GONE

            selectedDays.clear()
            selectedDays.addAll(
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
            )

            clearDayChips()
        }

        binding.btnCustom.setOnClickListener {

            repeatType = "custom"
            binding.chipDays.visibility = View.VISIBLE

            selectedDays.clear()
            clearDayChips()
        }
    }

    private fun setupDayChips() {

        binding.chipMon.setOnCheckedChangeListener { _, checked ->
            updateDay("Mon", checked)
        }

        binding.chipTue.setOnCheckedChangeListener { _, checked ->
            updateDay("Tue", checked)
        }

        binding.chipWed.setOnCheckedChangeListener { _, checked ->
            updateDay("Wed", checked)
        }

        binding.chipThu.setOnCheckedChangeListener { _, checked ->
            updateDay("Thu", checked)
        }

        binding.chipFri.setOnCheckedChangeListener { _, checked ->
            updateDay("Fri", checked)
        }

        binding.chipSat.setOnCheckedChangeListener { _, checked ->
            updateDay("Sat", checked)
        }

        binding.chipSun.setOnCheckedChangeListener { _, checked ->
            updateDay("Sun", checked)
        }
    }

    private fun updateDay(day: String, checked: Boolean) {

        if (checked) {

            if (!selectedDays.contains(day)) {
                selectedDays.add(day)
            }

        } else {

            selectedDays.remove(day)

        }
    }

    private fun clearDayChips() {

        binding.chipMon.isChecked = false
        binding.chipTue.isChecked = false
        binding.chipWed.isChecked = false
        binding.chipThu.isChecked = false
        binding.chipFri.isChecked = false
        binding.chipSat.isChecked = false
        binding.chipSun.isChecked = false
    }

    private fun setupSpinner() {

        val types = listOf(
            "Dry Food",
            "Wet Food",
            "Snack",
            "Medicine"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        binding.spinnerType.adapter = adapter
    }

    private fun setupPetSelection() {

        binding.petSection.setOnClickListener {

            val fragment = PetSelectionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("mode", SelectionMode.MULTIPLE)
                    putString("source_tag", Constants.TAG_FEEDING_ALARM)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(Constants.TAG_FEEDING_ALARM)   // ต้องมี
                .commit()
        }
    }

    private fun setupPetRecycler() {

        petAdapter = PetIconAdapter { petId ->

            baseActivity.removePetFromSelection(petId)

            petAdapter.submitList(baseActivity.selectedPets)

        }

        binding.petRow.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.petRow.adapter = petAdapter

        petAdapter.submitList(baseActivity.selectedPets)
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

            if (hour !in 1..12 || minute !in 0..59) {

                Toast.makeText(requireContext(), "Invalid time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }

            if (isAm) {

                if (hour == 12) hour = 0

            } else {

                if (hour != 12) hour += 12

            }

            val title = binding.etTitle.text.toString()
            val note = binding.etNote.text.toString()
            val type = binding.spinnerType.selectedItem?.toString() ?: ""

            val userId = baseActivity.getCurrentUserIdSafe()

            if (userId == null) {

                baseActivity.showToast("User not logged in")
                return@setOnClickListener

            }

            val petIds = baseActivity.getSelectedPetIds()

            if (petIds.isEmpty()) {

                baseActivity.showToast("Please select pet")
                return@setOnClickListener

            }

            val scheduleData = hashMapOf(

                "title" to title,
                "note" to note,
                "type" to type,
                "hour" to hour,
                "minute" to minute,
                "repeatType" to repeatType,
                "days" to selectedDays,
                "petIds" to petIds,
                "isActive" to true,
                "createdAt" to FieldValue.serverTimestamp()

            )

            baseActivity.db
                .collection("users")
                .document(userId)
                .collection("feeding_schedules")
                .add(scheduleData)
                .addOnSuccessListener {

                    scheduleAlarm(hour, minute)

                    baseActivity.showToast("Feeding alarm saved")

                    parentFragmentManager.popBackStack()

                }
                .addOnFailureListener {

                    baseActivity.showToast("Failed to save schedule")

                }
        }
    }


    private fun scheduleAlarm(hour: Int, minute: Int) {

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

        val pets = baseActivity.selectedPets

        if (pets.isEmpty()) return

        println("Selected pets: ${pets.size}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}