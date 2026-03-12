package com.example.petbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.FeedingSettingAdapter
import com.example.petbuddy.databinding.FragmentSettingFeedingBinding
import com.example.petbuddy.model.FeedingSchedule
import com.example.petbuddy.model.Pet

class SettingFeedingFragment : Fragment() {

    private var _binding: FragmentSettingFeedingBinding? = null
    private val binding get() = _binding!!

    private lateinit var baseActivity: BaseActivity
    private lateinit var adapter: FeedingSettingAdapter

    private var petMap: Map<String, Pet> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingFeedingBinding.inflate(inflater, container, false)

        baseActivity = requireActivity() as BaseActivity

        setupToolbar()
        setupRecyclerView()
        enableSwipeDelete()
        buttonNavigation()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {

        adapter = FeedingSettingAdapter(
            feedingList = emptyList(),
            petMap = petMap,

            onEditClick = { schedule ->
                openEditFragment(schedule)
            },

            onPetClick = { petId ->
                baseActivity.showToast("Pet clicked: $petId")
            }
        )

        binding.rvFeedingSettings.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvFeedingSettings.adapter = adapter
    }

    private fun loadData() {

        baseActivity.loadAllPets { pets ->

            petMap = pets.associateBy { it.petId }

            adapter.updatePetMap(petMap)

            baseActivity.loadFeedingSchedules { schedules ->

                val activeSchedules = schedules.filter { it.isActive }

                adapter.submitList(activeSchedules)
            }
        }
    }

    private fun buttonNavigation() {

        binding.btnAddFeedingSchedule.setOnClickListener {
            openAddFragment()
        }
    }

    private fun openAddFragment() {

        val fragment = FeedingAlarmFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openEditFragment(schedule: FeedingSchedule) {

        val fragment = FeedingAlarmFragment()

        val bundle = Bundle()
        bundle.putString("scheduleId", schedule.id)

        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun enableSwipeDelete() {

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {

                val position = viewHolder.adapterPosition
                val schedule = adapter.getItem(position)

                adapter.removeItem(position)

                baseActivity.deleteFeedingSchedule(schedule.id)

                baseActivity.showToast("Feeding deleted")
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)

        itemTouchHelper.attachToRecyclerView(binding.rvFeedingSettings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}