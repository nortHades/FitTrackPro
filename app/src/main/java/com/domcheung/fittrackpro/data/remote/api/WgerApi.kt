package com.domcheung.fittrackpro.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

/**
 * Wger API interface for fetching exercise data
 * Base URL: https://wger.de/api/v2/
 */
interface WgerApi {

    /**
     * Get all exercises with pagination
     * @param limit Number of exercises per page (default: 20)
     * @param offset Pagination offset (default: 0)
     * @param language Language code (default: 2 for English)
     */
    @GET("exercise/")
    suspend fun getExercises(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("language") language: Int = 2
    ): Response<WgerExerciseResponse>

    /**
     * Get specific exercise by ID
     * @param exerciseId Exercise ID from Wger database
     */
    @GET("exercise/{id}/")
    suspend fun getExerciseById(
        @Path("id") exerciseId: Int
    ): Response<WgerExerciseDetail>

    /**
     * Get exercise categories (muscle groups)
     */
    @GET("exercisecategory/")
    suspend fun getExerciseCategories(): Response<WgerCategoryResponse>

    /**
     * Get muscle groups
     */
    @GET("muscle/")
    suspend fun getMuscles(): Response<WgerMuscleResponse>

    /**
     * Get equipment types
     */
    @GET("equipment/")
    suspend fun getEquipment(): Response<WgerEquipmentResponse>

    /**
     * Search exercises by name
     * @param name Exercise name to search for
     * @param language Language code (default: 2 for English)
     */
    @GET("exercise/")
    suspend fun searchExercises(
        @Query("name") name: String,
        @Query("language") language: Int = 2
    ): Response<WgerExerciseResponse>

    /**
     * Get exercises by category
     * @param categoryId Category ID
     * @param language Language code (default: 2 for English)
     */
    @GET("exercise/")
    suspend fun getExercisesByCategory(
        @Query("category") categoryId: Int,
        @Query("language") language: Int = 2
    ): Response<WgerExerciseResponse>

    companion object {
        const val BASE_URL = "https://wger.de/api/v2/"

        // Language codes
        const val LANGUAGE_ENGLISH = 2
        const val LANGUAGE_GERMAN = 1
        const val LANGUAGE_SPANISH = 4

        // Common category IDs
        const val CATEGORY_ARMS = 8
        const val CATEGORY_LEGS = 9
        const val CATEGORY_ABS = 10
        const val CATEGORY_CHEST = 11
        const val CATEGORY_BACK = 12
        const val CATEGORY_SHOULDERS = 13
        const val CATEGORY_CALVES = 14
    }
}