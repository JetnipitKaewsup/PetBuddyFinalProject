package com.example.petbuddy.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.PetIconAdapter
import com.example.petbuddy.databinding.FragmentFeedingAlarmBinding
import com.example.petbuddy.model.FeedingSchedule
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.notifications.ReminderManager
import com.example.petbuddy.util.Constants
import com.google.firebase.firestore.FieldValue
import java.util.Calendar

class FeedingAlarmFragment : Fragment(R.layout.fragment_feeding_alarm) {

    private var _binding: FragmentFeedingAlarmBinding? = null
    private val binding get() = _binding!!

    private val baseActivity get() = requireActivity() as BaseActivity

    private var repeatType: String = "once"
    private val selectedDays = mutableListOf<String>()

    private var schedule: FeedingSchedule? = null

    private lateinit var petAdapter: PetIconAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFeedingAlarmBinding.bind(view)

        schedule = arguments?.getParcelable("schedule")

        if (schedule == null) {
            setDefaultTime()
        }

        setupTimePicker()
        setupRepeatButtons()
        setupDayChips()
        setupSpinner()
        setupPetSelection()
        setupPetRecycler()
        setupSaveButton()

        loadSelectedPets()

        schedule?.let {
            restoreSchedule()
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (schedule == null) {
            repeatType = "once"
            selectRepeatButton(binding.btnOnce)
        }
    }

    override fun onResume() {
        super.onResume()

        if (::petAdapter.isInitialized) {
            petAdapter.submitList(baseActivity.selectedPets)
        }
    }

    private fun setDefaultTime() {

        val calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        binding.tvTime.text = String.format("%02d:%02d", hour, minute)
    }

    private fun setupTimePicker() {

        binding.tvTime.setOnClickListener {

            val calendar = Calendar.getInstance()

            val dialog = TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->

                    binding.tvTime.text =
                        String.format("%02d:%02d", selectedHour, selectedMinute)

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

            dialog.show()
        }
    }

    private fun setupRepeatButtons() {

        binding.btnOnce.setOnClickListener {

            repeatType = "once"

            selectRepeatButton(binding.btnOnce)

            binding.chipDays.visibility = View.GONE

            selectedDays.clear()
            clearDayChips()
        }

        binding.btnEveryday.setOnClickListener {

            repeatType = "everyday"

            selectRepeatButton(binding.btnEveryday)

            binding.chipDays.visibility = View.GONE

            selectedDays.clear()

            selectedDays.addAll(
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
            )

            clearDayChips()
        }

        binding.btnCustom.setOnClickListener {

            repeatType = "custom"

            selectRepeatButton(binding.btnCustom)

            binding.chipDays.visibility = View.VISIBLE

            selectedDays.clear()
            clearDayChips()
        }
    }

