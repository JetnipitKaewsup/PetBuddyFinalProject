package com.example.petbuddy.model

/**
 * ข้อมูลวัคซีนสำหรับสุนัขและแมว
 * อ้างอิงจาก WSAVA, AAHA, AAFP Guidelines
 */

data class VaccineInfo(
    val name: String,
    val type: PetType,
    val description: String? = null,
    val isCore: Boolean = false,
    val isBooster: Boolean = false,
    val recommendedAge: String? = null,
    val frequency: String? = null
)

enum class PetType {
    DOG, CAT
}

object VaccineData {

    // ========== DOG VACCINES ==========
    val dogCoreVaccines = listOf(
        VaccineInfo(
            name = "DHPP (Distemper, Hepatitis, Parainfluenza, Parvo)",
            type = PetType.DOG,
            description = "Combination vaccine for Canine Distemper, Adenovirus, Parainfluenza, and Parvovirus",
            isCore = true,
            isBooster = false,
            recommendedAge = "6-8 weeks",
            frequency = "Every 3-4 weeks until 16-20 weeks"
        ),
        VaccineInfo(
            name = "Rabies",
            type = PetType.DOG,
            description = "Rabies virus vaccine",
            isCore = true,
            isBooster = false,
            recommendedAge = "12-16 weeks",
            frequency = "Booster 1 year later, then every 1-3 years"
        )
    )

    val dogNonCoreVaccines = listOf(
        VaccineInfo(
            name = "Bordetella bronchiseptica (Kennel Cough)",
            type = PetType.DOG,
            description = "Prevents kennel cough/respiratory infections",
            isCore = false,
            isBooster = false,
            recommendedAge = "8 weeks",
            frequency = "Annually or every 6 months for high-risk dogs"
        ),
        VaccineInfo(
            name = "Leptospirosis",
            type = PetType.DOG,
            description = "Protects against Leptospira bacteria",
            isCore = false,
            isBooster = false,
            recommendedAge = "12 weeks",
            frequency = "Annually"
        ),
        VaccineInfo(
            name = "Lyme Disease (Borrelia burgdorferi)",
            type = PetType.DOG,
            description = "Protects against tick-borne Lyme disease",
            isCore = false,
            isBooster = false,
            recommendedAge = "12 weeks",
            frequency = "Annually before tick season"
        ),
        VaccineInfo(
            name = "Canine Influenza (H3N8/H3N2)",
            type = PetType.DOG,
            description = "Protects against canine flu",
            isCore = false,
            isBooster = false,
            recommendedAge = "6-8 weeks",
            frequency = "Annually"
        ),
        VaccineInfo(
            name = "Canine Parainfluenza",
            type = PetType.DOG,
            description = "Respiratory virus vaccine",
            isCore = false,
            isBooster = false,
            recommendedAge = "6-8 weeks",
            frequency = "Annually"
        )
    )

    val dogBoosterVaccines = listOf(
        VaccineInfo(
            name = "DHPP Booster",
            type = PetType.DOG,
            description = "Booster for Distemper, Hepatitis, Parainfluenza, Parvo",
            isCore = true,
            isBooster = true,
            recommendedAge = "1 year",
            frequency = "Every 1-3 years"
        ),
        VaccineInfo(
            name = "Rabies Booster",
            type = PetType.DOG,
            description = "Rabies booster",
            isCore = true,
            isBooster = true,
            recommendedAge = "1 year after first, then every 1-3 years",
            frequency = "Every 1-3 years depending on local laws"
        )
    )

    // ========== CAT VACCINES ==========
    val catCoreVaccines = listOf(
        VaccineInfo(
            name = "FVRCP (Feline Viral Rhinotracheitis, Calicivirus, Panleukopenia)",
            type = PetType.CAT,
            description = "Combination vaccine for respiratory diseases and panleukopenia",
            isCore = true,
            isBooster = false,
            recommendedAge = "6-8 weeks",
            frequency = "Every 3-4 weeks until 16 weeks"
        ),
        VaccineInfo(
            name = "Rabies",
            type = PetType.CAT,
            description = "Rabies virus vaccine",
            isCore = true,
            isBooster = false,
            recommendedAge = "12-16 weeks",
            frequency = "Booster 1 year later, then every 1-3 years"
        )
    )

    val catNonCoreVaccines = listOf(
        VaccineInfo(
            name = "FeLV (Feline Leukemia)",
            type = PetType.CAT,
            description = "Protects against Feline Leukemia virus",
            isCore = false,
            isBooster = false,
            recommendedAge = "8-9 weeks",
            frequency = "Booster 3-4 weeks later, then annually"
        ),
        VaccineInfo(
            name = "FIV (Feline Immunodeficiency Virus)",
            type = PetType.CAT,
            description = "Protects against FIV",
            isCore = false,
            isBooster = false,
            recommendedAge = "8 weeks",
            frequency = "Annually for at-risk cats"
        ),
        VaccineInfo(
            name = "Chlamydia felis",
            type = PetType.CAT,
            description = "Protects against Chlamydia conjunctivitis",
            isCore = false,
            isBooster = false,
            recommendedAge = "9 weeks",
            frequency = "Annually"
        ),
        VaccineInfo(
            name = "Bordetella bronchiseptica",
            type = PetType.CAT,
            description = "Respiratory infection vaccine",
            isCore = false,
            isBooster = false,
            recommendedAge = "8 weeks",
            frequency = "Annually"
        )
    )

    val catBoosterVaccines = listOf(
        VaccineInfo(
            name = "FVRCP Booster",
            type = PetType.CAT,
            description = "Booster for FVRCP combination",
            isCore = true,
            isBooster = true,
            recommendedAge = "1 year",
            frequency = "Every 1-3 years"
        ),
        VaccineInfo(
            name = "Rabies Booster",
            type = PetType.CAT,
            description = "Rabies booster",
            isCore = true,
            isBooster = true,
            recommendedAge = "1 year after first, then every 1-3 years",
            frequency = "Every 1-3 years depending on local laws"
        )
    )

    fun getAllVaccinesByPetType(petType: String): List<VaccineInfo> {
        return when (petType.lowercase()) {
            "dog" -> dogCoreVaccines + dogNonCoreVaccines + dogBoosterVaccines
            "cat" -> catCoreVaccines + catNonCoreVaccines + catBoosterVaccines
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

    fun getBoosterVaccinesByPetType(petType: String): List<VaccineInfo> {
        return when (petType.lowercase()) {
            "dog" -> dogBoosterVaccines
            "cat" -> catBoosterVaccines
            else -> emptyList()
        }
    }

    fun getNonCoreVaccinesByPetType(petType: String): List<VaccineInfo> {
        return when (petType.lowercase()) {
            "dog" -> dogNonCoreVaccines
            "cat" -> catNonCoreVaccines
            else -> emptyList()
        }
    }
}