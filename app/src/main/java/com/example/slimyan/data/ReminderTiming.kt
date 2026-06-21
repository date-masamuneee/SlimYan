package com.example.slimyan.data

import java.time.LocalDateTime

/** リマインドの曜日マスク判定・次回発火時刻の計算（純粋ロジック）。 */
object ReminderTiming {

    /** dayOfWeek: 1=月 .. 7=日。mask は bit0=月 .. bit6=日。 */
    fun shouldFireOn(mask: Int, dayOfWeek: Int): Boolean =
        (mask and (1 shl (dayOfWeek - 1))) != 0

    /** now より後の、最も近い発火時刻を返す。該当曜日が無ければ null。 */
    fun nextTrigger(mask: Int, hour: Int, minute: Int, now: LocalDateTime): LocalDateTime? {
        if (mask == 0) return null
        for (offset in 0..7) {
            val candidate = now.toLocalDate().plusDays(offset.toLong()).atTime(hour, minute)
            if (shouldFireOn(mask, candidate.dayOfWeek.value) && candidate.isAfter(now)) {
                return candidate
            }
        }
        return null
    }
}
