package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petbuddy.R
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.databinding.FragmentFeedingBinding
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.notifications.ReminderManager
import com.example.petbuddy.util.Constants

class FeedingFragment : Fragment() {

    private var _binding: FragmentFeedingBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private var selectedPets: List<Pet> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFeedingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseActivity = activity as BaseActivity

        selectedPets = baseActivity.selectedPets

        setupToolbar()
        setupRecyclerView()
        setupButtons()
        setupToolbar()
        setupRecyclerView()
        setupButtons()

        ReminderManager.scheduleFeedingReminder(
            requireContext(),
            "Luna",
            18,
            30
        )
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            baseActivity.onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupRecyclerView() {

        binding.showFeeding.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        binding.showRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupButtons() {

        binding.btnAddFeeding.setOnClickListener {

//            val fragment = PetSelectionFragment().apply {
//                arguments = Bundle().apply {
//                    putSerializable("mode", SelectionMode.MULTIPLE)
//                    putString("source_tag", "feeding")
//                }
//            }

//            parentFragmentManager.beginTransaction()
//                .replace(
//                    com.example.petbuddy.R.id.fragment_container,
//                    fragment
//                )
//                .addToBackStack("feeding")
//                .commit()

            val fragment = FeedingAlarmFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        }

        binding.showRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

    }

    private fun setupButtons() {

        binding.btnAddFeeding.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    com.example.petbuddy.R.id.fragment_container,
                    PetSelectionFragment()
                )
                .addToBackStack(null)
                .commit()

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}