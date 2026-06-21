package com.example.slimyan.data

import com.example.slimyan.data.entity.SetEntry

/** ダブルプログレッションの重量UP判定（純粋ロジック）。 */
object ProgressionAdvisor {

    /**
     * 直近セッションのセットのうち repCeiling 回に到達したものが targetSets 以上なら重量UP推奨。
     * @param lastSessionSets 直近1回のセッションのセット（呼び出し側で同日に絞る）
     */
    fun shouldIncreaseWeight(lastSessionSets: List<SetEntry>, targetSets: Int, repCeiling: Int): Boolean {
        if (targetSets <= 0) return false
        val qualifying = lastSessionSets.count { it.reps >= repCeiling }
        return qualifying >= targetSets
    }
}
