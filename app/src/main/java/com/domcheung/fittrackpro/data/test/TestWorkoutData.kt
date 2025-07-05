package com.domcheung.fittrackpro.data.test

import com.domcheung.fittrackpro.data.model.*
import java.util.UUID

/**
 * Test data provider for workout-related entities
 * Used for testing database functionality and UI development
 */
object TestWorkoutData {

    /**
     * Sample exercises for testing
     */
    fun getSampleExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = 1,
                name = "Bench Press",
                description = "Lie on a bench and press the barbell up from your chest",
                category = "Chest",
                muscles = listOf("Chest", "Triceps", "Shoulders"),
                secondaryMuscles = listOf("Core"),
                equipment = listOf("Barbell", "Bench"),
                instructions = "Lie flat on bench. Grip bar slightly wider than shoulder width. Lower bar to chest, then press up.",
                isCustom = false
            ),
            Exercise(
                id = 2,
                name = "Squat",
                description = "Stand with feet shoulder-width apart and squat down",
                category = "Legs",
                muscles = listOf("Quadriceps", "Glutes"),
                secondaryMuscles = listOf("Hamstrings", "Calves"),
                equipment = listOf("Barbell"),
                instructions = "Stand with feet shoulder-width apart. Lower body by bending knees and hips. Return to starting position.",
                isCustom = false
            ),
            Exercise(
                id = 3,
                name = "Deadlift",
                description = "Lift a loaded barbell from the ground to hip level",
                category = "Back",
                muscles = listOf("Hamstrings", "Glutes", "Lower Back"),
                secondaryMuscles = listOf("Traps", "Forearms"),
                equipment = listOf("Barbell"),
                instructions = "Stand with feet hip-width apart. Bend at hips and knees to grab bar. Lift by extending hips and knees.",
                isCustom = false
            ),
            Exercise(
                id = 4,
                name = "Pull-ups",
                description = "Hang from a bar and pull your body up",
                category = "Back",
                muscles = listOf("Lats", "Rhomboids"),
                secondaryMuscles = listOf("Biceps", "Rear Delts"),
                equipment = listOf("Pull-up Bar"),
                instructions = "Hang from bar with overhand grip. Pull body up until chin is over bar. Lower with control.",
                isCustom = false
            ),
            Exercise(
                id = 5,
                name = "Push-ups",
                description = "Classic bodyweight chest exercise",
                category = "Chest",
                muscles = listOf("Chest", "Triceps"),
                secondaryMuscles = listOf("Shoulders", "Core"),
                equipment = emptyList(),
                instructions = "Start in plank position. Lower body until chest nearly touches ground. Push back up.",
                isCustom = false
            )
        )
    }

    /**
     * Sample workout plan for testing
     */
    fun getSampleWorkoutPlan(userId: String): WorkoutPlan {
        return WorkoutPlan(
            id = UUID.randomUUID().toString(),
            name = "Upper Body Strength",
            description = "Focus on building upper body strength with compound movements",
            targetMuscleGroups = listOf("Chest", "Back", "Shoulders", "Arms"),
            estimatedDuration = 45,
            exercises = listOf(
                PlannedExercise(
                    exerciseId = 1,
                    exerciseName = "Bench Press",
                    orderIndex = 0,
                    sets = listOf(
                        PlannedSet(setNumber = 1, targetWeight = 60f, targetReps = 8),
                        PlannedSet(setNumber = 2, targetWeight = 65f, targetReps = 6),
                        PlannedSet(setNumber = 3, targetWeight = 70f, targetReps = 4)
                    ),
                    restBetweenSets = 120
                ),
                PlannedExercise(
                    exerciseId = 4,
                    exerciseName = "Pull-ups",
                    orderIndex = 1,
                    sets = listOf(
                        PlannedSet(setNumber = 1, targetWeight = 0f, targetReps = 8),
                        PlannedSet(setNumber = 2, targetWeight = 0f, targetReps = 6),
                        PlannedSet(setNumber = 3, targetWeight = 0f, targetReps = 5)
                    ),
                    restBetweenSets = 90
                ),
                PlannedExercise(
                    exerciseId = 5,
                    exerciseName = "Push-ups",
                    orderIndex = 2,
                    sets = listOf(
                        PlannedSet(setNumber = 1, targetWeight = 0f, targetReps = 15),
                        PlannedSet(setNumber = 2, targetWeight = 0f, targetReps = 12),
                        PlannedSet(setNumber = 3, targetWeight = 0f, targetReps = 10)
                    ),
                    restBetweenSets = 60
                )
            ),
            createdBy = userId,
            isTemplate = true,
            tags = listOf("Upper Body", "Strength", "Beginner")
        )
    }

    /**
     * Sample personal records for testing
     */
    fun getSamplePersonalRecords(userId: String): List<PersonalRecord> {
        return listOf(
            PersonalRecord(
                id = UUID.randomUUID().toString(),
                exerciseId = 1,
                exerciseName = "Bench Press",
                userId = userId,
                recordType = RecordType.MAX_WEIGHT,
                weight = 80f,
                reps = 1,
                oneRepMax = 80f,
                volume = 80f,
                achievedAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
                sessionId = UUID.randomUUID().toString()
            ),
            PersonalRecord(
                id = UUID.randomUUID().toString(),
                exerciseId = 2,
                exerciseName = "Squat",
                userId = userId,
                recordType = RecordType.MAX_WEIGHT,
                weight = 100f,
                reps = 5,
                oneRepMax = 112.5f,
                volume = 500f,
                achievedAt = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                sessionId = UUID.randomUUID().toString()
            ),
            PersonalRecord(
                id = UUID.randomUUID().toString(),
                exerciseId = 4,
                exerciseName = "Pull-ups",
                userId = userId,
                recordType = RecordType.MAX_REPS,
                weight = 0f,
                reps = 12,
                oneRepMax = 0f,
                volume = 0f,
                achievedAt = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000), // 1 day ago
                sessionId = UUID.randomUUID().toString()
            )
        )
    }
}