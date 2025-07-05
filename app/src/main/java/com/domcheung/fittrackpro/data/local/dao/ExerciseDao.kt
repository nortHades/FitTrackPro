package com.domcheung.fittrackpro.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.domcheung.fittrackpro.data.model.Exercise

/**
 * Data Access Object for Exercise entity
 * Handles all exercise-related database operations
 */
@Dao
interface ExerciseDao {

    // ========== Query Operations ==========

    /**
     * Get all exercises with live updates
     */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Get exercises by muscle group category
     */
    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY name ASC")
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    /**
     * Search exercises by name (case insensitive)
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Get exercise by ID
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Int): Exercise?

    /**
     * Get multiple exercises by IDs
     */
    @Query("SELECT * FROM exercises WHERE id IN (:exerciseIds)")
    suspend fun getExercisesByIds(exerciseIds: List<Int>): List<Exercise>

    /**
     * Get exercises by equipment type
     */
    @Query("SELECT * FROM exercises WHERE equipment LIKE '%' || :equipment || '%' ORDER BY name ASC")
    fun getExercisesByEquipment(equipment: String): Flow<List<Exercise>>

    /**
     * Get custom exercises created by user
     */
    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomExercises(): Flow<List<Exercise>>

    /**
     * Get exercises not synced to Firebase
     */
    @Query("SELECT * FROM exercises WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedExercises(): List<Exercise>

    /**
     * Get total exercise count
     */
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    /**
     * Check if exercise exists by ID
     */
    @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE id = :exerciseId)")
    suspend fun exerciseExists(exerciseId: Int): Boolean

    // ========== Insert Operations ==========

    /**
     * Insert single exercise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    /**
     * Insert multiple exercises (for API data sync)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    // ========== Update Operations ==========

    /**
     * Update existing exercise
     */
    @Update
    suspend fun updateExercise(exercise: Exercise)

    /**
     * Mark exercise as synced to Firebase
     */
    @Query("UPDATE exercises SET syncedToFirebase = 1 WHERE id = :exerciseId")
    suspend fun markAsSynced(exerciseId: Int)

    /**
     * Mark multiple exercises as synced
     */
    @Query("UPDATE exercises SET syncedToFirebase = 1 WHERE id IN (:exerciseIds)")
    suspend fun markMultipleAsSynced(exerciseIds: List<Int>)

    // ========== Delete Operations ==========

    /**
     * Delete exercise by ID
     */
    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: Int)

    /**
     * Delete custom exercise (only user-created exercises can be deleted)
     */
    @Query("DELETE FROM exercises WHERE id = :exerciseId AND isCustom = 1")
    suspend fun deleteCustomExercise(exerciseId: Int)

    /**
     * Clear all exercises (for fresh API sync)
     */
    @Query("DELETE FROM exercises WHERE isCustom = 0")
    suspend fun clearApiExercises()

    /**
     * Delete all exercises
     */
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()
}