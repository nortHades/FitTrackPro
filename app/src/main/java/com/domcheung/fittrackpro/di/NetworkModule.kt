package com.domcheung.fittrackpro.di

import com.domcheung.fittrackpro.data.remote.api.WgerApi
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.data.repository.WorkoutRepositoryImpl
import com.domcheung.fittrackpro.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies
 * Provides Retrofit, API interfaces, and Repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides OkHttpClient with logging and timeout configuration
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance configured for Wger API
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WgerApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides WgerApi interface
     */
    @Provides
    @Singleton
    fun provideWgerApi(retrofit: Retrofit): WgerApi {
        return retrofit.create(WgerApi::class.java)
    }

    /**
     * Provides WorkoutRepository implementation
     */
    @Provides
    @Singleton
    fun provideWorkoutRepository(
        exerciseDao: ExerciseDao,
        workoutPlanDao: WorkoutPlanDao,
        workoutSessionDao: WorkoutSessionDao,
        personalRecordDao: PersonalRecordDao,
        wgerApi: WgerApi
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(
            exerciseDao = exerciseDao,
            workoutPlanDao = workoutPlanDao,
            workoutSessionDao = workoutSessionDao,
            personalRecordDao = personalRecordDao,
            wgerApi = wgerApi
        )
    }
}