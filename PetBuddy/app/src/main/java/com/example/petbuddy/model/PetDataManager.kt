package com.example.petbuddy.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlin.apply
import kotlin.collections.remove
import kotlin.text.contains

class PetDataManager (context: Context){
    private val prefs: SharedPreferences = context.getSharedPreferences("pet_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CURRENT_PET = "current_pet"  // เก็บ Pet object
        private const val KEY_CURRENT_PET_ID = "current_pet_id"
        private const val KEY_CURRENT_PET_NAME = "current_pet_name"
    }

    /**
     * บันทึกสัตว์เลี้ยงที่กำลังใช้งานอยู่
     */
    fun saveCurrentPet(pet: Pet) {
        // บันทึกทั้ง object (เผื่อใช้ข้อมูลอื่นๆ)
        val json = gson.toJson(pet)
        prefs.edit().putString(KEY_CURRENT_PET, json).apply()

        // บันทึกเฉพาะ id และ name เผื่อใช้บ่อยๆ
        prefs.edit()
            .putString(KEY_CURRENT_PET_ID, pet.id)
            .putString(KEY_CURRENT_PET_NAME, pet.name)
            .apply()
    }

    /**
     * อ่านสัตว์เลี้ยงที่กำลังใช้งานอยู่
     */
    fun getCurrentPet(): Pet? {
        val json = prefs.getString(KEY_CURRENT_PET, null)
        return if (json != null) {
            gson.fromJson(json, Pet::class.java)
        } else null
    }

    /**
     * อ่านเฉพาะ ID
     */
    fun getCurrentPetId(): String? {
        return prefs.getString(KEY_CURRENT_PET_ID, null)
    }

    /**
     * อ่านเฉพาะชื่อ
     */
    fun getCurrentPetName(): String? {
        return prefs.getString(KEY_CURRENT_PET_NAME, null)
    }

    /**
     * ตรวจสอบว่ามีสัตว์เลี้ยงที่เลือกหรือไม่
     */
    fun hasCurrentPet(): Boolean {
        return prefs.contains(KEY_CURRENT_PET_ID)
    }

    /**
     * ล้างข้อมูลสัตว์เลี้ยงที่เลือก
     */
    fun clearCurrentPet() {
        prefs.edit()
            .remove(KEY_CURRENT_PET)
            .remove(KEY_CURRENT_PET_ID)
            .remove(KEY_CURRENT_PET_NAME)
            .apply()
    }
}
