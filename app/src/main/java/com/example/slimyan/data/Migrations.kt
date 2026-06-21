package com.example.slimyan.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 加算的マイグレーション置き場。新規テーブル追加のみで既存データを保護する。
 * CREATE 文は Room が生成する期待スキーマと完全一致させること（不一致は実行時例外）。
 */

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `meal_template_item` (
                `id` INTEGER NOT NULL,
                `dayOfWeek` INTEGER NOT NULL,
                `mealSlot` TEXT NOT NULL,
                `foodId` INTEGER NOT NULL,
                `grams` REAL NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminder` (
                `id` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `mealSlot` TEXT,
                `label` TEXT NOT NULL,
                `hour` INTEGER NOT NULL,
                `minute` INTEGER NOT NULL,
                `daysOfWeekMask` INTEGER NOT NULL,
                `enabled` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
