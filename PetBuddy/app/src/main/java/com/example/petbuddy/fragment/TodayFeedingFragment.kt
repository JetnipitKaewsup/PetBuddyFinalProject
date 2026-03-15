package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.databinding.FragmentTodayFeedingBinding

class TodayFeedingFragment : Fragment() {

    private var _binding: FragmentTodayFeedingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTodayFeedingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.recyclerTodayFeeding.layoutManager =
            LinearLayoutManager(requireContext())

        // TODO: load full feeding list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}