package com.example.petbuddy.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.PetPreviewAdapter
import com.example.petbuddy.databinding.FragmentAddEventBinding
import com.example.petbuddy.model.Event
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.util.Constants
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator

    private var existingEvent: Event? = null
    private var selectedStartDate: Date = Date()  // เก็บเป็น Date ก่อน
    private var selectedPets: MutableList<Pet> = mutableListOf()
    private var allPets: List<Pet> = emptyList()

    // Formatters
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Tag options
    private val tagOptions = arrayOf("General", "Vet Visit", "Grooming", "Medication", "Play Date", "Training", "Other")

    companion object {
        private const val ARG_EVENT = "event"
        private const val ARG_SELECTED_DATE = "selected_date"
        private const val REQUEST_KEY_PETS = "pets_selected"  // ประกาศตรงนี้!
        private const val RESULT_PET_IDS = "selected_pet_ids"

        fun newInstance(existingEvent: Event? = null, selectedDate: Date? = null): AddEventFragment {
            return AddEventFragment().apply {
                arguments = Bundle().apply {
                    if (existingEvent != null) {
                        putSerializable(ARG_EVENT, existingEvent)
                    }
                    if (selectedDate != null) {
                        putSerializable(ARG_SELECTED_DATE, selectedDate)
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

        // โหลด existing event ถ้ามี
        existingEvent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_EVENT, Event::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_EVENT) as? Event
        }

        // ถ้ามีวันที่ส่งมา (จาก ScheduleFragment)
        val selectedDate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_SELECTED_DATE, Date::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_SELECTED_DATE) as? Date
        }

        selectedStartDate = selectedDate ?: Date()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupSpinners()
        setupDatePickers()
        setupListeners()
        setupFragmentResultListener()
        loadPets()

        if (existingEvent != null) {
            loadExistingData()
        } else {
            updateDateTimeDisplay(selectedStartDate)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.title = if (existingEvent == null) "Add Event" else "Edit Event"
    }

    private fun setupSpinners() {
        // Tag Spinner
        val tagAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tagOptions)
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTag.adapter = tagAdapter
    }

    private fun setupDatePickers() {
        // Date picker
        binding.layoutDate.setOnClickListener {
            showDatePicker()
        }

        // Time picker
        binding.layoutTime.setOnClickListener {
            if (!binding.swAllDay.isChecked) {
                showTimePicker()
            }
        }

        // All Day checkbox
        binding.swAllDay.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutTime.isEnabled = !isChecked
            if (isChecked) {
                binding.tvTime.text = "All Day"
            } else {
                updateDateTimeDisplay(selectedStartDate)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { time = selectedStartDate }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, dayOfMonth)

                // เก็บเวลาเดิม
                val timeCalendar = Calendar.getInstance().apply { time = selectedStartDate }
                newCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                newCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

                selectedStartDate = newCalendar.time
                updateDateTimeDisplay(selectedStartDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance().apply { time = selectedStartDate }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedStartDate = calendar.time
                updateDateTimeDisplay(selectedStartDate)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupFragmentResultListener() {
        // รับผลลัพธ์จาก PetSelectionFragment
        setFragmentResultListener(REQUEST_KEY_PETS) { _, bundle ->
            val petIds = bundle.getStringArrayList(RESULT_PET_IDS)
            if (!petIds.isNullOrEmpty()) {
                loadSelectedPetsFromIds(petIds)
            }
        }
    }
    private fun updateDateTimeDisplay(date: Date) {
        binding.tvDate.text = dateFormatter.format(date)
        binding.tvTime.text = timeFormatter.format(date)
    }

    private fun setupListeners() {
        // เลือกสัตว์เลี้ยง
        binding.btnSelectPets.setOnClickListener {
            navigateToPetSelection()
        }

        // บันทึก
        binding.btnSave.setOnClickListener {
            saveEvent()
        }
    }

    private fun loadPets() {
        baseActivity.loadAllPets { pets ->
            allPets = pets
        }
    }

    private fun loadExistingData() {
        existingEvent?.let { event ->
            binding.etTitle.setText(event.title)

            // เลือก tag
            val tagPosition = tagOptions.indexOf(event.tag ?: "General")
            if (tagPosition >= 0) {
                binding.spinnerTag.setSelection(tagPosition)
            }

            binding.swAllDay.isChecked = event.isAllDay
            selectedStartDate = event.startDate.toDate()  // แปลง Timestamp -> Date
            updateDateTimeDisplay(selectedStartDate)

            binding.etPlace.setText(event.place ?: "")
            binding.etNote.setText(event.note ?: "")

            // โหลดสัตว์เลี้ยงที่เลือก
            if (event.petIds.isNotEmpty()) {
                loadSelectedPetsFromIds(event.petIds)
            }
        }
    }

    private fun loadSelectedPetsFromIds(petIds: List<String>) {
        selectedPets.clear()
        petIds.forEach { petId ->
            baseActivity.loadPetById(petId) { pet ->
                pet?.let {
                    selectedPets.add(it)
                    if (selectedPets.size == petIds.size) {
                        updateSelectedPetsDisplay()
                    }
                }
            }
        }
    }

    private fun navigateToPetSelection() {
        navigator.navigateToPetSelectionForEvent(
            mode = SelectionMode.MULTIPLE,
            sourceTag = Constants.TAG_SCHEDULE,
            requestKey = REQUEST_KEY_PETS,
            selectedPetIds = selectedPets.map { it.petId }
        )
    }

    fun onPetsSelected(selectedPetsList: List<Pet>) {
        selectedPets.clear()
        selectedPets.addAll(selectedPetsList)
        updateSelectedPetsDisplay()
    }

    private fun updateSelectedPetsDisplay() {
        if (selectedPets.isEmpty()) {
            binding.horizontalScrollView.visibility = View.GONE
            binding.tvNoPetsSelected.visibility = View.VISIBLE
        } else {
            binding.horizontalScrollView.visibility = View.VISIBLE
            binding.tvNoPetsSelected.visibility = View.GONE

            val container = binding.layoutSelectedPets
            container.removeAllViews()

            selectedPets.forEach { pet ->
                val imageView = ImageView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(80, 80)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(4, 4, 4, 4)
                    setOnClickListener {
                        showRemovePetDialog(pet)
                    }
                }

                if (!pet.imagePath.isNullOrEmpty()) {
                    Glide.with(this)
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

                container.addView(imageView)
            }
        }
    }

    private fun showRemovePetDialog(pet: Pet) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove Pet")
            .setMessage("Remove ${pet.petName} from this event?")
            .setPositiveButton("Remove") { _, _ ->
                selectedPets.remove(pet)
                updateSelectedPetsDisplay()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveEvent() {
        val title = binding.etTitle.text.toString()
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter event title", Toast.LENGTH_SHORT).show()
            return
        }

        val now = Timestamp.now()

        // สร้าง startDate จาก selectedStartDate
        val startTimestamp = Timestamp(selectedStartDate)

        // คำนวณ endDate (ถ้าเป็น all day หรือ event ข้ามวัน)
        val endTimestamp = if (binding.swAllDay.isChecked) {
            // all day event: ตั้งแต่ 00:00 ถึง 23:59 ของวันเดียวกัน
            val calendar = Calendar.getInstance().apply { time = selectedStartDate }
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            Timestamp(calendar.time)
        } else {
            // normal event: ใช้เวลาเดียวกัน (หรือจะให้เพิ่ม duration ก็ได้)
            startTimestamp
        }

        val event = Event(
            eventId = existingEvent?.eventId ?: UUID.randomUUID().toString(),
            title = title,
            tag = binding.spinnerTag.selectedItem.toString(),
            isAllDay = binding.swAllDay.isChecked,
            startDate = startTimestamp,
            endDate = endTimestamp,
            place = binding.etPlace.text.toString().ifEmpty { null },
            note = binding.etNote.text.toString().ifEmpty { null },
            petIds = selectedPets.map { it.petId },
            createdAt = existingEvent?.createdAt ?: now,
            updatedAt = now
        )

        saveToFirebase(event)
    }

    private fun saveToFirebase(event: Event) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .document(event.eventId)
            .set(event)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    if (existingEvent == null) "Event saved successfully" else "Event updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}