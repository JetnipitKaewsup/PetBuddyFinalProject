package com.example.petbuddy.model

data class VaccineInfo(
    val name: String,
    val type: PetType, // DOG หรือ CAT
    val description: String? = null,
    val isCore: Boolean = false, // วัคซีนหลัก
    val recommendedAge: String? = null,
    val frequency: String? = null // ความถี่ที่ต้องฉีด
)

enum class PetType {
    DOG, CAT
}

// ข้อมูลวัคซีนสำหรับสุนัขและแมว
object VaccineData {

    // วัคซีนหลักสำหรับสุนัข
    val dogCoreVaccines = listOf(
        VaccineInfo("DHPP", PetType.DOG, "Distemper, Hepatitis, Parainfluenza, Parvo", true, "6-8 weeks", "Every 3-4 weeks until 16 weeks"),
        VaccineInfo("DHPPL", PetType.DOG, "Distemper, Hepatitis, Parainfluenza, Parvo, Leptospirosis", true, "8 weeks", "Booster annually"),
        VaccineInfo("Rabies", PetType.DOG, "Rabies virus", true, "12-16 weeks", "Booster annually or every 3 years")
    )

    // วัคซีนเสริมสำหรับสุนัข
    val dogNonCoreVaccines = listOf(
        VaccineInfo("Bordetella", PetType.DOG, "Kennel cough", false, "8 weeks", "Annually"),
        VaccineInfo("Leptospirosis", PetType.DOG, "Leptospirosis", false, "12 weeks", "Annually"),
        VaccineInfo("Lyme", PetType.DOG, "Lyme disease", false, "12 weeks", "Annually"),
        VaccineInfo("Canine Influenza", PetType.DOG, "Canine flu", false, "8 weeks", "Annually")
    )

    // วัคซีนหลักสำหรับแมว
    val catCoreVaccines = listOf(
        VaccineInfo("FVRCP", PetType.CAT, "Feline Viral Rhinotracheitis, Calicivirus, Panleukopenia", true, "6-8 weeks", "Every 3-4 weeks until 16 weeks"),
        VaccineInfo("Rabies", PetType.CAT, "Rabies virus", true, "12-16 weeks", "Booster annually or every 3 years")
    )

    // วัคซีนเสริมสำหรับแมว
    val catNonCoreVaccines = listOf(
        VaccineInfo("FeLV", PetType.CAT, "Feline Leukemia", false, "8 weeks", "Annually"),
        VaccineInfo("FIV", PetType.CAT, "Feline Immunodeficiency Virus", false, "8 weeks", "Annually"),
        VaccineInfo("Chlamydia", PetType.CAT, "Chlamydia felis", false, "9 weeks", "Annually")
    )

    fun getVaccinesByPetType(petType: String): List<VaccineInfo> {
        return when (petType.lowercase()) {
            "dog" -> dogCoreVaccines + dogNonCoreVaccines
            "cat" -> catCoreVaccines + catNonCoreVaccines
            else -> emptyList()
        }
    }

    fun getCoreVaccinesByPetType(petType: String): List<VaccineInfo> {
        return when (petType.lowercase()) {
            "dog" -> dogCoreVaccines
            "cat" -> catCoreVaccines
            else -> emptyList()
        }
    }
}