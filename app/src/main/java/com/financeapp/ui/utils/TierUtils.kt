package com.financeapp.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.financeapp.R

/**
 * Tier system: 7 tiers based on XP thresholds
 *
 * Tier 1: Pemula Finansial (0 - 199 XP) — Level 1
 * Tier 2: Pencatat Cermat (200 - 499 XP) — Level 2
 * Tier 3: Pengatur Budget (500 - 999 XP) — Level 3
 * Tier 4: Perencana Keuangan (1000 - 1999 XP) — Level 4
 * Tier 5: Ahli Finansial (2000 - 7999 XP) — Level 5-6
 * Tier 6: Legenda Hemat (8000 - 24999 XP) — Level 7-8
 * Tier 7: Master Kebebasan Finansial (25000+ XP) — Level 9-10
 */
data class TierInfo(
    val tier: Int,
    val name: String,
    val xpMin: Int,
    val xpMax: Int,  // -1 for no max (tier 7)
    val description: String
)

object TierUtils {

    val tiers = listOf(
        TierInfo(1, "Pemula Finansial", 0, 199, "Memulai perjalanan finansial"),
        TierInfo(2, "Pencatat Cermat", 200, 499, "Rajin mencatat transaksi"),
        TierInfo(3, "Pengatur Budget", 500, 999, "Mengatur anggaran dengan baik"),
        TierInfo(4, "Perencana Keuangan", 1000, 1999, "Merencanakan keuangan masa depan"),
        TierInfo(5, "Ahli Finansial", 2000, 7999, "Menguasai pengelolaan keuangan"),
        TierInfo(6, "Legenda Hemat", 8000, 24999, "Kebiasaan hemat yang luar biasa"),
        TierInfo(7, "Master Kebebasan Finansial", 25000, -1, "Mencapai kebebasan finansial")
    )

    /**
     * Get tier info based on total XP
     */
    fun getTierForXp(totalXp: Int): TierInfo {
        return tiers.last { totalXp >= it.xpMin }
    }

    /**
     * Get tier info based on current level
     */
    fun getTierForLevel(level: Int): TierInfo {
        return when (level) {
            1 -> tiers[0]
            2 -> tiers[1]
            3 -> tiers[2]
            4 -> tiers[3]
            5, 6 -> tiers[4]
            7, 8 -> tiers[5]
            9, 10 -> tiers[6]
            else -> tiers[6]
        }
    }

    /**
     * Get progress within current tier (0f - 1f)
     */
    fun getTierProgress(totalXp: Int): Float {
        val tier = getTierForXp(totalXp)
        if (tier.xpMax == -1) {
            // Tier 7: no max, always full
            return 1f
        }
        val range = tier.xpMax - tier.xpMin + 1
        val progress = totalXp - tier.xpMin
        return (progress.toFloat() / range).coerceIn(0f, 1f)
    }

    /**
     * Get XP needed to reach next tier
     */
    fun getXpToNextTier(totalXp: Int): Int? {
        val tier = getTierForXp(totalXp)
        if (tier.tier >= 7) return null  // Already max tier
        val nextTier = tiers[tier.tier]  // tiers is 0-indexed, tier.tier is 1-indexed
        return nextTier.xpMin - totalXp
    }

    /**
     * Get all tiers with their status (completed, current, locked)
     */
    fun getTierStatusList(totalXp: Int): List<TierStatus> {
        val currentTier = getTierForXp(totalXp)
        return tiers.map { tier ->
            TierStatus(
                tier = tier,
                status = when {
                    tier.tier < currentTier.tier -> TierState.COMPLETED
                    tier.tier == currentTier.tier -> TierState.CURRENT
                    else -> TierState.LOCKED
                }
            )
        }
    }
}

enum class TierState { COMPLETED, CURRENT, LOCKED }

data class TierStatus(
    val tier: TierInfo,
    val status: TierState
)

/**
 * Get drawable resource ID for tier badge
 * SVG files stored in assets/tiers/ — use Coil to load
 * For now, return asset path
 */
fun getTierAssetPath(tier: Int): String {
    return "file:///android_asset/tiers/tier$tier.svg"
}
