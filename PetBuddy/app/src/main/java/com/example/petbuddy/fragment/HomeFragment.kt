package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setUpDate()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: โหลดข้อมูลสำหรับหน้า Home

    }

    private fun setUpDate(){
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val currentDate = formatter.format(calendar.time)

        binding.homeDate.text = currentDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // ป้องกัน memory leak
    }
}