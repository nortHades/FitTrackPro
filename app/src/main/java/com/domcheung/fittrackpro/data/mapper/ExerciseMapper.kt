package com.domcheung.fittrackpro.data.mapper

import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.data.remote.model.*

/**
 * Mapper to convert Wger API models to local database models
 */
object ExerciseMapper {

    /**
     * Convert Wger exercise to local Exercise entity
     */
    fun mapWgerExerciseToExercise(
        wgerExercise: WgerExercise,
        categoryName: String = "",
        muscleNames: List<String> = emptyList(),
        equipmentNames: List<String> = emptyList()
    ): Exercise {
        return Exercise(
            id = wgerExercise.id,
            name = wgerExercise.name.trim(),
            description = cleanDescription(wgerExercise.description),
            category = categoryName,
            muscles = muscleNames,
            secondaryMuscles = emptyList(), // Will be populated separately
            equipment = equipmentNames,
            imageUrl = null, // Will be populated from images if available
            videoUrl = null, // Will be populated from videos if available
            instructions = cleanDescription(wgerExercise.description),
            createdAt = System.currentTimeMillis(),
            isCustom = false
        )
    }

    /**
     * Convert detailed Wger exercise to local Exercise entity
     */
    fun mapWgerExerciseDetailToExercise(wgerDetail: WgerExerciseDetail): Exercise {
        val primaryMuscles = wgerDetail.muscles.map { it.name_en.ifEmpty { it.name } }
        val secondaryMuscles = wgerDetail.muscles_secondary.map { it.name_en.ifEmpty { it.name } }
        val equipment = wgerDetail.equipment.map { it.name }

        // Get main image URL
        val mainImage = wgerDetail.images.find { it.is_main }
        val imageUrl = mainImage?.image?.let {
            if (it.startsWith("http")) it else "https://wger.de$it"
        }

        // Get main video URL
        val mainVideo = wgerDetail.videos.find { it.is_main }
        val videoUrl = mainVideo?.video?.let {
            if (it.startsWith("http")) it else "https://wger.de$it"
        }

        return Exercise(
            id = wgerDetail.id,
            name = wgerDetail.name.trim(),
            description = cleanDescription(wgerDetail.description),
            category = getCategoryNameFromId(wgerDetail.category),
            muscles = primaryMuscles,
            secondaryMuscles = secondaryMuscles,
            equipment = equipment,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            instructions = cleanDescription(wgerDetail.description),
            createdAt = System.currentTimeMillis(),
            isCustom = false
        )
    }

    /**
     * Convert list of Wger exercises to local exercises
     */
    fun mapWgerExercisesToExercises(
        wgerExercises: List<WgerExercise>,
        categories: Map<Int, String> = emptyMap(),
        muscles: Map<Int, String> = emptyMap(),
        equipment: Map<Int, String> = emptyMap()
    ): List<Exercise> {
        return wgerExercises.map { wgerExercise ->
            val categoryName = categories[wgerExercise.category] ?: ""
            val muscleNames = wgerExercise.muscles.mapNotNull { muscles[it] }
            val equipmentNames = wgerExercise.equipment.mapNotNull { equipment[it] }

            mapWgerExerciseToExercise(
                wgerExercise = wgerExercise,
                categoryName = categoryName,
                muscleNames = muscleNames,
                equipmentNames = equipmentNames
            )
        }
    }

    /**
     * Clean HTML and formatting from description text
     */
    private fun cleanDescription(description: String): String {
        return description
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .trim()
    }

    /**
     * Get category name from category ID
     * This is a simplified mapping - in practice, you'd fetch from API
     */
    private fun getCategoryNameFromId(categoryId: Int): String {
        return when (categoryId) {
            8 -> "Arms"
            9 -> "Legs"
            10 -> "Abs"
            11 -> "Chest"
            12 -> "Back"
            13 -> "Shoulders"
            14 -> "Calves"
            15 -> "Cardio"
            else -> "Other"
        }
    }

    /**
     * Create lookup maps for efficient conversion
     */
    fun createCategoryLookupMap(categories: List<WgerCategory>): Map<Int, String> {
        return categories.associate { it.id to it.name }
    }

    fun createMuscleLookupMap(muscles: List<WgerMuscle>): Map<Int, String> {
        return muscles.associate { muscle ->
            muscle.id to (muscle.name_en.ifEmpty { muscle.name })
        }
    }

    fun createEquipmentLookupMap(equipment: List<WgerEquipment>): Map<Int, String> {
        return equipment.associate { it.id to it.name }
    }

    /**
     * Filter exercises by language and status
     */
    fun filterValidExercises(exercises: List<WgerExercise>): List<WgerExercise> {
        return exercises.filter { exercise ->
            exercise.name.isNotBlank() &&
                    exercise.language == 2 && // English only
                    exercise.id > 0
        }
    }

    /**
     * Group exercises by category for easier processing
     */
    fun groupExercisesByCategory(exercises: List<Exercise>): Map<String, List<Exercise>> {
        return exercises.groupBy { it.category }
    }

    /**
     * Extract unique muscle groups from exercises
     */
    fun extractUniqueMuscleGroups(exercises: List<Exercise>): Set<String> {
        return exercises.flatMap { it.muscles + it.secondaryMuscles }.toSet()
    }

    /**
     * Extract unique equipment types from exercises
     */
    fun extractUniqueEquipment(exercises: List<Exercise>): Set<String> {
        return exercises.flatMap { it.equipment }.toSet()
    }
}