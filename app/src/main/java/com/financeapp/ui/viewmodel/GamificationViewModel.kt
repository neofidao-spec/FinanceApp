package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.Challenge
import com.financeapp.data.model.DailyQuest
import com.financeapp.data.model.UserProgress
import com.financeapp.data.model.XpHistory
import com.financeapp.data.model.XpSource
import com.financeapp.data.repository.AchievementRepository
import com.financeapp.data.repository.GamificationRepository
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
    val dailyQuests: List<DailyQuest> = emptyList(),
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
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    init {
        initializeGamification()
        observeGamificationData()
    }

    private fun initializeGamification() {
        viewModelScope.launch {
            try {
                gamificationUseCase.initializeIfNeeded()
                // Generate today's quests if not present
                val today = LocalDate.now()
                val existingQuests = repository.getDailyQuestsOnce(today)
                if (existingQuests.isEmpty()) {
                    repository.saveDailyQuests(DailyQuest.generateForDate(today))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    private fun observeGamificationData() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val firstThree = combine(
                    repository.getUserProgress(),
                    repository.getDailyQuests(today),
                    repository.getActiveChallenges()
                ) { progress, quests, active ->
                    Triple(progress, quests, active)
                }
                val lastThree = combine(
                    repository.getCompletedChallenges(),
                    repository.getRecentXpHistory(20),
                    achievementRepository.getAllAchievements()
                ) { completed, xpHistory, achievements ->
                    Triple(completed, xpHistory, achievements)
                }
                combine(firstThree, lastThree) { (progress, quests, active), (completed, xpHistory, achievements) ->
                    GamificationUiState(
                        userProgress = progress,
                        dailyQuests = quests,
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
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
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
            } catch (_: Exception) { /* gamification non-blocking */ }
        }
    }

    /** Call when user logs in daily */
    fun onDailyLogin() {
        viewModelScope.launch {
            try {
                gamificationUseCase.onDailyLogin()
                gamificationUseCase.updateStreak()
            } catch (_: Exception) { /* gamification non-blocking */ }
        }
    }

    /** Call when budget adherence is confirmed for the day */
    fun onBudgetAdhered() {
        viewModelScope.launch {
            try {
                gamificationUseCase.onBudgetAdhered()
            } catch (_: Exception) { /* gamification non-blocking */ }
        }
    }

    /** Mark a quest as completed */
    fun completeQuest(quest: DailyQuest) {
        viewModelScope.launch {
            try {
                repository.updateQuestProgress(quest.id, quest.targetValue, true)
                gamificationUseCase.onQuestCompleted(quest.name, quest.xpReward)
            } catch (_: Exception) { /* gamification non-blocking */ }
        }
    }

    /** Mark a challenge as completed */
    fun completeChallenge(challenge: Challenge) {
        viewModelScope.launch {
            try {
                repository.updateChallengeProgress(challenge.id, challenge.targetValue, true)
                gamificationUseCase.onChallengeCompleted(challenge.name, challenge.xpReward)
            } catch (_: Exception) { /* gamification non-blocking */ }
        }
    }

    /** Use a streak freeze */
    fun useFreeze() {
        viewModelScope.launch {
            try {
                gamificationUseCase.useFreeze()
            } catch (_: Exception) { /* gamification non-blocking */ }
        }
    }
}
