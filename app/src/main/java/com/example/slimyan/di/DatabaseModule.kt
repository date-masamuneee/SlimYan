package com.example.slimyan.di

import android.content.Context
import androidx.room.Room
import com.example.slimyan.data.AppDatabase
import com.example.slimyan.data.MIGRATION_2_3
import com.example.slimyan.data.MIGRATION_3_4
import com.example.slimyan.data.MIGRATION_4_5
import com.example.slimyan.data.SeedData
import com.example.slimyan.data.dao.BodyWeightDao
import com.example.slimyan.data.dao.ExerciseDao
import com.example.slimyan.data.dao.FoodDao
import com.example.slimyan.data.dao.MealEntryDao
import com.example.slimyan.data.dao.MealTemplateDao
import com.example.slimyan.data.dao.ReminderDao
import com.example.slimyan.data.dao.SetEntryDao
import com.example.slimyan.data.dao.UserProfileDao
import com.example.slimyan.data.dao.WorkoutProgramDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "slimyan.db")
            .addCallback(SeedData.callback)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMealTemplateDao(db: AppDatabase): MealTemplateDao = db.mealTemplateDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
    @Provides fun provideWorkoutProgramDao(db: AppDatabase): WorkoutProgramDao = db.workoutProgramDao()

    @Provides fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideBodyWeightDao(db: AppDatabase): BodyWeightDao = db.bodyWeightDao()
    @Provides fun provideFoodDao(db: AppDatabase): FoodDao = db.foodDao()
    @Provides fun provideMealEntryDao(db: AppDatabase): MealEntryDao = db.mealEntryDao()
    @Provides fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideSetEntryDao(db: AppDatabase): SetEntryDao = db.setEntryDao()
}
