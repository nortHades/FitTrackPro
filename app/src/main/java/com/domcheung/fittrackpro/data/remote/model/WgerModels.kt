package com.domcheung.fittrackpro.data.remote.model

import kotlinx.serialization.Serializable

/**
 * Wger API response models
 * These match the JSON structure returned by Wger API
 */

/**
 * Main exercise list response from Wger API
 */
@Serializable
data class WgerExerciseResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<WgerExercise>
)

/**
 * Individual exercise from Wger API
 */
@Serializable
data class WgerExercise(
    val id: Int,
    val uuid: String,
    val name: String,
    val description: String = "",
    val creation_date: String,
    val category: Int,
    val muscles: List<Int> = emptyList(),
    val muscles_secondary: List<Int> = emptyList(),
    val equipment: List<Int> = emptyList(),
    val language: Int,
    val license: Int,
    val license_author: String = "",
    val variations: List<Int> = emptyList()
)

/**
 * Detailed exercise information (when fetching by ID)
 */
@Serializable
data class WgerExerciseDetail(
    val id: Int,
    val uuid: String,
    val name: String,
    val description: String = "",
    val creation_date: String,
    val category: Int,
    val muscles: List<WgerMuscle> = emptyList(),
    val muscles_secondary: List<WgerMuscle> = emptyList(),
    val equipment: List<WgerEquipment> = emptyList(),
    val language: Int,
    val license: Int,
    val license_author: String = "",
    val variations: List<Int> = emptyList(),
    val images: List<WgerExerciseImage> = emptyList(),
    val videos: List<WgerExerciseVideo> = emptyList()
)

/**
 * Exercise category response
 */
@Serializable
data class WgerCategoryResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<WgerCategory>
)

/**
 * Exercise category (muscle group)
 */
@Serializable
data class WgerCategory(
    val id: Int,
    val name: String
)

/**
 * Muscle response
 */
@Serializable
data class WgerMuscleResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<WgerMuscle>
)

/**
 * Individual muscle
 */
@Serializable
data class WgerMuscle(
    val id: Int,
    val name: String,
    val name_en: String,
    val is_front: Boolean
)

/**
 * Equipment response
 */
@Serializable
data class WgerEquipmentResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<WgerEquipment>
)

/**
 * Exercise equipment
 */
@Serializable
data class WgerEquipment(
    val id: Int,
    val name: String
)

/**
 * Exercise image
 */
@Serializable
data class WgerExerciseImage(
    val id: Int,
    val uuid: String,
    val exercise: Int,
    val image: String,
    val is_main: Boolean,
    val status: String,
    val style: String
)

/**
 * Exercise video
 */
@Serializable
data class WgerExerciseVideo(
    val id: Int,
    val uuid: String,
    val exercise: Int,
    val video: String,
    val is_main: Boolean,
    val status: String,
    val codec: String,
    val duration: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val license: Int,
    val license_author: String = ""
)