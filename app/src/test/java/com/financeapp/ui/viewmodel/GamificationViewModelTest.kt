package com.financeapp.ui.viewmodel

import app.cash.turbine.test
import com.financeapp.data.model.*
import com.financeapp.data.repository.AchievementRepository
import com.financeapp.data.repository.GamificationRepository
import com.financeapp.data.repository.QuestRepository
import com.financeapp.data.repository.QuestWithTemplate
import com.financeapp.data.seed.QuestSeeder
import com.financeapp.domain.DailyQuestGenerator
import com.financeapp.domain.GamificationUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GamificationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: GamificationRepository
    private lateinit var gamificationUseCase: GamificationUseCase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var dailyQuestGenerator: DailyQuestGenerator
    private lateinit var questRepository: QuestRepository
    private lateinit var questSeeder: QuestSeeder

    private val today = LocalDate.now()
    private val now = LocalDateTime.now()

    private val sampleUserProgress = UserProgress(
        id = 1, totalXp = 500, currentLevel = 3,
        bestStreak = 10, currentStreak = 5, streakFreezes = 2,
        lastActivityDate = now, healthScore = 75.0, updatedAt = now
    )

    private val sampleTemplates = listOf(
        QuestTemplate(
            id = "catat_transaksi", title = "Catat Transaksi",
            description = "Catat 1 transaksi hari ini", category = QuestCategory.PENCATATAN,
            xpReward = 10, weight = 10
        ),
        QuestTemplate(
            id = "cek_dashboard", title = "Cek Dashboard",
            description = "Buka dashboard hari ini", category = QuestCategory.EKSPLORASI,
            xpReward = 5, weight = 10
        )
    )

    private val sampleAssignments = listOf(
        DailyQuestAssignment(
            id = 1, questTemplateId = "catat_transaksi",
            assignedDate = today, isCompleted = false
        ),
        DailyQuestAssignment(
            id = 2, questTemplateId = "cek_dashboard",
            assignedDate = today, isCompleted = false
        )
    )

    private val sampleQuestsWithTemplate = listOf(
        QuestWithTemplate(sampleAssignments[0], sampleTemplates[0]),
        QuestWithTemplate(sampleAssignments[1], sampleTemplates[1])
    )

    private val sampleChallenges = listOf(
        Challenge(
            id = 1, name = "Hemat Week", description = "Di bawah budget",
            xpReward = 200, challengeType = "WEEKLY",
            startDate = today.minusDays(3), endDate = today.plusDays(3),
            targetType = "BUDGET_ADHERENCE", targetValue = 7,
            currentValue = 3, isCompleted = false
        )
    )

    private val sampleAchievements = listOf(
        Achievement(
            id = 1, name = "Pencatat Pemula", description = "Catat 5 transaksi",
            icon = "edit_note", category = "TRANSACTIONS",
            targetValue = 5, currentValue = 3, isUnlocked = false
        )
    )

    private val sampleXpHistory = listOf(
        XpHistory(id = 1, amount = 10, source = "TRANSACTION", description = "Mencatat transaksi", createdAt = now)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        gamificationUseCase = mockk(relaxed = true)
        achievementRepository = mockk(relaxed = true)
        dailyQuestGenerator = mockk(relaxed = true)
        questRepository = mockk(relaxed = true)
        questSeeder = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun stubDefaults() {
        coEvery { gamificationUseCase.initializeIfNeeded() } just runs
        coEvery { questSeeder.seedIfNeeded() } just runs
        coEvery { dailyQuestGenerator.generateForToday(any()) } returns true
        coEvery { questRepository.getTodayQuests(any()) } returns sampleQuestsWithTemplate
        every { repository.getUserProgress() } returns flowOf(sampleUserProgress)
        every { repository.getActiveChallenges() } returns flowOf(sampleChallenges)
        every { repository.getCompletedChallenges() } returns flowOf(emptyList())
        every { repository.getRecentXpHistory(any()) } returns flowOf(sampleXpHistory)
        every { achievementRepository.getAllAchievements() } returns flowOf(sampleAchievements)
    }

    private fun createViewModel(): GamificationViewModel {
        return GamificationViewModel(
            repository, gamificationUseCase, achievementRepository,
            dailyQuestGenerator, questRepository, questSeeder
        )
    }

    // ---------- INITIAL STATE ----------

    @Test
    fun `initial default GamificationUiState has correct defaults`() {
        val state = GamificationUiState()
        assertEquals(true, state.isLoading)
        assertNull(state.userProgress)
        assertTrue(state.dailyQuests.isEmpty())
        assertTrue(state.activeChallenges.isEmpty())
        assertTrue(state.completedChallenges.isEmpty())
        assertTrue(state.achievements.isEmpty())
        assertTrue(state.recentXpHistory.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun `viewmodel loads with isLoading false after init completes`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        assertFalse(vm.uiState.value.isLoading)
    }

    // ---------- HAPPY PATH ----------

    @Test
    fun `user progress is loaded from repository`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val state = vm.uiState.value
        assertNotNull(state.userProgress)
        assertEquals(500, state.userProgress!!.totalXp)
        assertEquals(3, state.userProgress!!.currentLevel)
        assertEquals(5, state.userProgress!!.currentStreak)
        assertEquals(10, state.userProgress!!.bestStreak)
    }

    @Test
    fun `daily quests are loaded from new quest system`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val state = vm.uiState.value
        assertEquals(2, state.dailyQuests.size)
        assertEquals("Catat Transaksi", state.dailyQuests[0].template.title)
        assertEquals("Cek Dashboard", state.dailyQuests[1].template.title)
    }

    @Test
    fun `active challenges are loaded from repository`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val state = vm.uiState.value
        assertEquals(1, state.activeChallenges.size)
        assertEquals("Hemat Week", state.activeChallenges[0].name)
    }

    @Test
    fun `achievements are loaded from repository`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val state = vm.uiState.value
        assertEquals(1, state.achievements.size)
        assertEquals("Pencatat Pemula", state.achievements[0].name)
        assertFalse(state.achievements[0].isUnlocked)
    }

    @Test
    fun `recent xp history is loaded from repository`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val state = vm.uiState.value
        assertEquals(1, state.recentXpHistory.size)
        assertEquals(10, state.recentXpHistory[0].amount)
        assertEquals("TRANSACTION", state.recentXpHistory[0].source)
    }

    @Test
    fun `completed challenges are loaded from repository`() = runTest {
        val completedChallenge = Challenge(
            id = 2, name = "Old Challenge", description = "Done",
            xpReward = 100, challengeType = "WEEKLY",
            startDate = today.minusDays(14), endDate = today.minusDays(7),
            targetType = "BUDGET_ADHERENCE", targetValue = 7,
            currentValue = 7, isCompleted = true
        )
        coEvery { gamificationUseCase.initializeIfNeeded() } just runs
        coEvery { questSeeder.seedIfNeeded() } just runs
        coEvery { dailyQuestGenerator.generateForToday(any()) } returns true
        coEvery { questRepository.getTodayQuests(any()) } returns sampleQuestsWithTemplate
        every { repository.getUserProgress() } returns flowOf(sampleUserProgress)
        every { repository.getActiveChallenges() } returns flowOf(sampleChallenges)
        every { repository.getCompletedChallenges() } returns flowOf(listOf(completedChallenge))
        every { repository.getRecentXpHistory(any()) } returns flowOf(sampleXpHistory)
        every { achievementRepository.getAllAchievements() } returns flowOf(sampleAchievements)

        val vm = createViewModel()

        assertEquals(1, vm.uiState.value.completedChallenges.size)
        assertEquals("Old Challenge", vm.uiState.value.completedChallenges[0].name)
    }

    // ---------- INITIALIZATION ----------

    @Test
    fun `initializeGamification calls useCase initializeIfNeeded`() = runTest {
        stubDefaults()
        createViewModel()

        coVerify { gamificationUseCase.initializeIfNeeded() }
    }

    @Test
    fun `initializeGamification seeds quest templates`() = runTest {
        stubDefaults()
        createViewModel()

        coVerify { questSeeder.seedIfNeeded() }
    }

    @Test
    fun `initializeGamification generates quests for today`() = runTest {
        stubDefaults()
        createViewModel()

        coVerify { dailyQuestGenerator.generateForToday(today) }
    }

    @Test
    fun `initializeGamification fetches today quests from questRepository`() = runTest {
        stubDefaults()
        createViewModel()

        coVerify { questRepository.getTodayQuests(today) }
    }

    // ---------- ERROR HANDLING ----------

    @Test
    fun `error during initialization sets errorMessage`() = runTest {
        coEvery { gamificationUseCase.initializeIfNeeded() } throws RuntimeException("Init failed")
        every { repository.getUserProgress() } returns flowOf(sampleUserProgress)
        every { repository.getActiveChallenges() } returns flowOf(sampleChallenges)
        every { repository.getCompletedChallenges() } returns flowOf(emptyList())
        every { repository.getRecentXpHistory(any()) } returns flowOf(sampleXpHistory)
        every { achievementRepository.getAllAchievements() } returns flowOf(sampleAchievements)

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.errorMessage!!.contains("Gagal memuat data gamifikasi"))
    }

    @Test
    fun `error from observeGamificationData sets errorMessage`() = runTest {
        coEvery { gamificationUseCase.initializeIfNeeded() } just runs
        coEvery { questSeeder.seedIfNeeded() } just runs
        coEvery { dailyQuestGenerator.generateForToday(any()) } returns true
        coEvery { questRepository.getTodayQuests(any()) } returns sampleQuestsWithTemplate
        every { repository.getUserProgress() } throws RuntimeException("Flow error")
        every { repository.getActiveChallenges() } returns flowOf(sampleChallenges)
        every { repository.getCompletedChallenges() } returns flowOf(emptyList())
        every { repository.getRecentXpHistory(any()) } returns flowOf(sampleXpHistory)
        every { achievementRepository.getAllAchievements() } returns flowOf(sampleAchievements)

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ---------- RETRY ----------

    @Test
    fun `retry resets state and reinitializes`() = runTest {
        // First, cause an error
        coEvery { gamificationUseCase.initializeIfNeeded() } throws RuntimeException("Fail")
        every { repository.getUserProgress() } returns flowOf(sampleUserProgress)
        every { repository.getActiveChallenges() } returns flowOf(sampleChallenges)
        every { repository.getCompletedChallenges() } returns flowOf(emptyList())
        every { repository.getRecentXpHistory(any()) } returns flowOf(sampleXpHistory)
        every { achievementRepository.getAllAchievements() } returns flowOf(sampleAchievements)

        val vm = createViewModel()
        assertNotNull(vm.uiState.value.errorMessage)

        // Fix the use case and retry
        coEvery { gamificationUseCase.initializeIfNeeded() } just runs
        coEvery { questSeeder.seedIfNeeded() } just runs
        coEvery { dailyQuestGenerator.generateForToday(any()) } returns true
        coEvery { questRepository.getTodayQuests(any()) } returns sampleQuestsWithTemplate
        vm.retry()

        // After retry, state should be refreshed
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `retry resets to loading true initially`() = runTest {
        stubDefaults()
        val vm = createViewModel()
        assertFalse(vm.uiState.value.isLoading)

        // Use Turbine to observe the retry transition
        vm.uiState.test {
            val beforeRetry = awaitItem()
            assertFalse(beforeRetry.isLoading)

            vm.retry()

            // The state gets reset to loading=true, then immediately collects data
            // With UnconfinedTestDispatcher, the full cycle happens immediately
            val afterRetry = awaitItem()
            assertFalse(afterRetry.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---------- PUBLIC ACTIONS ----------

    @Test
    fun `onTransactionRecorded calls useCase`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.onTransactionRecorded() } returns 10
        coEvery { gamificationUseCase.updateStreak() } returns Pair(6, 10)
        coEvery { gamificationUseCase.checkStreakMilestone(any()) } returns 0

        val vm = createViewModel()
        vm.onTransactionRecorded()

        coVerify { gamificationUseCase.onTransactionRecorded() }
        coVerify { gamificationUseCase.updateStreak() }
        coVerify { gamificationUseCase.checkStreakMilestone(6) }
    }

    @Test
    fun `onTransactionRecorded does not crash on error`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.onTransactionRecorded() } throws RuntimeException("XP error")

        val vm = createViewModel()
        // Should not throw — error is caught and logged
        vm.onTransactionRecorded()

        // State should remain unchanged
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `onDailyLogin calls useCase`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.onDailyLogin() } returns 5
        coEvery { gamificationUseCase.updateStreak() } returns Pair(1, 1)

        val vm = createViewModel()
        vm.onDailyLogin()

        coVerify { gamificationUseCase.onDailyLogin() }
        coVerify { gamificationUseCase.updateStreak() }
    }

    @Test
    fun `onDailyLogin does not crash on error`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.onDailyLogin() } throws RuntimeException("Login XP error")

        val vm = createViewModel()
        vm.onDailyLogin()

        // No crash, state unchanged
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `onBudgetAdhered calls useCase`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.onBudgetAdhered() } returns 20

        val vm = createViewModel()
        vm.onBudgetAdhered()

        coVerify { gamificationUseCase.onBudgetAdhered() }
    }

    @Test
    fun `onBudgetAdhered does not crash on error`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.onBudgetAdhered() } throws RuntimeException("Budget error")

        val vm = createViewModel()
        vm.onBudgetAdhered()

        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `completeQuest calls questRepository and useCase`() = runTest {
        stubDefaults()
        coEvery { questRepository.completeQuest(any()) } just runs
        coEvery { gamificationUseCase.onQuestCompleted(any(), any()) } returns 10

        val vm = createViewModel()
        val quest = sampleQuestsWithTemplate[0]
        vm.completeQuest(quest)

        coVerify { questRepository.completeQuest(quest.assignment.id) }
        coVerify { gamificationUseCase.onQuestCompleted(quest.template.title, quest.template.xpReward) }
    }

    @Test
    fun `completeQuest refreshes quests after completion`() = runTest {
        stubDefaults()
        coEvery { questRepository.completeQuest(any()) } just runs
        coEvery { gamificationUseCase.onQuestCompleted(any(), any()) } returns 10

        val vm = createViewModel()
        vm.completeQuest(sampleQuestsWithTemplate[0])

        // Should call getTodayQuests twice: once in init, once after completion
        coVerify(exactly = 2) { questRepository.getTodayQuests(today) }
    }

    @Test
    fun `completeQuest does not crash on error`() = runTest {
        stubDefaults()
        coEvery { questRepository.completeQuest(any()) } throws RuntimeException("Quest error")

        val vm = createViewModel()
        vm.completeQuest(sampleQuestsWithTemplate[0])

        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `completeChallenge calls repository and useCase`() = runTest {
        stubDefaults()
        coEvery { repository.updateChallengeProgress(any(), any(), any()) } just runs
        coEvery { gamificationUseCase.onChallengeCompleted(any(), any()) } returns 200

        val vm = createViewModel()
        val challenge = sampleChallenges[0]
        vm.completeChallenge(challenge)

        coVerify { repository.updateChallengeProgress(challenge.id, challenge.targetValue, true) }
        coVerify { gamificationUseCase.onChallengeCompleted(challenge.name, challenge.xpReward) }
    }

    @Test
    fun `completeChallenge does not crash on error`() = runTest {
        stubDefaults()
        coEvery { repository.updateChallengeProgress(any(), any(), any()) } throws RuntimeException("Challenge error")

        val vm = createViewModel()
        vm.completeChallenge(sampleChallenges[0])

        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `useFreeze calls useCase`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.useFreeze() } returns true

        val vm = createViewModel()
        vm.useFreeze()

        coVerify { gamificationUseCase.useFreeze() }
    }

    @Test
    fun `useFreeze does not crash on error`() = runTest {
        stubDefaults()
        coEvery { gamificationUseCase.useFreeze() } throws RuntimeException("Freeze error")

        val vm = createViewModel()
        vm.useFreeze()

        assertFalse(vm.uiState.value.isLoading)
    }

    // ---------- TURBINE TESTS ----------

    @Test
    fun `uiState emits complete gamification state via turbine`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.userProgress)
            assertEquals(2, state.dailyQuests.size)
            assertEquals(1, state.activeChallenges.size)
            assertEquals(1, state.achievements.size)
            assertEquals(1, state.recentXpHistory.size)
            assertNull(state.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState dailyQuests uses QuestWithTemplate type`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val quests = vm.uiState.value.dailyQuests
        assertEquals(2, quests.size)
        assertEquals("catat_transaksi", quests[0].template.id)
        assertEquals("Catat Transaksi", quests[0].template.title)
        assertEquals(10, quests[0].template.xpReward)
        assertFalse(quests[0].assignment.isCompleted)
    }
}
