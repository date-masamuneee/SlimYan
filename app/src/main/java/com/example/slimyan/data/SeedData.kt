package com.example.slimyan.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object SeedData {
    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedExercises(db)
            seedFoods(db)
        }
    }

    private fun seedExercises(db: SupportSQLiteDatabase) {
        val exercises = listOf(
            "ベンチプレス" to "胸",
            "インクラインベンチプレス" to "胸",
            "スクワット" to "脚",
            "レッグプレス" to "脚",
            "デッドリフト" to "背中",
            "ラットプルダウン" to "背中",
            "チンニング" to "背中",
            "ショルダープレス" to "肩",
            "サイドレイズ" to "肩",
            "アームカール" to "腕",
            "トライセップスエクステンション" to "腕",
            "プランク" to "体幹",
            "クランチ" to "体幹",
        )
        exercises.forEach { (name, group) ->
            db.execSQL("INSERT INTO exercise (name, muscleGroup) VALUES ('$name', '$group')")
        }
    }

    private data class FoodSeed(
        val name: String,
        val kcal: Float,
        val protein: Float,
        val fat: Float,
        val carb: Float,
        val defaultGrams: Float,
    )

    private fun seedFoods(db: SupportSQLiteDatabase) {
        val foods = listOf(
            FoodSeed("ご飯（白米）", 168f, 2.5f, 0.3f, 37.1f, 150f),
            FoodSeed("食パン", 264f, 9.0f, 4.4f, 46.7f, 60f),
            FoodSeed("卵", 151f, 12.3f, 10.3f, 0.3f, 50f),
            FoodSeed("鶏むね肉（皮なし）", 116f, 23.3f, 1.9f, 0.0f, 150f),
            FoodSeed("鶏もも肉（皮なし）", 127f, 22.0f, 3.9f, 0.0f, 150f),
            FoodSeed("豆腐（絹）", 56f, 4.9f, 3.0f, 2.0f, 150f),
            FoodSeed("バナナ", 86f, 1.1f, 0.2f, 22.5f, 100f),
            FoodSeed("ヨーグルト（無糖）", 62f, 3.6f, 3.0f, 4.9f, 100f),
            FoodSeed("サーモン", 209f, 22.3f, 12.7f, 0.1f, 100f),
            FoodSeed("オートミール", 380f, 13.7f, 5.7f, 69.1f, 40f),
            FoodSeed("牛乳", 67f, 3.3f, 3.8f, 4.8f, 200f),
            FoodSeed("プロテインパウダー（WPC）", 380f, 70.0f, 6.0f, 12.0f, 30f),
        )
        foods.forEach { f ->
            db.execSQL(
                "INSERT INTO food (name, caloriesPer100g, proteinPer100g, fatPer100g, carbPer100g, defaultGrams, isFavorite) " +
                "VALUES ('${f.name}', ${f.kcal}, ${f.protein}, ${f.fat}, ${f.carb}, ${f.defaultGrams}, 0)"
            )
        }
    }
}
