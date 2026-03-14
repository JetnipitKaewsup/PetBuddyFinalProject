package com.example.petbuddy.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentAddWeightBinding
import com.example.petbuddy.model.WeightRecord
import java.text.SimpleDateFormat
import java.util.*

class AddWeightFragment : Fragment() {

    private var _binding: FragmentAddWeightBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity

    private var existingRecord: WeightRecord? = null
    private var selectedTimestamp: Long = System.currentTimeMillis() // เก็บเป็น timestamp

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val ARG_RECORD = "record"

        fun newInstance(existingRecord: WeightRecord? = null): AddWeightFragment {
            return AddWeightFragment().apply {
                arguments = Bundle().apply {
                    if (existingRecord != null) {
                        arguments?.putSerializable(ARG_RECORD, existingRecord)
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        existingRecord = arguments?.getSerializable(ARG_RECORD) as? WeightRecord
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        if (existingRecord != null) {
            loadExistingData()
        } else {
            // ตั้งค่าวันที่เริ่มต้นเป็นวันนี้
            updateDateTimeDisplay(selectedTimestamp)
        }
    }

    private fun setupUI() {
        binding.tvTitle.text = if (existingRecord == null) "Add weight" else "Edit weight"

        binding.layoutDate.setOnClickListener {
            showDatePicker()
        }

        binding.layoutTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSave.setOnClickListener {
            saveWeightRecord()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                // เก็บเวลาเดิมไว้
                val timeCalendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

                selectedTimestamp = calendar.timeInMillis
                updateDateTimeDisplay(selectedTimestamp)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                selectedTimestamp = calendar.timeInMillis
                updateDateTimeDisplay(selectedTimestamp)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeDisplay(timestamp: Long) {
        binding.tvDate.text = dateFormatter.format(Date(timestamp))
        binding.tvTime.text = timeFormatter.format(Date(timestamp))
    }

    private fun loadExistingData() {
        existingRecord?.let { record ->
            binding.etWeight.setText(record.weight.toString())
            selectedTimestamp = record.timestamp
            updateDateTimeDisplay(selectedTimestamp)
            binding.etNote.setText(record.note ?: "")
        }
    }

    private fun saveWeightRecord() {
        val weightStr = binding.etWeight.text.toString()
        if (weightStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter weight", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            Toast.makeText(requireContext(), "No pet information found", Toast.LENGTH_SHORT).show()
            return
        }

        val now = System.currentTimeMillis()
        val record = WeightRecord(
            id = existingRecord?.id ?: UUID.randomUUID().toString(),
            petId = currentPet.petId,
            weight = weight,
            timestamp = selectedTimestamp,
            note = binding.etNote.text.toString().ifEmpty { null },
            createdAt = existingRecord?.createdAt ?: now,
            updatedAt = now
        )

        saveToFirebase(record)
    }

    private fun saveToFirebase(record: WeightRecord) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("weights")
            .document(record.petId)
            .collection("records")
            .document(record.id)
            .set(record)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    if (existingRecord == null) "Weight saved successfully" else "Weight updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}