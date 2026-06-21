package com.example.slimyan.data

/** 曜日のビットマスク変換（dayIndex 0=月 .. 6=日）の純粋ロジック。 */
object DayMask {
    const val ALL = 0b1111111

    fun isSet(mask: Int, dayIndex: Int): Boolean = (mask and (1 shl dayIndex)) != 0

    fun toggle(mask: Int, dayIndex: Int): Int = mask xor (1 shl dayIndex)

    fun fromDays(days: Set<Int>): Int = days.fold(0) { acc, d -> acc or (1 shl d) }

    fun toDays(mask: Int): Set<Int> = (0..6).filter { isSet(mask, it) }.toSet()
}
