package com.alrobyad.gpa5

import java.time.ZoneId
import java.time.ZonedDateTime

enum class RankTier(val title: String, val bnTitle: String, val minStars: Int) {
    BRONZE("Bronze", "ব্রোঞ্জ", 0),
    SILVER("Silver", "সিলভার", 10),
    GOLD("Gold", "গোল্ড", 25),
    PLATINUM("Platinum", "প্লাটিনাম", 50),
    DIAMOND("Diamond", "ডায়মন্ড", 90),
    HEROIC("Heroic", "হিরোইক", 140),
    MASTER("Master", "মাস্টার", 200),
    GRANDMASTER("Grandmaster", "গ্র্যান্ডমাস্টার", 300)
}

object RankAndSeasonEngine {
    private val bdZone = ZoneId.of("Asia/Dhaka")

    fun calculateStars(activeStudyMinutes: Int): Int = activeStudyMinutes / 30

    fun getRankFromStars(stars: Int): RankTier {
        return RankTier.values().lastOrNull { stars >= it.minStars } ?: RankTier.BRONZE
    }

    fun getCurrentSeasonId(): String {
        val now = ZonedDateTime.now(bdZone)
        val twoMonthBlock = (now.monthValue - 1) / 2 + 1
        return "${now.year}-S$twoMonthBlock"
    }

    fun applySeasonReset(currentRank: RankTier): RankTier {
        return when (currentRank) {
            RankTier.GRANDMASTER, RankTier.MASTER, RankTier.HEROIC, RankTier.DIAMOND -> RankTier.DIAMOND
            RankTier.PLATINUM -> RankTier.GOLD
            RankTier.GOLD -> RankTier.SILVER
            RankTier.SILVER, RankTier.BRONZE -> RankTier.BRONZE
        }
    }
}
