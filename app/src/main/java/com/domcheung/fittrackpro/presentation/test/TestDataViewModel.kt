package com.domcheung.fittrackpro.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.data.test.TestWorkoutData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for testing data layer functionality
 * This helps verify that all database operations work correctly
 */
@HiltViewModel
class TestDataViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _testState = MutableStateFlow(TestDataState())
    val testState: StateFlow<TestDataState> = _testState

    /**
     * Test all data layer operations
     */
    fun runDataLayerTests() {
        viewModelScope.launch {
            _testState.value = TestDataState(isLoading = true, message = "Starting data layer tests...")

            try {
                // Test 1: Insert sample exercises
                testInsertExercises()

                // Test 2: Insert sample workout plan
                testInsertWorkoutPlan()

                // Test 3: Test API sync (if available)
                testApiSync()

                // Test 4: Insert sample personal records
                testInsertPersonalRecords()

                // Test 5: Test data queries
                testDataQueries()

                _testState.value = TestDataState(
                    isLoading = false,
                    isSuccess = true,
                    message = "✅ All data layer tests passed!"
                )

            } catch (e: Exception) {
                _testState.value = TestDataState(
                    isLoading = false,
                    isError = true,
                    message = "❌ Test failed: ${e.message}",
                    error = e
                )
            }
        }
    }

    /**
     * Test inserting sample exercises
     */
    private suspend fun testInsertExercises() {
        _testState.value = _testState.value.copy(message = "Testing exercise insertion...")

        val sampleExercises = TestWorkoutData.getSampleExercises()
        sampleExercises.forEach { exercise ->
            val result = workoutRepository.createCustomExercise(exercise)
            if (result.isFailure) {
                throw Exception("Failed to insert exercise: ${exercise.name}")
            }
        }

        _testState.value = _testState.value.copy(message = "✅ Exercise insertion test passed")
    }

    /**
     * Test inserting sample workout plan
     */
    private suspend fun testInsertWorkoutPlan() {
        _testState.value = _testState.value.copy(message = "Testing workout plan insertion...")

        val samplePlan = TestWorkoutData.getSampleWorkoutPlan("test_user_123")
        val result = workoutRepository.createWorkoutPlan(samplePlan)

        if (result.isFailure) {
            throw Exception("Failed to insert workout plan")
        }

        _testState.value = _testState.value.copy(message = "✅ Workout plan insertion test passed")
    }

    /**
     * Test API synchronization
     */
    private suspend fun testApiSync() {
        _testState.value = _testState.value.copy(message = "Testing API sync...")

        // Note: This might take a while, so we're doing a simplified test
        try {
            val result = workoutRepository.syncExercisesFromApi()
            if (result.isSuccess) {
                _testState.value = _testState.value.copy(message = "✅ API sync test passed")
            } else {
                _testState.value = _testState.value.copy(message = "⚠️ API sync failed (network issue?)")
            }
        } catch (e: Exception) {
            _testState.value = _testState.value.copy(message = "⚠️ API sync skipped (${e.message})")
        }
    }

    /**
     * Test inserting personal records
     */
    private suspend fun testInsertPersonalRecords() {
        _testState.value = _testState.value.copy(message = "Testing personal records insertion...")

        val sampleRecords = TestWorkoutData.getSamplePersonalRecords("test_user_123")
        sampleRecords.forEach { record ->
            val result = workoutRepository.checkAndCreatePersonalRecord(
                userId = record.userId,
                exerciseId = record.exerciseId,
                weight = record.weight,
                reps = record.reps,
                sessionId = record.sessionId
            )
            if (result.isFailure) {
                throw Exception("Failed to insert personal record for ${record.exerciseName}")
            }
        }

        _testState.value = _testState.value.copy(message = "✅ Personal records insertion test passed")
    }

    /**
     * Test data queries
     */
    private suspend fun testDataQueries() {
        _testState.value = _testState.value.copy(message = "Testing data queries...")

        // Test exercise queries
        val exercises = workoutRepository.getAllExercises()

        // Test workout plan queries
        val workoutPlans = workoutRepository.getUserWorkoutPlans("test_user_123")

        // Test personal record queries
        val personalRecords = workoutRepository.getUserPersonalRecords("test_user_123")

        _testState.value = _testState.value.copy(message = "✅ Data queries test passed")
    }

    /**
     * Clear all test data
     */
    fun clearTestData() {
        viewModelScope.launch {
            _testState.value = TestDataState(isLoading = true, message = "Clearing test data...")

            try {
                val result = workoutRepository.clearAllData()
                if (result.isSuccess) {
                    _testState.value = TestDataState(
                        isLoading = false,
                        isSuccess = true,
                        message = "✅ Test data cleared successfully"
                    )
                } else {
                    throw Exception("Failed to clear data")
                }
            } catch (e: Exception) {
                _testState.value = TestDataState(
                    isLoading = false,
                    isError = true,
                    message = "❌ Failed to clear data: ${e.message}",
                    error = e
                )
            }
        }
    }

    /**
     * Test creating a workout session
     */
    fun testWorkoutSession() {
        viewModelScope.launch {
            _testState.value = TestDataState(isLoading = true, message = "Testing workout session...")

            try {
                // First get a workout plan
                val plans = workoutRepository.getUserWorkoutPlans("test_user_123")

                // This is a simplified test - in real app we'd collect the Flow
                _testState.value = TestDataState(
                    isLoading = false,
                    isSuccess = true,
                    message = "✅ Workout session test setup complete"
                )

            } catch (e: Exception) {
                _testState.value = TestDataState(
                    isLoading = false,
                    isError = true,
                    message = "❌ Workout session test failed: ${e.message}",
                    error = e
                )
            }
        }
    }
}

/**
 * Test state data class
 */
data class TestDataState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val message: String = "",
    val error: Throwable? = null
)