    private fun setupDayChips() {

        binding.chipMon.setOnCheckedChangeListener { _, checked -> updateDay("Mon", checked) }
        binding.chipTue.setOnCheckedChangeListener { _, checked -> updateDay("Tue", checked) }
        binding.chipWed.setOnCheckedChangeListener { _, checked -> updateDay("Wed", checked) }
        binding.chipThu.setOnCheckedChangeListener { _, checked -> updateDay("Thu", checked) }
        binding.chipFri.setOnCheckedChangeListener { _, checked -> updateDay("Fri", checked) }
        binding.chipSat.setOnCheckedChangeListener { _, checked -> updateDay("Sat", checked) }
        binding.chipSun.setOnCheckedChangeListener { _, checked -> updateDay("Sun", checked) }
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

    private fun restoreSchedule() {

        val s = schedule ?: return

        binding.etTitle.setText(s.title)
        binding.etNote.setText(s.note)

        binding.tvTime.text =
            String.format("%02d:%02d", s.hour, s.minute)

        val adapter = binding.spinnerType.adapter as ArrayAdapter<String>

        val index = adapter.getPosition(s.type)

        if (index >= 0) {
            binding.spinnerType.setSelection(index)
        }

        repeatType = s.repeatType

        when (repeatType) {

            "once" -> selectRepeatButton(binding.btnOnce)

            "everyday" -> selectRepeatButton(binding.btnEveryday)

            "custom" -> {
                selectRepeatButton(binding.btnCustom)
                binding.chipDays.visibility = View.VISIBLE
            }
        }

        selectedDays.clear()
        selectedDays.addAll(s.days)

        binding.chipMon.isChecked = "Mon" in s.days
        binding.chipTue.isChecked = "Tue" in s.days
        binding.chipWed.isChecked = "Wed" in s.days
        binding.chipThu.isChecked = "Thu" in s.days
        binding.chipFri.isChecked = "Fri" in s.days
        binding.chipSat.isChecked = "Sat" in s.days
        binding.chipSun.isChecked = "Sun" in s.days
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
                .addToBackStack(Constants.TAG_FEEDING_ALARM)
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

            val timeParts = binding.tvTime.text.toString().split(":")

            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val title = binding.etTitle.text.toString()
            val note = binding.etNote.text.toString()
            val type = binding.spinnerType.selectedItem?.toString() ?: ""

            val userId = baseActivity.getCurrentUserIdSafe()
                ?: return@setOnClickListener

            val petIds = baseActivity.getSelectedPetIds()

            if (petIds.isEmpty()) {

                baseActivity.showToast("Please select pet")
                return@setOnClickListener
            }

            if (repeatType == "custom" && selectedDays.isEmpty()) {

                baseActivity.showToast("Please select days")
                return@setOnClickListener
            }

            if (repeatType == "everyday") {

                selectedDays.clear()

                selectedDays.addAll(
                    listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
                )
            }

            val collection = baseActivity.db
                .collection("users")
                .document(userId)
                .collection("feeding_schedules")

            val data = hashMapOf(

                "title" to title,
                "note" to note,
                "type" to type,
                "hour" to hour,
                "minute" to minute,
                "repeatType" to repeatType,
                "days" to selectedDays.toList(),
                "petIds" to petIds,
                "isActive" to true
            )

            if (schedule == null) {

                val doc = collection.document()

                data["id"] = doc.id
                data["createdAt"] = FieldValue.serverTimestamp()

                doc.set(data)

                    .addOnSuccessListener {

                        scheduleAlarm(hour, minute)

                        baseActivity.showToast("Schedule saved")

                        parentFragmentManager.popBackStack()

                    }

                    .addOnFailureListener {

                        baseActivity.showToast("Failed to save schedule")

                    }

            } else {

                collection.document(schedule!!.id)

                    .update(data as Map<String, Any>)

                    .addOnSuccessListener {

                        scheduleAlarm(hour, minute)

                        baseActivity.showToast("Schedule updated")

                        parentFragmentManager.popBackStack()

                    }

                    .addOnFailureListener {

                        baseActivity.showToast("Failed to update schedule")

                    }
            }
        }
    }

    private fun scheduleAlarm(hour: Int, minute: Int) {

        val pets = baseActivity.selectedPets

        if (pets.isEmpty()) return

        pets.forEach { pet ->

            ReminderManager.cancelFeedingReminder(
                requireContext(),
                pet.petId,
                hour,
                minute,
                selectedDays
            )

            ReminderManager.scheduleFeedingReminder(
                requireContext(),
                pet.petId,
                pet.petName,
                hour,
                minute,
                repeatType,
                selectedDays.toList()
            )
        }
    }

    private fun loadSelectedPets() {

        val pets = baseActivity.selectedPets

        if (pets.isEmpty()) return

        println("Selected pets: ${pets.size}")
    }

    private fun selectRepeatButton(selected: View) {

        val buttons = listOf(
            binding.btnOnce,
            binding.btnEveryday,
            binding.btnCustom
        )

        buttons.forEach {
            it.isSelected = false
        }

        selected.isSelected = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}