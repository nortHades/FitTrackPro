package com.domcheung.fittrackpro.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.Converters
import com.domcheung.fittrackpro.data.local.dao.ExerciseDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutPlanDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutSessionDao
import com.domcheung.fittrackpro.data.local.dao.PersonalRecordDao

/**
 * FitTrack Pro Room Database
 * Contains all workout-related entities and provides DAOs
 */
@Database(
    entities = [
        Exercise::class,
        WorkoutPlan::class,
        WorkoutSession::class,
        PersonalRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)  // Use single unified converter
abstract class AppDatabase : RoomDatabase() {

    // DAO interfaces
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun personalRecordDao(): PersonalRecordDao

    companion object {
        const val DATABASE_NAME = "fittrack_pro_database"
    }
}