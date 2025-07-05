package com.domcheung.fittrackpro.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.domcheung.fittrackpro.data.model.*
import com.domcheung.fittrackpro.data.local.dao.*
import com.domcheung.fittrackpro.data.remote.api.WgerApi
import com.domcheung.fittrackpro.data.mapper.ExerciseMapper
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Implementation of WorkoutRepository
 * Handles data operations between local database and remote API
 */
@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val personalRecordDao: PersonalRecordDao,
    private val wgerApi: WgerApi
) : WorkoutRepository {

    // ========== Exercise Operations ==========

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query)
    }

    override fun getExercisesByCategory(category: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByCategory(category)
    }

    override suspend fun getExerciseById(exerciseId: Int): Exercise? {
        return withContext(Dispatchers.IO) {
            exerciseDao.getExerciseById(exerciseId)
        }
    }

    override suspend fun syncExercisesFromApi(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if exercises already exist
                val currentCount = exerciseDao.getExerciseCount()
                if (currentCount > 0) {
                    return@withContext Result.success(Unit) // Already synced
                }

                // Fetch categories, muscles, and equipment first
                val categoriesResponse = wgerApi.getExerciseCategories()
                val musclesResponse = wgerApi.getMuscles()
                val equipmentResponse = wgerApi.getEquipment()

                if (!categoriesResponse.isSuccessful ||
                    !musclesResponse.isSuccessful ||
                    !equipmentResponse.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Failed to fetch exercise metadata")
                    )
                }

                val categories = ExerciseMapper.createCategoryLookupMap(
                    categoriesResponse.body()?.results ?: emptyList()
                )
                val muscles = ExerciseMapper.createMuscleLookupMap(
                    musclesResponse.body()?.results ?: emptyList()
                )
                val equipment = ExerciseMapper.createEquipmentLookupMap(
                    equipmentResponse.body()?.results ?: emptyList()
                )

                // Fetch exercises in batches
                var offset = 0
                val limit = 50
                val allExercises = mutableListOf<Exercise>()

                do {
                    val response = wgerApi.getExercises(limit = limit, offset = offset)
                    if (!response.isSuccessful) {
                        break
                    }

                    val exerciseResponse = response.body()
                    if (exerciseResponse != null) {
                        val validExercises = ExerciseMapper.filterValidExercises(
                            exerciseResponse.results
                        )
                        val mappedExercises = ExerciseMapper.mapWgerExercisesToExercises(
                            validExercises,
                            categories,
                            muscles,
                            equipment
                        )
                        allExercises.addAll(mappedExercises)
                        offset += limit
                    }
                } while (exerciseResponse?.next != null && allExercises.size < 500) // Limit total exercises

                // Insert exercises into database
                if (allExercises.isNotEmpty()) {
                    exerciseDao.insertExercises(allExercises)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createCustomExercise(exercise: Exercise): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val customExercise = exercise.copy(
                    isCustom = true,
                    createdAt = System.currentTimeMillis()
                )
                exerciseDao.insertExercise(customExercise)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateExercise(exercise: Exercise): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                exerciseDao.updateExercise(exercise)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteCustomExercise(exerciseId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                exerciseDao.deleteCustomExercise(exerciseId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ========== Workout Plan Operations ==========

    override fun getUserWorkoutPlans(userId: String): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getUserWorkoutPlans(userId)
    }

    override suspend fun getWorkoutPlanById(planId: String): WorkoutPlan? {
        return withContext(Dispatchers.IO) {
            workoutPlanDao.getWorkoutPlanById(planId)
        }
    }

    override fun getWorkoutPlanByIdFlow(planId: String): Flow<WorkoutPlan?> {
        return workoutPlanDao.getWorkoutPlanByIdFlow(planId)
    }

    override suspend fun createWorkoutPlan(workoutPlan: WorkoutPlan): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val planWithId = if (workoutPlan.id.isEmpty()) {
                    workoutPlan.copy(
                        id = UUID.randomUUID().toString(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    workoutPlan.copy(updatedAt = System.currentTimeMillis())
                }
                workoutPlanDao.insertWorkoutPlan(planWithId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val updatedPlan = workoutPlan.copy(updatedAt = System.currentTimeMillis())
                workoutPlanDao.updateWorkoutPlan(updatedPlan)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteWorkoutPlan(planId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                workoutPlanDao.deleteWorkoutPlanById(planId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun duplicateWorkoutPlan(planId: String, newName: String): Result<WorkoutPlan> {
        return withContext(Dispatchers.IO) {
            try {
                val originalPlan = workoutPlanDao.getWorkoutPlanById(planId)
                    ?: return@withContext Result.failure(Exception("Plan not found"))

                val newPlan = originalPlan.copy(
                    id = UUID.randomUUID().toString(),
                    name = newName,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                workoutPlanDao.insertWorkoutPlan(newPlan)
                Result.success(newPlan)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getTemplateWorkoutPlans(): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getTemplateWorkoutPlans()
    }

    override fun getRecentWorkoutPlans(userId: String, limit: Int): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getRecentWorkoutPlans(limit)
    }

    // ========== Workout Session Operations ==========

    override fun getUserWorkoutSessions(userId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getUserWorkoutSessions(userId)
    }

    override suspend fun getActiveWorkoutSession(userId: String): WorkoutSession? {
        return withContext(Dispatchers.IO) {
            workoutSessionDao.getActiveWorkoutSession(userId)
        }
    }

    override fun getActiveWorkoutSessionFlow(userId: String): Flow<WorkoutSession?> {
        return workoutSessionDao.getActiveWorkoutSessionFlow(userId)
    }

    override suspend fun startWorkoutSession(planId: String, userId: String): Result<WorkoutSession> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if there's already an active session
                val activeSession = workoutSessionDao.getActiveWorkoutSession(userId)
                if (activeSession != null) {
                    return@withContext Result.failure(
                        Exception("There is already an active workout session")
                    )
                }

                // Get the workout plan
                val plan = workoutPlanDao.getWorkoutPlanById(planId)
                    ?: return@withContext Result.failure(Exception("Workout plan not found"))

                // Create new workout session
                val session = WorkoutSession(
                    id = UUID.randomUUID().toString(),
                    planId = planId,
                    planName = plan.name,
                    originalPlan = plan,
                    currentPlan = plan,
                    userId = userId,
                    startTime = System.currentTimeMillis(),
                    status = WorkoutStatus.IN_PROGRESS,
                    exercises = plan.exercises.mapIndexed { index, plannedExercise ->
                        ExecutedExercise(
                            exerciseId = plannedExercise.exerciseId,
                            exerciseName = plannedExercise.exerciseName,
                            orderIndex = index,
                            plannedSets = plannedExercise.sets,
                            executedSets = emptyList(),
                            restBetweenSets = plannedExercise.restBetweenSets
                        )
                    }
                )

                workoutSessionDao.insertWorkoutSession(session)
                Result.success(session)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateWorkoutSession(workoutSession: WorkoutSession): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                workoutSessionDao.updateWorkoutSession(workoutSession)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun pauseWorkoutSession(sessionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                workoutSessionDao.updateWorkoutSessionStatus(sessionId, WorkoutStatus.PAUSED)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun resumeWorkoutSession(sessionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                workoutSessionDao.updateWorkoutSessionStatus(sessionId, WorkoutStatus.IN_PROGRESS)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun completeWorkoutSession(sessionId: String, endTime: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                    ?: return@withContext Result.failure(Exception("Session not found"))

                // Calculate completion percentage and total volume
                val completionPercentage = calculateCompletionPercentage(session)
                val totalVolume = calculateTotalVolume(session)

                // Update session
                val updatedSession = session.copy(
                    status = WorkoutStatus.COMPLETED,
                    endTime = endTime,
                    completionPercentage = completionPercentage,
                    totalVolume = totalVolume
                )

                workoutSessionDao.updateWorkoutSession(updatedSession)

                // Check for personal records
                checkPersonalRecordsFromSession(session)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun abandonWorkoutSession(sessionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                workoutSessionDao.updateWorkoutSessionStatus(sessionId, WorkoutStatus.ABANDONED)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateExerciseInSession(
        sessionId: String,
        exerciseIndex: Int,
        updatedExercise: ExecutedExercise
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                    ?: return@withContext Result.failure(Exception("Session not found"))

                val updatedExercises = session.exercises.toMutableList()
                if (exerciseIndex < updatedExercises.size) {
                    updatedExercises[exerciseIndex] = updatedExercise
                    val updatedSession = session.copy(exercises = updatedExercises)
                    workoutSessionDao.updateWorkoutSession(updatedSession)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun addSetToExercise(
        sessionId: String,
        exerciseIndex: Int,
        set: ExecutedSet
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                    ?: return@withContext Result.failure(Exception("Session not found"))

                val updatedExercises = session.exercises.toMutableList()
                if (exerciseIndex < updatedExercises.size) {
                    val exercise = updatedExercises[exerciseIndex]
                    val updatedSets = exercise.executedSets.toMutableList()
                    updatedSets.add(set)

                    updatedExercises[exerciseIndex] = exercise.copy(executedSets = updatedSets)
                    val updatedSession = session.copy(exercises = updatedExercises)
                    workoutSessionDao.updateWorkoutSession(updatedSession)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun completeSet(
        sessionId: String,
        exerciseIndex: Int,
        setIndex: Int,
        weight: Float,
        reps: Int,
        rpe: Float?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                    ?: return@withContext Result.failure(Exception("Session not found"))

                val updatedExercises = session.exercises.toMutableList()
                if (exerciseIndex < updatedExercises.size) {
                    val exercise = updatedExercises[exerciseIndex]
                    val updatedSets = exercise.executedSets.toMutableList()

                    if (setIndex < updatedSets.size) {
                        val set = updatedSets[setIndex]
                        updatedSets[setIndex] = set.copy(
                            actualWeight = weight,
                            actualReps = reps,
                            actualRpe = rpe,
                            isCompleted = true,
                            completedAt = System.currentTimeMillis()
                        )
                    }

                    updatedExercises[exerciseIndex] = exercise.copy(executedSets = updatedSets)
                    val updatedSession = session.copy(exercises = updatedExercises)
                    workoutSessionDao.updateWorkoutSession(updatedSession)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun replaceExerciseInSession(
        sessionId: String,
        exerciseIndex: Int,
        newExerciseId: Int
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                    ?: return@withContext Result.failure(Exception("Session not found"))

                val newExercise = exerciseDao.getExerciseById(newExerciseId)
                    ?: return@withContext Result.failure(Exception("Exercise not found"))

                val updatedExercises = session.exercises.toMutableList()
                if (exerciseIndex < updatedExercises.size) {
                    val oldExercise = updatedExercises[exerciseIndex]
                    val replacedExercise = ExecutedExercise(
                        exerciseId = newExerciseId,
                        exerciseName = newExercise.name,
                        orderIndex = oldExercise.orderIndex,
                        plannedSets = oldExercise.plannedSets,
                        executedSets = emptyList(), // Clear executed sets when replacing
                        restBetweenSets = oldExercise.restBetweenSets,
                        isReplaced = true,
                        replacedFromId = oldExercise.exerciseId
                    )

                    updatedExercises[exerciseIndex] = replacedExercise
                    val updatedSession = session.copy(exercises = updatedExercises)
                    workoutSessionDao.updateWorkoutSession(updatedSession)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getWorkoutSessionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getWorkoutSessionsByDateRange(userId, startDate, endDate)
    }

    override fun getCompletedWorkoutSessions(userId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getCompletedWorkoutSessions(userId)
    }

    // ========== Personal Record Operations ==========

    override fun getUserPersonalRecords(userId: String): Flow<List<PersonalRecord>> {
        return personalRecordDao.getUserPersonalRecords(userId)
    }

    override fun getPersonalRecordsByExercise(userId: String, exerciseId: Int): Flow<List<PersonalRecord>> {
        return personalRecordDao.getPersonalRecordsByExercise(userId, exerciseId)
    }

    override suspend fun checkAndCreatePersonalRecord(
        userId: String,
        exerciseId: Int,
        weight: Float,
        reps: Int,
        sessionId: String
    ): Result<PersonalRecord?> {
        return withContext(Dispatchers.IO) {
            try {
                val exercise = exerciseDao.getExerciseById(exerciseId)
                    ?: return@withContext Result.failure(Exception("Exercise not found"))

                // Calculate 1RM using Epley formula
                val oneRepMax = if (reps == 1) weight else weight * (1 + reps / 30f)
                val volume = weight * reps

                // Check different record types
                val recordTypes = mutableListOf<RecordType>()

                // Check max weight record
                val currentMaxWeight = personalRecordDao.getBestWeightRecord(userId, exerciseId)
                if (currentMaxWeight == null || weight > currentMaxWeight.weight) {
                    recordTypes.add(RecordType.MAX_WEIGHT)
                }

                // Check max reps record (for same or higher weight)
                val currentMaxReps = personalRecordDao.getBestRepRecord(userId, exerciseId)
                if (currentMaxReps == null ||
                    (weight >= currentMaxReps.weight && reps > currentMaxReps.reps)) {
                    recordTypes.add(RecordType.MAX_REPS)
                }

                // Check max volume record
                val currentMaxVolume = personalRecordDao.getBestVolumeRecord(userId, exerciseId)
                if (currentMaxVolume == null || volume > currentMaxVolume.volume) {
                    recordTypes.add(RecordType.MAX_VOLUME)
                }

                // Check max 1RM record
                val current1RM = personalRecordDao.getBest1RMRecord(userId, exerciseId)
                if (current1RM == null || oneRepMax > current1RM.oneRepMax) {
                    recordTypes.add(RecordType.MAX_ONE_REP_MAX)
                }

                // Create personal records for applicable types
                var latestRecord: PersonalRecord? = null
                recordTypes.forEach { recordType ->
                    val record = PersonalRecord(
                        id = UUID.randomUUID().toString(),
                        exerciseId = exerciseId,
                        exerciseName = exercise.name,
                        userId = userId,
                        recordType = recordType,
                        weight = weight,
                        reps = reps,
                        oneRepMax = oneRepMax,
                        volume = volume,
                        achievedAt = System.currentTimeMillis(),
                        sessionId = sessionId
                    )
                    personalRecordDao.insertPersonalRecord(record)
                    latestRecord = record
                }

                Result.success(latestRecord)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getBestPersonalRecord(
        userId: String,
        exerciseId: Int,
        recordType: RecordType
    ): PersonalRecord? {
        return withContext(Dispatchers.IO) {
            when (recordType) {
                RecordType.MAX_WEIGHT -> personalRecordDao.getBestWeightRecord(userId, exerciseId)
                RecordType.MAX_REPS -> personalRecordDao.getBestRepRecord(userId, exerciseId)
                RecordType.MAX_VOLUME -> personalRecordDao.getBestVolumeRecord(userId, exerciseId)
                RecordType.MAX_ONE_REP_MAX -> personalRecordDao.getBest1RMRecord(userId, exerciseId)
            }
        }
    }

    override fun getRecentPersonalRecords(userId: String, days: Int): Flow<List<PersonalRecord>> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return personalRecordDao.getRecentPersonalRecords(userId, cutoffTime)
    }

    // ========== Statistics and Analytics ==========

    override suspend fun getWorkoutStatistics(userId: String): WorkoutStatistics {
        return withContext(Dispatchers.IO) {
            val totalWorkouts = workoutSessionDao.getUserWorkoutSessionCount(userId)
            val completedWorkouts = workoutSessionDao.getUserCompletedWorkoutSessionCount(userId)
            val totalWorkoutTime = workoutSessionDao.getTotalWorkoutTime(userId) ?: 0L
            val averageWorkoutDuration = workoutSessionDao.getAverageWorkoutDuration(userId)?.toLong() ?: 0L
            val totalVolume = workoutSessionDao.getTotalVolume(userId) ?: 0f
            val averageVolume = if (completedWorkouts > 0) totalVolume / completedWorkouts else 0f
            val personalRecordCount = personalRecordDao.getUserPersonalRecordCount(userId)

            // Calculate workout frequency (last 4 weeks)
            val fourWeeksAgo = System.currentTimeMillis() - (4 * 7 * 24 * 60 * 60 * 1000L)
            val recentWorkouts = workoutSessionDao.getWorkoutFrequency(userId, fourWeeksAgo, System.currentTimeMillis())
            val workoutFrequency = recentWorkouts / 4f

            WorkoutStatistics(
                totalWorkouts = totalWorkouts,
                completedWorkouts = completedWorkouts,
                totalWorkoutTime = totalWorkoutTime,
                averageWorkoutDuration = averageWorkoutDuration,
                totalVolume = totalVolume,
                averageVolume = averageVolume,
                workoutFrequency = workoutFrequency,
                personalRecordCount = personalRecordCount
            )
        }
    }

    override suspend fun getExerciseProgress(userId: String, exerciseId: Int): List<PersonalRecord> {
        return withContext(Dispatchers.IO) {
            personalRecordDao.getWeightProgression(userId, exerciseId)
        }
    }

    override suspend fun getTotalWorkoutTime(userId: String): Long {
        return withContext(Dispatchers.IO) {
            workoutSessionDao.getTotalWorkoutTime(userId) ?: 0L
        }
    }

    override suspend fun getWorkoutFrequency(userId: String, weeks: Int): Float {
        return withContext(Dispatchers.IO) {
            val weeksAgo = System.currentTimeMillis() - (weeks * 7 * 24 * 60 * 60 * 1000L)
            val workoutCount = workoutSessionDao.getWorkoutFrequency(userId, weeksAgo, System.currentTimeMillis())
            workoutCount.toFloat() / weeks
        }
    }

    override suspend fun getTotalVolume(userId: String): Float {
        return withContext(Dispatchers.IO) {
            workoutSessionDao.getTotalVolume(userId) ?: 0f
        }
    }

    // ========== Data Sync Operations ==========

    override suspend fun syncToFirebase(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement Firebase sync
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshAllData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                syncExercisesFromApi()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun clearAllData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                personalRecordDao.deleteAllPersonalRecords()
                workoutSessionDao.deleteAllWorkoutSessions()
                workoutPlanDao.deleteAllWorkoutPlans()
                exerciseDao.deleteAllExercises()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ========== Private Helper Methods ==========

    private fun calculateCompletionPercentage(session: WorkoutSession): Float {
        val totalPlannedSets = session.exercises.sumOf { it.plannedSets.size }
        val completedSets = session.exercises.sumOf { exercise ->
            exercise.executedSets.count { it.isCompleted }
        }
        return if (totalPlannedSets > 0) {
            (completedSets.toFloat() / totalPlannedSets) * 100f
        } else 0f
    }

    private fun calculateTotalVolume(session: WorkoutSession): Float {
        return session.exercises.sumOf { exercise ->
            exercise.executedSets.filter { it.isCompleted }.sumOf { set ->
                (set.actualWeight * set.actualReps).toDouble()
            }
        }.toFloat()
    }

    private suspend fun checkPersonalRecordsFromSession(session: WorkoutSession) {
        session.exercises.forEach { exercise ->
            exercise.executedSets.filter { it.isCompleted }.forEach { set ->
                checkAndCreatePersonalRecord(
                    userId = session.userId,
                    exerciseId = exercise.exerciseId,
                    weight = set.actualWeight,
                    reps = set.actualReps,
                    sessionId = session.id
                )
            }
        }
    }
}