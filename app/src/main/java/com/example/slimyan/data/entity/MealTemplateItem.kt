package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 週間メニューテンプレの1品（曜日 × 食事枠 × 品目）。 */
@Entity(tableName = "meal_template_item")
data class MealTemplateItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,         // 1=月 .. 7=日
    val mealSlot: String,       // "breakfast" | "lunch" | "dinner" | "snack"
    val foodId: Long,           // マイ食品参照。kcal/PFCは展開・表示時に Food から算出
    val grams: Float,
    val sortOrder: Int = 0,
)
