package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentFeedingBinding
import com.example.petbuddy.model.Pet

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
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            baseActivity.onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupRecyclerView() {

        binding.showFeeding.layoutManager = LinearLayoutManager(requireContext())
        binding.showRecords.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun setupButtons() {

        binding.btnAddFeeding.setOnClickListener {

            val fragment = FeedingAlarmFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}