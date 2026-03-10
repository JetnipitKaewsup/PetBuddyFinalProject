package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.MyPetsAdapter
import com.example.petbuddy.databinding.FragmentAllMyPetsBinding
import com.example.petbuddy.model.Pet


class AllMyPetsFragment : Fragment() {
    private var _binding: FragmentAllMyPetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: MyPetsAdapter
    private var pets: List<Pet> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllMyPetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAllPets()

        binding.toolbar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }



    private fun setupRecyclerView() {
        adapter = MyPetsAdapter { pet ->
            navigateToPetDetail(pet)
        }
        binding.rvAllPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllPets.adapter = adapter
    }
    private fun loadAllPets() {
        binding.progressBar.visibility = View.VISIBLE

        baseActivity.loadAllPets { petList ->
            binding.progressBar.visibility = View.GONE
            pets = petList
            adapter.submitList(pets)

            if (pets.isEmpty()) {
                binding.tvNoPets.visibility = View.VISIBLE
                binding.rvAllPets.visibility = View.GONE
            } else {
                binding.tvNoPets.visibility = View.GONE
                binding.rvAllPets.visibility = View.VISIBLE
                binding.tvPetCount.text = "Total ${pets.size} pets"
            }
        }
    }

    private fun navigateToPetDetail(pet: Pet) {
        Toast.makeText(requireContext(), "Viewing ${pet.petName}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to Pet Detail Fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}