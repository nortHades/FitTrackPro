package com.domcheung.fittrackpro.data.repository

import kotlinx.coroutines.flow.Flow
import com.domcheung.fittrackpro.data.model.*

/**
 * Repository interface for workout-related operations
 * Defines business capabilities for exercises, plans, sessions, and records
 */
interface WorkoutRepository {

    // ========== Exercise Operations ==========

    /**
     * Get all exercises with live updates
     */
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Search exercises by name
     */
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Get exercises by category
     */
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    /**
     * Get exercise by ID
     */
    suspend fun getExerciseById(exerciseId: Int): Exercise?

    /**
     * Sync exercises from Wger API
     */
    suspend fun syncExercisesFromApi(): Result<Unit>

    /**
     * Create custom exercise
     */
    suspend fun createCustomExercise(exercise: Exercise): Result<Unit>

    /**
     * Update existing exercise
     */
    suspend fun updateExercise(exercise: Exercise): Result<Unit>

    /**
     * Delete custom exercise
     */
    suspend fun deleteCustomExercise(exerciseId: Int): Result<Unit>

    // ========== Workout Plan Operations ==========

    /**
     * Get all workout plans for user
     */
    fun getUserWorkoutPlans(userId: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plan by ID
     */
    suspend fun getWorkoutPlanById(planId: String): WorkoutPlan?

    /**
     * Get workout plan by ID with live updates
     */
    fun getWorkoutPlanByIdFlow(planId: String): Flow<WorkoutPlan?>

    /**
     * Create new workout plan
     */
    suspend fun createWorkoutPlan(workoutPlan: WorkoutPlan): Result<Unit>

    /**
     * Update existing workout plan
     */
    suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan): Result<Unit>

    /**
     * Delete workout plan
     */
    suspend fun deleteWorkoutPlan(planId: String): Result<Unit>

    /**
     * Duplicate workout plan
     */
    suspend fun duplicateWorkoutPlan(planId: String, newName: String): Result<WorkoutPlan>

    /**
     * Get template workout plans
     */
    fun getTemplateWorkoutPlans(): Flow<List<WorkoutPlan>>

    /**
     * Get recent workout plans
     */
    fun getRecentWorkoutPlans(userId: String, limit: Int = 5): Flow<List<WorkoutPlan>>

    // ========== Workout Session Operations ==========

    /**
     * Get workout sessions for user
     */
    fun getUserWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    /**
     * Get current active workout session
     */
    suspend fun getActiveWorkoutSession(userId: String): WorkoutSession?

    /**
     * Get active workout session with live updates
     */
    fun getActiveWorkoutSessionFlow(userId: String): Flow<WorkoutSession?>

    /**
     * Start new workout session
     */
    suspend fun startWorkoutSession(
        planId: String,
        userId: String
    ): Result<WorkoutSession>

    /**
     * Update workout session
     */
    suspend fun updateWorkoutSession(workoutSession: WorkoutSession): Result<Unit>

    /**
     * Pause workout session
     */
    suspend fun pauseWorkoutSession(sessionId: String): Result<Unit>

    /**
     * Resume workout session
     */
    suspend fun resumeWorkoutSession(sessionId: String): Result<Unit>

    /**
     * Complete workout session
     */
    suspend fun completeWorkoutSession(
        sessionId: String,
        endTime: Long = System.currentTimeMillis()
    ): Result<Unit>

    /**
     * Abandon workout session
     */
    suspend fun abandonWorkoutSession(sessionId: String): Result<Unit>

    /**
     * Update exercise in session (for real-time modifications)
     */
    suspend fun updateExerciseInSession(
        sessionId: String,
        exerciseIndex: Int,
        updatedExercise: ExecutedExercise
    ): Result<Unit>

    /**
     * Add set to exercise in session
     */
    suspend fun addSetToExercise(
        sessionId: String,
        exerciseIndex: Int,
        set: ExecutedSet
    ): Result<Unit>

    /**
     * Complete set in session
     */
    suspend fun completeSet(
        sessionId: String,
        exerciseIndex: Int,
        setIndex: Int,
        weight: Float,
        reps: Int,
        rpe: Float? = null
    ): Result<Unit>

    /**
     * Replace exercise in session
     */
    suspend fun replaceExerciseInSession(
        sessionId: String,
        exerciseIndex: Int,
        newExerciseId: Int
    ): Result<Unit>

    /**
     * Get workout sessions by date range
     */
    fun getWorkoutSessionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkoutSession>>

    /**
     * Get completed workout sessions
     */
    fun getCompletedWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    // ========== Personal Record Operations ==========

    /**
     * Get personal records for user
     */
    fun getUserPersonalRecords(userId: String): Flow<List<PersonalRecord>>

    /**
     * Get personal records for specific exercise
     */
    fun getPersonalRecordsByExercise(userId: String, exerciseId: Int): Flow<List<PersonalRecord>>

    /**
     * Check and create personal record if applicable
     */
    suspend fun checkAndCreatePersonalRecord(
        userId: String,
        exerciseId: Int,
        weight: Float,
        reps: Int,
        sessionId: String
    ): Result<PersonalRecord?>

    /**
     * Get best personal record for exercise by type
     */
    suspend fun getBestPersonalRecord(
        userId: String,
        exerciseId: Int,
        recordType: RecordType
    ): PersonalRecord?

    /**
     * Get recent personal records
     */
    fun getRecentPersonalRecords(
        userId: String,
        days: Int = 30
    ): Flow<List<PersonalRecord>>

    // ========== Statistics and Analytics ==========

    /**
     * Get workout statistics for user
     */
    suspend fun getWorkoutStatistics(userId: String): WorkoutStatistics

    /**
     * Get exercise progress over time
     */
    suspend fun getExerciseProgress(
        userId: String,
        exerciseId: Int
    ): List<PersonalRecord>

    /**
     * Get total workout time for user
     */
    suspend fun getTotalWorkoutTime(userId: String): Long

    /**
     * Get workout frequency (sessions per week)
     */
    suspend fun getWorkoutFrequency(
        userId: String,
        weeks: Int = 4
    ): Float

    /**
     * Get total volume lifted
     */
    suspend fun getTotalVolume(userId: String): Float

    // ========== Data Sync Operations ==========

    /**
     * Sync local data to Firebase
     */
    suspend fun syncToFirebase(): Result<Unit>

    /**
     * Force refresh all data
     */
    suspend fun refreshAllData(): Result<Unit>

    /**
     * Clear all local data
     */
    suspend fun clearAllData(): Result<Unit>
}

/**
 * Workout statistics data class
 */
data class WorkoutStatistics(
    val totalWorkouts: Int,
    val completedWorkouts: Int,
    val totalWorkoutTime: Long, // in milliseconds
    val averageWorkoutDuration: Long, // in milliseconds
    val totalVolume: Float, // total weight lifted
    val averageVolume: Float, // average volume per workout
    val workoutFrequency: Float, // workouts per week
    val personalRecordCount: Int,
    val favoriteExercise: String? = null,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0
)