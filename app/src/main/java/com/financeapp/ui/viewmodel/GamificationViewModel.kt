package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.Challenge
import com.financeapp.data.model.UserProgress
import com.financeapp.data.model.XpHistory
import com.financeapp.data.model.XpSource
import com.financeapp.data.repository.AchievementRepository
import com.financeapp.data.repository.GamificationRepository
import com.financeapp.data.repository.QuestRepository
import com.financeapp.data.repository.QuestWithTemplate
import com.financeapp.data.seed.QuestSeeder
import com.financeapp.domain.DailyQuestGenerator
import com.financeapp.domain.GamificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class GamificationUiState(
    val userProgress: UserProgress? = null,
    val dailyQuests: List<QuestWithTemplate> = emptyList(),
    val activeChallenges: List<Challenge> = emptyList(),
    val completedChallenges: List<Challenge> = emptyList(),
    val achievements: List<com.financeapp.data.model.Achievement> = emptyList(),
    val recentXpHistory: List<XpHistory> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val repository: GamificationRepository,
    private val gamificationUseCase: GamificationUseCase,
    private val achievementRepository: AchievementRepository,
    private val dailyQuestGenerator: DailyQuestGenerator,
    private val questRepository: QuestRepository,
    private val questSeeder: QuestSeeder
    ) : ViewModel() {
    companion object {
        private const val TAG = "GamificationVM"
    }

    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            initializeGamification()
            observeGamificationData()
        }
    }

    /** Retry after error — re-initialize and re-observe */
    fun retry() {
        _uiState.value = GamificationUiState(isLoading = true)
        viewModelScope.launch {
            initializeGamification()
            observeGamificationData()
        }
    }

    private suspend fun initializeGamification() {
        try {
            gamificationUseCase.initializeIfNeeded()
            questSeeder.seedIfNeeded()
            dailyQuestGenerator.generateForToday(LocalDate.now())
            // Fetch today's quests with templates
            val quests = questRepository.getTodayQuests(LocalDate.now())
            _uiState.value = _uiState.value.copy(dailyQuests = quests)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize gamification", e)
            _uiState.value = _uiState.value.copy(errorMessage = "Gagal memuat data gamifikasi. Silakan coba lagi.")
        }
    }

    private suspend fun observeGamificationData() {
        try {
                val combine = combine(
                    repository.getUserProgress(),
                    repository.getActiveChallenges()
                ) { progress, active ->
                    Pair(progress, active)
                }
                val lastThree = combine(
                    repository.getCompletedChallenges(),
                    repository.getRecentXpHistory(20),
                    achievementRepository.getAllAchievements()
                ) { completed, xpHistory, achievements ->
                    Triple(completed, xpHistory, achievements)
                }
                combine(combine, lastThree) { (progress, active), (completed, xpHistory, achievements) ->
                    // Preserve dailyQuests from the current state (set by initializeGamification or completeQuest)
                    val currentQuests = _uiState.value.dailyQuests
                    val currentIsLoading = _uiState.value.isLoading
                    GamificationUiState(
                        userProgress = progress,
                        dailyQuests = currentQuests,
                        activeChallenges = active,
                        completedChallenges = completed,
                        recentXpHistory = xpHistory,
                        achievements = achievements,
                        isLoading = false
                    )
                }.collectLatest { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to observe gamification data", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memuat data gamifikasi. Silakan coba lagi.",
                    isLoading = false
                )
            }
    }

    // ===================== PUBLIC ACTIONS =====================

    /** Call when user records a transaction */
    fun onTransactionRecorded() {
        viewModelScope.launch {
            try {
                gamificationUseCase.onTransactionRecorded()
                val (streak, best) = gamificationUseCase.updateStreak()
                gamificationUseCase.checkStreakMilestone(streak)
            } catch (e: Exception) { Log.w("GamificationVM", "Action failed", e) }
        }
    }

    /** Call when user logs in daily */
    fun onDailyLogin() {
        viewModelScope.launch {
            try {
                gamificationUseCase.onDailyLogin()
                gamificationUseCase.updateStreak()
            } catch (e: Exception) { Log.w("GamificationVM", "Action failed", e) }
        }
    }

    /** Call when budget adherence is confirmed for the day */
    fun onBudgetAdhered() {
        viewModelScope.launch {
            try {
                gamificationUseCase.onBudgetAdhered()
            } catch (e: Exception) { Log.w("GamificationVM", "Action failed", e) }
        }
    }

    /** Mark a quest as completed using the new dynamic quest system */
    fun completeQuest(quest: QuestWithTemplate) {
        viewModelScope.launch {
            try {
                questRepository.completeQuest(quest.assignment.id)
                gamificationUseCase.onQuestCompleted(quest.template.title, quest.template.xpReward)
                // Refresh quests
                val quests = questRepository.getTodayQuests(LocalDate.now())

                // If all quests are completed, generate a new batch (max 2 batches per day = 6 quests)
                if (quests.isNotEmpty() && quests.all { it.assignment.isCompleted }) {
                    val todayQuests = assignmentDao.getForDate(LocalDate.now())
                    if (todayQuests.size <= 3) {
                        // First batch completed — generate second batch
                        dailyQuestGenerator.generateForToday(LocalDate.now())
                    }
                }

                val refreshedQuests = questRepository.getTodayQuests(LocalDate.now())
                _uiState.value = _uiState.value.copy(
                    dailyQuests = refreshedQuests,
                    recentXpHistory = repository.getRecentXpHistoryOnce(20)
                )
            } catch (e: Exception) { Log.w(TAG, "Quest completion failed", e) }
        }
    }

    /** Auto-complete a quest by template ID (called from screens once) */
    fun autoCompleteQuest(templateId: String) {
        viewModelScope.launch {
            try {
                // Wait for quests to be loaded
                var attempts = 0
                while (_uiState.value.dailyQuests.isEmpty() && _uiState.value.isLoading && attempts < 50) {
                    kotlinx.coroutines.delay(100)
                    attempts++
                }
                val quest = _uiState.value.dailyQuests.find {
                    it.template.id == templateId && !it.assignment.isCompleted
                }
                quest?.let { completeQuest(it) }
            } catch (e: Exception) { Log.w(TAG, "Auto-complete quest failed: $templateId", e) }
        }
    }

    /** Use a streak freeze */
    fun useFreeze() {
        viewModelScope.launch {
            try {
                gamificationUseCase.useFreeze()
            } catch (e: Exception) { Log.w("GamificationVM", "Action failed", e) }
        }
    }
}
