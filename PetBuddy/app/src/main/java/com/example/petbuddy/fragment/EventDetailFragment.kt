package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentEventDetailBinding
import com.example.petbuddy.model.Event
import com.example.petbuddy.model.Pet
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.notifications.ReminderManager
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator

    private var event: Event? = null
    private var relatedPets: MutableList<Pet> = mutableListOf()

    companion object {
        private const val ARG_EVENT = "event"

        fun newInstance(event: Event): EventDetailFragment {
            return EventDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_EVENT, event)
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
        event = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_EVENT, Event::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_EVENT) as? Event
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadEventData()
        loadRelatedPets()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadEventData() {
        event?.let { event ->
            binding.tvTitle.text = event.title
            binding.tvTag.text = event.tag ?: "General"
            binding.tvDate.text = formatDate(event.startDate.toDate())
            binding.tvTime.text = if (event.isAllDay) "All Day" else formatTime(event.startDate.toDate())
            binding.tvPlace.text = event.place ?: "No location specified"
            binding.tvNote.text = event.note ?: "No notes"

            // แสดง reminder ถ้ามี
            if (event.reminderEnabled && event.reminderBefore > 0) {
                binding.tvReminder.visibility = View.VISIBLE
                binding.tvReminder.text = getReminderText(event.reminderBefore)
            } else {
                binding.tvReminder.visibility = View.GONE
            }
        }
    }

    private fun loadRelatedPets() {
        event?.petIds?.let { petIds ->
            if (petIds.isEmpty()) {
                binding.tvNoPets.visibility = View.VISIBLE
                binding.petsContainer.visibility = View.GONE
                return
            }

            relatedPets.clear()
            petIds.forEach { petId ->
                baseActivity.loadPetById(petId) { pet ->
                    pet?.let {
                        relatedPets.add(it)
                        if (relatedPets.size == petIds.size) {
                            displayPets()
                        }
                    }
                }
            }
        }
    }

    private fun displayPets() {
        binding.tvNoPets.visibility = View.GONE
        binding.petsContainer.visibility = View.VISIBLE
        binding.petsContainer.removeAllViews()

        relatedPets.forEach { pet ->
            val petView = layoutInflater.inflate(R.layout.item_pet_thumbnail, binding.petsContainer, false)

            val ivPet = petView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivPetThumbnail)
            val tvPetName = petView.findViewById<TextView>(R.id.tvPetName)

            tvPetName.text = pet.petName

            if (!pet.imagePath.isNullOrEmpty()) {
                Glide.with(this)
                    .load(pet.imagePath)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.pet_placeholder)
                            .error(R.drawable.pet_placeholder)
                            .circleCrop()
                    )
                    .into(ivPet)
            } else {
                ivPet.setImageResource(R.drawable.pet_placeholder)
            }

            binding.petsContainer.addView(petView)
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            event?.let {
                navigator.navigateToEditEvent(it)
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete") { _, _ ->
                deleteEvent()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteEvent() {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return
        val eventId = event?.eventId ?: return

        // ยกเลิก alarm ถ้ามี
        event?.let {
            if (it.reminderEnabled && it.reminderBefore > 0) {
                ReminderManager.cancelEventReminder(
                    context = requireContext(),
                    eventId = it.eventId,
                    reminderBeforeMinutes = it.reminderBefore
                )
            }
        }

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return format.format(date)
    }

    private fun formatTime(date: Date): String {
        val format = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return format.format(date)
    }

    private fun getReminderText(minutes: Int): String {
        return when (minutes) {
            0 -> "At time of event"
            5 -> "5 minutes before"
            15 -> "15 minutes before"
            30 -> "30 minutes before"
            60 -> "1 hour before"
            120 -> "2 hours before"
            1440 -> "1 day before"
            2880 -> "2 days before"
            else -> "$minutes minutes before"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}