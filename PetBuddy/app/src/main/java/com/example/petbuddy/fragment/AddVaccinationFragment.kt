package com.example.petbuddy.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentAddVaccinationBinding
import com.example.petbuddy.model.Event
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.VaccinationRecord
import com.example.petbuddy.model.VaccineData
import com.example.petbuddy.model.VaccineInfo
import com.example.petbuddy.navigation.MainNavigator
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class AddVaccinationFragment : Fragment() {

    private var _binding: FragmentAddVaccinationBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator
    private var existingRecord: VaccinationRecord? = null
    private var currentPet: Pet? = null

    // Timestamps
    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var nextSelectedTimestamp: Long? = null

    // Vaccine lists
    private lateinit var vaccineList: List<VaccineInfo>
    private lateinit var vaccineNames: List<String>

    // Dose options
    private val doseOptions = (1..10).map { it.toString() }

    // Formatters
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    // State
    private var isNextVaccineVisible = false

    companion object {
        private const val ARG_RECORD = "record"

        fun newInstance(existingRecord: VaccinationRecord? = null): AddVaccinationFragment {
            return AddVaccinationFragment().apply {
                arguments = Bundle().apply {
                    if (existingRecord != null) {
                        putSerializable(ARG_RECORD, existingRecord)
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        existingRecord = arguments?.getSerializable(ARG_RECORD) as? VaccinationRecord
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVaccinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            Toast.makeText(requireContext(), "Please select a pet first", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        setupVaccineList()
        setupSpinners()
        setupDatePickers()
        setupClickListeners()

        if (existingRecord != null) {
            loadExistingData()
            setupNextVaccineVisibility(existingRecord?.nextDueDate != null)
        } else {
            updateDateTimeDisplay(selectedTimestamp)
            setupNextVaccineVisibility(false)
        }
    }

    private fun setupVaccineList() {
        vaccineList = when (currentPet?.petType?.lowercase()) {
            "dog" -> VaccineData.getAllVaccinesByPetType("dog")
            "cat" -> VaccineData.getAllVaccinesByPetType("cat")
            else -> emptyList()
        }
        vaccineNames = vaccineList.map { it.name }
    }

    private fun setupSpinners() {
        // Vaccine Name Spinner
        val vaccineAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vaccineNames)
        vaccineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVaccineName.adapter = vaccineAdapter

        // Dose Spinner
        val doseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, doseOptions)
        doseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDose.adapter = doseAdapter

        // Next Vaccine Name Spinner
        binding.spinnerNextVaccineName.adapter = vaccineAdapter

        // Next Dose Spinner
        binding.spinnerNextDose.adapter = doseAdapter
    }

    private fun setupDatePickers() {
        binding.btnVaccineDatePicker.setOnClickListener {
            showDatePicker { timestamp ->
                selectedTimestamp = timestamp
                updateDateTimeDisplay(selectedTimestamp)
            }
        }

        binding.btnVaccineTimePicker.setOnClickListener {
            showTimePicker(selectedTimestamp) { timestamp ->
                selectedTimestamp = timestamp
                updateDateTimeDisplay(selectedTimestamp)
            }
        }

        binding.btnNextVaccineDatePicker.setOnClickListener {
            showDatePicker { timestamp ->
                nextSelectedTimestamp = timestamp
                updateNextDateTimeDisplay()
            }
        }

        binding.btnNextVaccineTimePicker.setOnClickListener {
            if (nextSelectedTimestamp != null) {
                showTimePicker(nextSelectedTimestamp!!) { newTimestamp ->
                    nextSelectedTimestamp = newTimestamp
                    updateNextDateTimeDisplay()
                }
            } else {
                nextSelectedTimestamp = System.currentTimeMillis()
                showTimePicker(nextSelectedTimestamp!!) { newTimestamp ->
                    nextSelectedTimestamp = newTimestamp
                    updateNextDateTimeDisplay()
                }
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedTimestamp

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, dayOfMonth)

                val timeCalendar = Calendar.getInstance()
                timeCalendar.timeInMillis = selectedTimestamp

                newCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                newCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                newCalendar.set(Calendar.SECOND, 0)
                newCalendar.set(Calendar.MILLISECOND, 0)

                onDateSelected(newCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(currentTimestamp: Long, onTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimestamp

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onTimeSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeDisplay(timestamp: Long) {
        binding.edtVaccineDate.setText(dateFormatter.format(Date(timestamp)))
        binding.edtVaccineTime.setText(timeFormatter.format(Date(timestamp)))
    }

    private fun updateNextDateTimeDisplay() {
        nextSelectedTimestamp?.let { timestamp ->
            binding.edtNextVaccineDate.setText(dateFormatter.format(Date(timestamp)))
            binding.edtNextVaccineTime.setText(timeFormatter.format(Date(timestamp)))
        }
    }

    private fun setupClickListeners() {
        binding.btnNextVaccine.setOnClickListener {
            setupNextVaccineVisibility(!isNextVaccineVisible)
        }

        binding.btnSave.setOnClickListener {
            saveVaccinationRecord()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupNextVaccineVisibility(visible: Boolean) {
        isNextVaccineVisible = visible
        binding.layoutNextVaccine.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnNextVaccine.alpha = if (visible) 0.5f else 1.0f
    }

    private fun loadExistingData() {
        existingRecord?.let { record ->
            val vaccinePosition = vaccineNames.indexOf(record.vaccineName)
            if (vaccinePosition >= 0) {
                binding.spinnerVaccineName.setSelection(vaccinePosition)
            }

            binding.spinnerDose.setSelection(record.dose - 1)

            selectedTimestamp = record.timestamp
            updateDateTimeDisplay(selectedTimestamp)

            binding.edtVaccinePlace.setText(record.place ?: "")

            if (record.nextDueDate != null) {
                nextSelectedTimestamp = record.nextDueDate
                updateNextDateTimeDisplay()

                record.nextVaccineName?.let { name ->
                    val nextPos = vaccineNames.indexOf(name)
                    if (nextPos >= 0) {
                        binding.spinnerNextVaccineName.setSelection(nextPos)
                    }
                }

                record.nextDose?.let { dose ->
                    binding.spinnerNextDose.setSelection(dose - 1)
                }

                binding.edtNextVaccinePlace.setText(record.nextPlace ?: "")

                setupNextVaccineVisibility(true)
            }
        }
    }

    private fun saveVaccinationRecord() {
        val vaccineName = binding.spinnerVaccineName.selectedItem?.toString()
        if (vaccineName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select vaccine name", Toast.LENGTH_SHORT).show()
            return
        }

        val dose = binding.spinnerDose.selectedItem?.toString()?.toIntOrNull() ?: 1

        val now = System.currentTimeMillis()
        val record = VaccinationRecord(
            id = existingRecord?.id ?: UUID.randomUUID().toString(),
            petId = currentPet?.petId ?: "",
            vaccineName = vaccineName,
            dose = dose,
            timestamp = selectedTimestamp,
            place = binding.edtVaccinePlace.text.toString().ifEmpty { null },
            nextDueDate = nextSelectedTimestamp,
            nextDose = if (nextSelectedTimestamp != null) {
                binding.spinnerNextDose.selectedItem?.toString()?.toIntOrNull() ?: (dose + 1)
            } else null,
            nextVaccineName = if (nextSelectedTimestamp != null) {
                binding.spinnerNextVaccineName.selectedItem?.toString() ?: vaccineName
            } else null,
            nextPlace = binding.edtNextVaccinePlace.text.toString().ifEmpty { null },
            createdAt = existingRecord?.createdAt ?: now,
            updatedAt = now
        )

        saveToFirebase(record)
    }

    private fun saveToFirebase(record: VaccinationRecord) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return
        val petId = currentPet?.petId ?: return

        // ถ้าเป็นการแก้ไข ให้ลบ Event เดิมก่อน
        if (existingRecord != null && existingRecord?.nextDueDate != null) {
            deleteExistingEvent(existingRecord!!.id)
        }

        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .document(petId)
            .collection("records")
            .document(record.id)
            .set(record)
            .addOnSuccessListener {
                // สร้าง Event ใน Schedule ถ้ามี nextDueDate
                if (record.nextDueDate != null) {
                    createEventFromVaccination(record)
                } else {
                    Toast.makeText(
                        requireContext(),
                        if (existingRecord == null) "Vaccination record saved" else "Vaccination record updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createEventFromVaccination(record: VaccinationRecord) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        val event = Event.fromVaccination(
            vaccinationId = record.id,
            petId = record.petId,
            vaccineName = record.vaccineName,
            dose = record.dose,
            dueDate = com.google.firebase.Timestamp(Date(record.nextDueDate!!)),
            place = record.nextPlace
        )

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .document(event.eventId)
            .set(event)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    if (existingRecord == null) "Vaccination record and reminder saved" else "Vaccination record updated",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Vaccination saved but reminder failed: ${e.message}", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
    }

    private fun deleteExistingEvent(vaccinationId: String) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .whereEqualTo("sourceId", vaccinationId)
            .whereEqualTo("sourceType", "vaccination")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    doc.reference.delete()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}