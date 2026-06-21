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
        val servingGrams: Float,
    )

    private fun seedFoods(db: SupportSQLiteDatabase) {
        // 値はすべて一食分（servingGrams あたり）
        val foods = listOf(
            FoodSeed("ご飯（白米）茶碗1杯", 252f, 3.8f, 0.5f, 55.7f, 150f),
            FoodSeed("食パン6枚切り1枚", 158f, 5.4f, 2.6f, 28.0f, 60f),
            FoodSeed("卵1個", 76f, 6.2f, 5.2f, 0.2f, 50f),
            FoodSeed("鶏むね肉（皮なし）1食", 174f, 35.0f, 2.9f, 0.0f, 150f),
            FoodSeed("鶏もも肉（皮なし）1食", 191f, 33.0f, 5.9f, 0.0f, 150f),
            FoodSeed("豆腐（絹）半丁", 84f, 7.4f, 4.5f, 3.0f, 150f),
            FoodSeed("バナナ1本", 86f, 1.1f, 0.2f, 22.5f, 100f),
            FoodSeed("ヨーグルト（無糖）1個", 62f, 3.6f, 3.0f, 4.9f, 100f),
            FoodSeed("サーモン1切れ", 209f, 22.3f, 12.7f, 0.1f, 100f),
            FoodSeed("オートミール1食", 152f, 5.5f, 2.3f, 27.6f, 40f),
            FoodSeed("牛乳コップ1杯", 134f, 6.6f, 7.6f, 9.6f, 200f),
            FoodSeed("プロテイン（WPC）1杯", 114f, 21.0f, 1.8f, 3.6f, 30f),
        )
        foods.forEach { f ->
            db.execSQL(
                "INSERT INTO food (name, calories, protein, fat, carb, servingGrams, isFavorite) " +
                "VALUES ('${f.name}', ${f.kcal}, ${f.protein}, ${f.fat}, ${f.carb}, ${f.servingGrams}, 0)"
            )
        }
    }
}
