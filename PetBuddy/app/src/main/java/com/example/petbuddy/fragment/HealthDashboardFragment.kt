package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.databinding.FragmentHealthDashboardBinding
import com.example.petbuddy.model.SelectionMode
import com.example.petbuddy.navigation.MainNavigator
import com.example.petbuddy.util.Constants
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class HealthDashboardFragment : Fragment() {

    private var _binding: FragmentHealthDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupUI()
        setupClickListeners()
        checkSelectedPet()
    }

    override fun onResume() {
        super.onResume()
        checkSelectedPet()
    }

    private fun setupToolbar(){
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    private fun checkSelectedPet() {
        val currentPet = baseActivity.selectedPet
        if (currentPet == null) {
            goToPetSelection()
        } else {
            updatePetInfo(currentPet)
            loadHealthData(currentPet.petId)
        }
    }

    private fun setupUI() {
        val currentPet = baseActivity.selectedPet
        if (currentPet != null) {
            binding.tvPetName.text = currentPet.petName
        }
    }

    private fun updatePetInfo(pet: com.example.petbuddy.model.Pet) {
        binding.tvPetName.text = pet.petName
    }

    private fun loadHealthData(petId: String) {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        loadLatestWeight(userId, petId)
        loadNextVaccine(userId, petId)  // ส่ง userId และ petId
    }

    private fun loadLatestWeight(userId: String, petId: String) {
        baseActivity.db.collection("users")
            .document(userId)
            .collection("weights")
            .document(petId)
            .collection("records")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (isAdded && _binding != null) {
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val weight = doc.getDouble("weight") ?: 0.0

                        // ใช้ getLong() เพราะ timestamp เก็บเป็น Long
                        val timestamp = doc.getLong("timestamp")

                        binding.tvLatestWeight.text = String.format("%.1f kg", weight)

                        timestamp?.let { time ->
                            val date = Date(time)
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                            binding.tvWeightDate.text = "Recorded on ${dateFormat.format(date)}"
                            binding.tvWeightDate.visibility = View.VISIBLE
                            binding.tvNoWeight.visibility = View.GONE
                        }


                    } else {
                        binding.tvLatestWeight.visibility = View.GONE
                        binding.tvWeightDate.visibility = View.GONE
                        binding.tvNoWeight.visibility = View.VISIBLE

                    }
                }
            }
            .addOnFailureListener {
                binding.tvLatestWeight.text = "- kg"

                binding.tvWeightDate.visibility = View.GONE
            }
    }

    private fun loadNextVaccine(userId: String, petId: String) {
        // Path: /users/{userId}/vaccinations/{petId}/records
        baseActivity.db.collection("users")
            .document(userId)
            .collection("vaccinations")
            .document(petId)  // ต้องมี document(petId) ตรงนี้
            .collection("records")
            .whereGreaterThan("nextDueDate", System.currentTimeMillis())
            .orderBy("nextDueDate", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (isAdded && _binding != null) {
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val vaccineName = doc.getString("vaccineName") ?: "Vaccine"
                        val dose = doc.getLong("dose")?.toInt() ?: 1
                        val nextDose = doc.getLong("nextDose")?.toInt() ?: (dose + 1)

                        // ใช้ getLong() เพราะ nextDueDate เก็บเป็น Long
                        val nextDueDate = doc.getLong("nextDueDate")

                        binding.tvNextVaccine.text = "$vaccineName (Dose $nextDose)"
                        binding.tvNextVaccine.visibility = View.VISIBLE

                        nextDueDate?.let { dueTime ->
                            val daysUntil = ((dueTime - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                            val statusText = when {
                                daysUntil < 0 -> "Overdue by ${-daysUntil} days"
                                daysUntil == 0 -> "Due today"
                                else -> "Due in $daysUntil days"
                            }

                            binding.tvNextVaccineStatus.text = statusText
                            binding.tvNextVaccineStatus.visibility = View.VISIBLE

                            val color = when {
                                daysUntil < 0 -> android.graphics.Color.parseColor("#F44336") // Red
                                daysUntil <= 7 -> android.graphics.Color.parseColor("#FF9800") // Orange
                                else -> android.graphics.Color.parseColor("#4CAF50") // Green
                            }
                            binding.tvNextVaccineStatus.setTextColor(color)
                        }
                    } else {
                        binding.tvNextVaccine.text = "No upcoming vaccines"
                        binding.tvNextVaccine.visibility = View.VISIBLE
                        binding.tvNextVaccineStatus.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener { e ->
                binding.tvNextVaccine.text = "Error loading vaccines"
                binding.tvNextVaccineStatus.visibility = View.GONE
            }
    }

    private fun setupClickListeners() {
        binding.btnChangePet.setOnClickListener {
            goToPetSelection()
        }

        binding.cardWeight.setOnClickListener {
            navigator.navigateToWeight()
        }

        binding.cardVaccination.setOnClickListener {
            navigator.navigateToVaccination()
        }
    }

    private fun goToPetSelection() {
        val fragment = PetSelectionFragment()
        val data = Bundle().apply {
            putSerializable("mode", SelectionMode.SINGLE)
            putString("source_tag", Constants.TAG_HEALTH_DASHBOARD)
        }
        fragment.arguments = data

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("pet_selection")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}