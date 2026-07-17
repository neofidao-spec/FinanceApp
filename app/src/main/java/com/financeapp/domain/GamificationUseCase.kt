package com.financeapp.domain

import com.financeapp.data.model.UserProgress
import com.financeapp.data.model.XpHistory
import com.financeapp.data.model.XpSource
import com.financeapp.data.repository.GamificationRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GamificationUseCase @Inject constructor(
    private val repository: GamificationRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // ===================== XP EARNING =====================

    /** XP for recording a single transaction */
    suspend fun onTransactionRecorded(): Int {
        val xp = 10
        addXp(xp, XpSource.TRANSACTION, "Mencatat transaksi")
        return xp
    }

    /** Bonus XP when transaction streak reaches milestones */
    suspend fun checkStreakMilestone(streak: Int): Int {
        val bonus = when (streak) {
            7 -> 75
            14 -> 150
            30 -> 500
            60 -> 1000
            100 -> 2000
            else -> 0
        }
        if (bonus > 0) {
            addXp(bonus, XpSource.STREAK, "Streak milestone $streak hari!")
        }
        return bonus
    }

    /** XP for staying under budget in a day */
    suspend fun onBudgetAdhered(): Int {
        val xp = 20
        addXp(xp, XpSource.BUDGET_ADHERENCE, "Tidak melebihi budget hari ini")
        return xp
    }

    /** XP for daily login */
    suspend fun onDailyLogin(): Int {
        val xp = 5
        addXp(xp, XpSource.DAILY_LOGIN, "Login harian")
        return xp
    }

    /** XP for completing a daily quest */
    suspend fun onQuestCompleted(questName: String, xpReward: Int): Int {
        addXp(xpReward, XpSource.QUEST, "Selesaikan quest: $questName")
        return xpReward
    }

    /** XP for completing a challenge */
    suspend fun onChallengeCompleted(challengeName: String, xpReward: Int): Int {
        addXp(xpReward, XpSource.CHALLENGE, "Selesaikan challenge: $challengeName")
        return xpReward
    }

    // ===================== STREAK =====================

    suspend fun updateStreak(): Pair<Int, Int> {
        val progress = repository.getUserProgressOnce()
            ?: UserProgress().also { repository.saveUserProgress(it) }

        val today = LocalDate.now()
        val lastActivity = progress.lastActivityDate?.toLocalDate()
        val now = LocalDateTime.now()

        val newStreak: Int
        val bestStreak: Int

        when {
            lastActivity == null -> {
                // First activity ever
                newStreak = 1
                bestStreak = maxOf(progress.bestStreak, 1)
            }
            lastActivity == today -> {
                // Already active today — no change
                newStreak = progress.currentStreak
                bestStreak = progress.bestStreak
            }
            lastActivity == today.minusDays(1) -> {
                // Consecutive day
                newStreak = progress.currentStreak + 1
                bestStreak = maxOf(progress.bestStreak, newStreak)
            }
            else -> {
                // Streak broken — check for freeze
                val daysSince = Duration.between(lastActivity.atStartOfDay(), today.atStartOfDay()).toDays()
                if (daysSince == 2L && progress.streakFreezes > 0) {
                    // Use freeze (skip 1 day)
                    newStreak = progress.currentStreak + 1
                    bestStreak = maxOf(progress.bestStreak, newStreak)
                    repository.saveUserProgress(
                        progress.copy(
                            currentStreak = newStreak,
                            bestStreak = bestStreak,
                            lastActivityDate = now,
                            streakFreezes = progress.streakFreezes - 1,
                            updatedAt = now
                        )
                    )
                    return Pair(newStreak, bestStreak)
                } else {
                    // Streak broken
                    newStreak = 1
                    bestStreak = progress.bestStreak
                }
            }
        }

        // Earn a freeze every 7 days of streak
        val freezesGained = if (newStreak > 0 && newStreak % 7 == 0) 1 else 0
        val totalFreezes = minOf(progress.streakFreezes + freezesGained, 3) // max 3 freezes

        repository.saveUserProgress(
            progress.copy(
                currentStreak = newStreak,
                bestStreak = bestStreak,
                lastActivityDate = now,
                streakFreezes = totalFreezes,
                updatedAt = now
            )
        )

        return Pair(newStreak, bestStreak)
    }

    /** Use a freeze to protect streak (manual action) */
    suspend fun useFreeze(): Boolean {
        val progress = repository.getUserProgressOnce() ?: return false
        if (progress.streakFreezes <= 0) return false

        repository.saveUserProgress(
            progress.copy(
                streakFreezes = progress.streakFreezes - 1,
                updatedAt = LocalDateTime.now()
            )
        )
        return true
    }

    // ===================== LEVEL PROGRESSION =====================

    private suspend fun addXp(amount: Int, source: XpSource, description: String) {
        // Record XP entry
        repository.addXpEntry(
            XpHistory(
                amount = amount,
                source = source.name,
                description = description,
                createdAt = LocalDateTime.now()
            )
        )

        // Update user progress
        val progress = repository.getUserProgressOnce()
            ?: UserProgress().also { repository.saveUserProgress(it) }

        val newTotal = progress.totalXp + amount
        val newLevel = calculateLevel(newTotal)

        repository.saveUserProgress(
            progress.copy(
                totalXp = newTotal,
                currentLevel = newLevel,
                updatedAt = LocalDateTime.now()
            )
        )
    }

    /** Calculate level from total XP using GAMIFICATION_CONCEPT.md thresholds */
    private fun calculateLevel(totalXp: Int): Int = when {
        totalXp >= 50000 -> 10
        totalXp >= 25000 -> 9
        totalXp >= 15000 -> 8
        totalXp >= 8000 -> 7
        totalXp >= 4000 -> 6
        totalXp >= 2000 -> 5
        totalXp >= 1000 -> 4
        totalXp >= 500 -> 3
        totalXp >= 200 -> 2
        else -> 1
    }

    // ===================== INIT =====================

    /** Ensure user_progress row exists */
    suspend fun initializeIfNeeded() {
        val existing = repository.getUserProgressOnce()
        if (existing == null) {
            repository.saveUserProgress(
                UserProgress(
                    id = 1,
                    totalXp = 0,
                    currentLevel = 1,
                    bestStreak = 0,
                    currentStreak = 0,
                    streakFreezes = 1,
                    lastActivityDate = null,
                    healthScore = 0.0,
                    updatedAt = LocalDateTime.now()
                )
            )
        }
    }
}
