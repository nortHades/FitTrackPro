package com.domcheung.fittrackpro.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.domcheung.fittrackpro.data.model.WorkoutPlan

/**
 * Data Access Object for WorkoutPlan entity
 * Handles all workout plan-related database operations
 */
@Dao
interface WorkoutPlanDao {

    // ========== Query Operations ==========

    /**
     * Get all workout plans with live updates
     */
    @Query("SELECT * FROM workout_plans ORDER BY updatedAt DESC")
    fun getAllWorkoutPlans(): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans created by specific user
     */
    @Query("SELECT * FROM workout_plans WHERE createdBy = :userId ORDER BY updatedAt DESC")
    fun getUserWorkoutPlans(userId: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plan by ID
     */
    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    suspend fun getWorkoutPlanById(planId: String): WorkoutPlan?

    /**
     * Get workout plan by ID with live updates
     */
    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    fun getWorkoutPlanByIdFlow(planId: String): Flow<WorkoutPlan?>

    /**
     * Get template workout plans (reusable plans)
     */
    @Query("SELECT * FROM workout_plans WHERE isTemplate = 1 ORDER BY updatedAt DESC")
    fun getTemplateWorkoutPlans(): Flow<List<WorkoutPlan>>

    /**
     * Search workout plans by name
     */
    @Query("SELECT * FROM workout_plans WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchWorkoutPlans(query: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans by target muscle groups
     */
    @Query("SELECT * FROM workout_plans WHERE targetMuscleGroups LIKE '%' || :muscleGroup || '%' ORDER BY updatedAt DESC")
    fun getWorkoutPlansByMuscleGroup(muscleGroup: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans by tags
     */
    @Query("SELECT * FROM workout_plans WHERE tags LIKE '%' || :tag || '%' ORDER BY updatedAt DESC")
    fun getWorkoutPlansByTag(tag: String): Flow<List<WorkoutPlan>>

    /**
     * Get recently created workout plans
     */
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentWorkoutPlans(limit: Int = 5): Flow<List<WorkoutPlan>>

    /**
     * Get frequently used workout plans (based on session count)
     * This is a simplified version - in real implementation, you'd join with workout_sessions
     */
    @Query("SELECT * FROM workout_plans ORDER BY updatedAt DESC LIMIT :limit")
    fun getPopularWorkoutPlans(limit: Int = 5): Flow<List<WorkoutPlan>>

    /**
     * Check if workout plan exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM workout_plans WHERE id = :planId)")
    suspend fun workoutPlanExists(planId: String): Boolean

    /**
     * Get total workout plan count for user
     */
    @Query("SELECT COUNT(*) FROM workout_plans WHERE createdBy = :userId")
    suspend fun getUserWorkoutPlanCount(userId: String): Int

    // ========== Insert Operations ==========

    /**
     * Insert single workout plan
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan)

    /**
     * Insert multiple workout plans
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlans(workoutPlans: List<WorkoutPlan>)

    // ========== Update Operations ==========

    /**
     * Update existing workout plan
     */
    @Update
    suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan)

    /**
     * Update workout plan's updated timestamp
     */
    @Query("UPDATE workout_plans SET updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateTimestamp(planId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update workout plan name
     */
    @Query("UPDATE workout_plans SET name = :newName, updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateWorkoutPlanName(
        planId: String,
        newName: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update workout plan description
     */
    @Query("UPDATE workout_plans SET description = :newDescription, updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateWorkoutPlanDescription(
        planId: String,
        newDescription: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Toggle template status
     */
    @Query("UPDATE workout_plans SET isTemplate = :isTemplate, updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateTemplateStatus(
        planId: String,
        isTemplate: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    // ========== Delete Operations ==========

    /**
     * Delete workout plan by ID
     */
    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deleteWorkoutPlanById(planId: String)

    /**
     * Delete workout plans created by user
     */
    @Query("DELETE FROM workout_plans WHERE createdBy = :userId")
    suspend fun deleteUserWorkoutPlans(userId: String)

    /**
     * Delete all workout plans
     */
    @Query("DELETE FROM workout_plans")
    suspend fun deleteAllWorkoutPlans()

    // ========== Complex Queries ==========

    /**
     * Get workout plans with exercise count
     * Note: This is a simplified version. In practice, you might want to create a view or use @Embedded
     */
    @Query("""
        SELECT wp.*, 
               (LENGTH(wp.exercises) - LENGTH(REPLACE(wp.exercises, 'exerciseId', ''))) / LENGTH('exerciseId') as exerciseCount
        FROM workout_plans wp 
        WHERE wp.createdBy = :userId 
        ORDER BY wp.updatedAt DESC
    """)
    suspend fun getWorkoutPlansWithExerciseCount(userId: String): List<WorkoutPlan>

    /**
     * Get workout plans by duration range
     */
    @Query("SELECT * FROM workout_plans WHERE estimatedDuration BETWEEN :minDuration AND :maxDuration ORDER BY estimatedDuration ASC")
    fun getWorkoutPlansByDuration(minDuration: Int, maxDuration: Int): Flow<List<WorkoutPlan>>
}