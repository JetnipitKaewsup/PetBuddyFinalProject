// PetHeaderHelper.kt
package com.example.petbuddy.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.example.petbuddy.R
import com.example.petbuddy.databinding.PetHeaderBinding
import com.example.petbuddy.fragment.HealthDashboardFragment
import com.example.petbuddy.fragment.PetSelectionFragment
import com.example.petbuddy.fragment.VaccinationFragment
import com.example.petbuddy.fragment.WeightFragment
import com.example.petbuddy.model.Pet
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.viewmodel.SharedPetViewModel
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Helper class สำหรับจัดการ Pet Header ใน Fragment ต่างๆ
 * ทำให้ header มีลักษณะเดียวกันทุกหน้า
 */
class PetHeaderHelper(
    private val fragment: Fragment,
    private val container: ViewGroup,
    private val viewModel: SharedPetViewModel
) {

    private lateinit var binding: PetHeaderBinding

    // Views ที่ใช้งาน
    lateinit var ivPetImage: CircleImageView
    lateinit var tvPetName: TextView
    lateinit var tvPetDetail: TextView
    lateinit var btnChangePet: Button
    lateinit var tvSelectedCount: TextView

    init {
        setupHeader()
        observeViewModel()
    }

    private fun setupHeader() {
        // Inflate header layout
        binding = PetHeaderBinding.inflate(
            LayoutInflater.from(fragment.requireContext()),
            container,
            true  // attach to root
        )

        // เก็บ reference views
        ivPetImage = binding.ivPetHeaderImage
        tvPetName = binding.tvPetHeaderName
        tvPetDetail = binding.tvPetHeaderDetail
        btnChangePet = binding.btnChangePet
        tvSelectedCount = binding.tvSelectedCount
    }

    private fun observeViewModel() {
        // สำหรับโหมด SINGLE - ดูสัตว์เลี้ยงตัวเดียว
        viewModel.selectedPet.observe(fragment.viewLifecycleOwner) { pet ->
            pet?.let {
                showSinglePetMode(it)
            }
        }

        // สำหรับโหมด MULTIPLE - ดูจำนวนสัตว์เลี้ยงที่เลือก
        viewModel.selectedPets.observe(fragment.viewLifecycleOwner) { pets ->
            if (pets.isNotEmpty()) {
                showMultiplePetMode(pets)
            } else {
                showNoPetSelected()
            }
        }
    }

    private fun showSinglePetMode(pet: Pet) {
        // แสดงข้อมูลสัตว์เลี้ยงตัวเดียว
        btnChangePet.visibility = Button.VISIBLE
        tvSelectedCount.visibility = TextView.GONE

        Glide.with(fragment.requireContext())
            .load(pet.imageUrl)
            .placeholder(R.drawable.pet_placeholder)
            .error(R.drawable.pet_placeholder)
            .circleCrop()
            .into(ivPetImage)

        tvPetName.text = pet.name
        tvPetDetail.text = pet.species

        // ตั้งค่าปุ่มเปลี่ยน
        btnChangePet.setOnClickListener {
            // ไป PetSelectionFragment ในโหมด SINGLE
            val petSelectionFragment = PetSelectionFragment()
            val data = Bundle().apply {
                putSerializable("mode", SelectionMode.SINGLE)
                putString("source", fragment.javaClass.simpleName)
            }
            petSelectionFragment.arguments = data

            fragment.parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, petSelectionFragment)
                .addToBackStack("pet_selection")
                .commit()
        }
    }

    private fun showMultiplePetMode(pets: List<Pet>) {
        // แสดงจำนวนสัตว์เลี้ยงที่เลือก
        btnChangePet.visibility = Button.GONE
        tvSelectedCount.visibility = TextView.VISIBLE

        // ถ้ามีสัตว์เลี้ยง แสดงรูปตัวแรกเป็นตัวอย่าง
        if (pets.isNotEmpty()) {
            val firstPet = pets.first()
            Glide.with(fragment.requireContext())
                .load(firstPet.imageUrl)
                .placeholder(R.drawable.pet_placeholder)
                .error(R.drawable.pet_placeholder)
                .circleCrop()
                .into(ivPetImage)

            tvPetName.text = "เลือก ${pets.size} ตัว"
            tvPetDetail.text = pets.joinToString { it.name }
            tvSelectedCount.text = "${pets.size} ตัว"
        }
    }

    private fun showNoPetSelected() {
        // ไม่มีสัตว์เลี้ยงที่เลือก
        ivPetImage.setImageResource(R.drawable.pet_placeholder)
        tvPetName.text = "ยังไม่ได้เลือกสัตว์เลี้ยง"
        tvPetDetail.text = "กรุณาเลือกสัตว์เลี้ยง"
        btnChangePet.visibility = Button.VISIBLE
        tvSelectedCount.visibility = TextView.GONE

        btnChangePet.setOnClickListener {
            // ไป PetSelectionFragment ในโหมดตาม fragment
            val mode = when (fragment) {
                is HealthDashboardFragment,
                is WeightFragment,
                is VaccinationFragment -> SelectionMode.SINGLE
                else -> SelectionMode.MULTIPLE
            }

            val petSelectionFragment = PetSelectionFragment()
            val data = Bundle().apply {
                putSerializable("mode", mode)
                putString("source", fragment.javaClass.simpleName)
            }
            petSelectionFragment.arguments = data

            fragment.parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, petSelectionFragment)
                .addToBackStack("pet_selection")
                .commit()
        }
    }

    fun setOnChangePetClickListener(listener: () -> Unit) {
        btnChangePet.setOnClickListener { listener() }
    }
}