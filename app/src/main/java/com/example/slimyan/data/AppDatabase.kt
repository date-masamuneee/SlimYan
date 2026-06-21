package com.example.slimyan.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.slimyan.data.dao.BodyWeightDao
import com.example.slimyan.data.dao.ExerciseDao
import com.example.slimyan.data.dao.FoodDao
import com.example.slimyan.data.dao.MealEntryDao
import com.example.slimyan.data.dao.MealTemplateDao
import com.example.slimyan.data.dao.SetEntryDao
import com.example.slimyan.data.dao.UserProfileDao
import com.example.slimyan.data.entity.BodyWeight
import com.example.slimyan.data.entity.Exercise
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealEntry
import com.example.slimyan.data.entity.MealTemplateItem
import com.example.slimyan.data.entity.SetEntry
import com.example.slimyan.data.entity.UserProfile

@Database(
    entities = [
        UserProfile::class,
        BodyWeight::class,
        Food::class,
        MealEntry::class,
        Exercise::class,
        SetEntry::class,
        MealTemplateItem::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun foodDao(): FoodDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setEntryDao(): SetEntryDao
    abstract fun mealTemplateDao(): MealTemplateDao
}
