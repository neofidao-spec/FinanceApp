package com.financeapp.domain

import com.financeapp.data.model.UserProgress
import com.financeapp.data.model.XpHistory
import com.financeapp.data.model.XpSource
import com.financeapp.data.repository.GamificationRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GamificationUseCaseTest {

    private lateinit var repository: GamificationRepository
    private lateinit var useCase: GamificationUseCase

    private val now = LocalDateTime.now()

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = GamificationUseCase(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ===================== XP ADDITION =====================

    @Test
    fun `onTransactionRecorded returns 10 XP`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        val xp = useCase.onTransactionRecorded()

        assertEquals(10, xp)
    }

    @Test
    fun `onTransactionRecorded adds XP entry to repository`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded()

        coVerify {
            repository.addXpEntry(match {
                it.amount == 10 && it.source == XpSource.TRANSACTION.name
            })
        }
    }

    @Test
    fun `onTransactionRecorded updates totalXp in user progress`() = runTest {
        val progress = UserProgress(totalXp = 50, currentLevel = 1)
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded()

        coVerify {
            repository.saveUserProgress(match { it.totalXp == 60 })
        }
    }

    // ===================== LEVEL CALCULATION =====================

    @Test
    fun `0 XP corresponds to level 1`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded() // adds 10 XP -> total 10

        coVerify {
            repository.saveUserProgress(match { it.currentLevel == 1 })
        }
    }

    @Test
    fun `200 XP corresponds to level 2`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 190, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded() // 190 + 10 = 200

        coVerify {
            repository.saveUserProgress(match { it.currentLevel == 2 })
        }
    }

    @Test
    fun `500 XP corresponds to level 3`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 490, currentLevel = 2)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded() // 490 + 10 = 500

        coVerify {
            repository.saveUserProgress(match { it.currentLevel == 3 })
        }
    }

    @Test
    fun `1000 XP corresponds to level 4`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 990, currentLevel = 3)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded() // 990 + 10 = 1000

        coVerify {
            repository.saveUserProgress(match { it.currentLevel == 4 })
        }
    }

    @Test
    fun `2000 XP corresponds to level 5`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 1990, currentLevel = 4)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded() // 1990 + 10 = 2000

        coVerify {
            repository.saveUserProgress(match { it.currentLevel == 5 })
        }
    }

    @Test
    fun `50000 XP corresponds to level 10`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 49990, currentLevel = 9)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.onTransactionRecorded() // 49990 + 10 = 50000

        coVerify {
            repository.saveUserProgress(match { it.currentLevel == 10 })
        }
    }

    // ===================== STREAK LOGIC =====================

    @Test
    fun `updateStreak with consecutive day increments streak`() = runTest {
        val yesterday = now.minusDays(1)
        val progress = UserProgress(
            currentStreak = 5, bestStreak = 10,
            lastActivityDate = yesterday, streakFreezes = 1
        )
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.saveUserProgress(any()) } just runs

        val (newStreak, bestStreak) = useCase.updateStreak()

        assertEquals(6, newStreak)
        assertEquals(10, bestStreak) // bestStreak stays 10 since 6 < 10
    }

    @Test
    fun `updateStreak with first activity sets streak to 1`() = runTest {
        val progress = UserProgress(
            currentStreak = 0, bestStreak = 0,
            lastActivityDate = null, streakFreezes = 0
        )
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.saveUserProgress(any()) } just runs

        val (newStreak, bestStreak) = useCase.updateStreak()

        assertEquals(1, newStreak)
        assertEquals(1, bestStreak)
    }

    @Test
    fun `updateStreak broken streak resets to 1`() = runTest {
        val threeDaysAgo = now.minusDays(3)
        val progress = UserProgress(
            currentStreak = 10, bestStreak = 10,
            lastActivityDate = threeDaysAgo, streakFreezes = 0
        )
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.saveUserProgress(any()) } just runs

        val (newStreak, bestStreak) = useCase.updateStreak()

        assertEquals(1, newStreak)
        assertEquals(10, bestStreak) // bestStreak preserved
    }

    @Test
    fun `updateStreak uses freeze when 2 days gap and freeze available`() = runTest {
        val twoDaysAgo = now.minusDays(2)
        val progress = UserProgress(
            currentStreak = 5, bestStreak = 10,
            lastActivityDate = twoDaysAgo, streakFreezes = 2
        )
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.saveUserProgress(any()) } just runs

        val (newStreak, _) = useCase.updateStreak()

        assertEquals(6, newStreak)
        // Verify freeze was consumed
        coVerify {
            repository.saveUserProgress(match { it.streakFreezes == 1 })
        }
    }

    @Test
    fun `updateStreak does not change streak when already active today`() = runTest {
        val progress = UserProgress(
            currentStreak = 5, bestStreak = 10,
            lastActivityDate = now, streakFreezes = 1
        )
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.saveUserProgress(any()) } just runs

        val (newStreak, bestStreak) = useCase.updateStreak()

        assertEquals(5, newStreak)
        assertEquals(10, bestStreak)
    }

    @Test
    fun `useFreeze returns true when freezes available`() = runTest {
        val progress = UserProgress(streakFreezes = 2)
        coEvery { repository.getUserProgressOnce() } returns progress
        coEvery { repository.saveUserProgress(any()) } just runs

        val result = useCase.useFreeze()

        assertTrue(result)
        coVerify {
            repository.saveUserProgress(match { it.streakFreezes == 1 })
        }
    }

    @Test
    fun `useFreeze returns false when no freezes available`() = runTest {
        val progress = UserProgress(streakFreezes = 0)
        coEvery { repository.getUserProgressOnce() } returns progress

        val result = useCase.useFreeze()

        assertFalse(result)
    }

    @Test
    fun `useFreeze returns false when no progress found`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns null

        val result = useCase.useFreeze()

        assertFalse(result)
    }

    // ===================== STREAK MILESTONE =====================

    @Test
    fun `checkStreakMilestone returns 75 XP for 7-day streak`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        val bonus = useCase.checkStreakMilestone(7)

        assertEquals(75, bonus)
    }

    @Test
    fun `checkStreakMilestone returns 500 XP for 30-day streak`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        val bonus = useCase.checkStreakMilestone(30)

        assertEquals(500, bonus)
    }

    @Test
    fun `checkStreakMilestone returns 150 XP for 14-day streak`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        val bonus = useCase.checkStreakMilestone(14)

        assertEquals(150, bonus)
    }

    @Test
    fun `checkStreakMilestone returns 0 for non-milestone streak`() = runTest {
        val bonus = useCase.checkStreakMilestone(5)

        assertEquals(0, bonus)
    }

    @Test
    fun `checkStreakMilestone records XP entry with STREAK source`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.checkStreakMilestone(7)

        coVerify {
            repository.addXpEntry(match {
                it.amount == 75 && it.source == XpSource.STREAK.name
            })
        }
    }

    // ===================== INITIALIZE =====================

    @Test
    fun `initializeIfNeeded creates default UserProgress when none exists`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns null
        coEvery { repository.saveUserProgress(any()) } just runs

        useCase.initializeIfNeeded()

        coVerify {
            repository.saveUserProgress(match {
                it.id == 1L &&
                it.totalXp == 0 &&
                it.currentLevel == 1 &&
                it.bestStreak == 0 &&
                it.currentStreak == 0 &&
                it.streakFreezes == 1
            })
        }
    }

    @Test
    fun `initializeIfNeeded does nothing when progress already exists`() = runTest {
        val existing = UserProgress(id = 1, totalXp = 100, currentLevel = 2)
        coEvery { repository.getUserProgressOnce() } returns existing

        useCase.initializeIfNeeded()

        coVerify(exactly = 0) { repository.saveUserProgress(any()) }
    }

    // ===================== OTHER XP METHODS =====================

    @Test
    fun `onBudgetAdhered returns 20 XP`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        val xp = useCase.onBudgetAdhered()

        assertEquals(20, xp)
    }

    @Test
    fun `onDailyLogin returns 5 XP`() = runTest {
        coEvery { repository.getUserProgressOnce() } returns UserProgress(totalXp = 0, currentLevel = 1)
        coEvery { repository.addXpEntry(any()) } just runs
        coEvery { repository.saveUserProgress(any()) } just runs

        val xp = useCase.onDailyLogin()

        assertEquals(5, xp)
    }
}
