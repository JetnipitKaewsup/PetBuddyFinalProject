package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petbuddy.databinding.FragmentSettingFeedingBinding

class SettingFeedingFragment : Fragment() {

    private var _binding: FragmentSettingFeedingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingFeedingBinding.inflate(inflater, container, false)

        setupToolbar()

        return binding.root
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}