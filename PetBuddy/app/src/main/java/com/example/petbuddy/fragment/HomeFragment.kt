package com.example.petbuddy.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petbuddy.R
import com.example.petbuddy.databinding.FragmentHomeBinding

/**
 * HomeFragment - หน้าแรกของแอป
 * ไม่ต้องเลือกสัตว์เลี้ยง
 */
class HomeFragment : Fragment() {

    // View Binding - เชื่อมกับ fragment_home.xml
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // สร้าง Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: โหลดข้อมูลสำหรับหน้า Home
        setupUI()
    }

    private fun setupUI() {
        binding.tvTitle.text = "ยินดีต้อนรับสู่ PetBuddy"
        // TODO: เพิ่มการทำงานอื่นๆ
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // ป้องกัน memory leak
    }
}