package com.domcheung.fittrackpro.di

import android.content.Context
import androidx.room.Room
import com.domcheung.fittrackpro.data.local.AppDatabase
import com.domcheung.fittrackpro.data.local.dao.ExerciseDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutPlanDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutSessionDao
import com.domcheung.fittrackpro.data.local.dao.PersonalRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies
 * Provides Room database and DAO instances
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the main Room database instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development - remove in production
            .build()
    }

    /**
     * Provides ExerciseDao instance
     */
    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    /**
     * Provides WorkoutPlanDao instance
     */
    @Provides
    fun provideWorkoutPlanDao(database: AppDatabase): WorkoutPlanDao {
        return database.workoutPlanDao()
    }

    /**
     * Provides WorkoutSessionDao instance
     */
    @Provides
    fun provideWorkoutSessionDao(database: AppDatabase): WorkoutSessionDao {
        return database.workoutSessionDao()
    }

    /**
     * Provides PersonalRecordDao instance
     */
    @Provides
    fun providePersonalRecordDao(database: AppDatabase): PersonalRecordDao {
        return database.personalRecordDao()
    }
}