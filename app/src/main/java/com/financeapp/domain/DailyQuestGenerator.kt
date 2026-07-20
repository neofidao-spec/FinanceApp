package com.financeapp.domain

import com.financeapp.data.database.DailyQuestAssignmentDao
import com.financeapp.data.database.QuestTemplateDao
import com.financeapp.data.model.DailyQuestAssignment
import com.financeapp.data.model.QuestTemplate
import java.time.LocalDate
import javax.inject.Inject

class DailyQuestGenerator @Inject constructor(
    private val questTemplateDao: QuestTemplateDao,
    private val assignmentDao: DailyQuestAssignmentDao,
    private val conditionEvaluator: QuestConditionEvaluator
) {
    suspend fun generateForToday(today: LocalDate): Boolean {
        val existing = assignmentDao.getForDate(today)
        // Allow regeneration if all existing quests are completed (max 6 quests per day = 2 batches)
        if (existing.isNotEmpty() && existing.any { !it.isCompleted }) return false
        if (existing.size >= 6) return false // max 2 batches per day

        val recentIds = assignmentDao.getQuestIdsSince(today.minusDays(2))
        val allTemplates = questTemplateDao.getAll()

        val eligible = allTemplates.filter { template ->
            (template.requiresCondition == null ||
                conditionEvaluator.evaluate(template.requiresCondition)) &&
            template.id !in recentIds
        }

        val pool = if (eligible.size < 3) {
            allTemplates.filter {
                (it.requiresCondition == null ||
                    conditionEvaluator.evaluate(it.requiresCondition)) &&
                    it.id !in recentIds
            }
        } else eligible

        val selected = weightedRandomPick(pool, count = 3)

        selected.forEach { template ->
            assignmentDao.insert(
                DailyQuestAssignment(
                    questTemplateId = template.id,
                    assignedDate = today
                )
            )
        }
        return true
    }

    private fun weightedRandomPick(
        pool: List<QuestTemplate>,
        count: Int
    ): List<QuestTemplate> {
        val remaining = pool.toMutableList()
        val result = mutableListOf<QuestTemplate>()
        repeat(minOf(count, remaining.size)) {
            val totalWeight = remaining.sumOf { it.weight }
            if (totalWeight <= 0) return result
            var r = (0 until totalWeight).random()
            val picked = remaining.first { entry ->
                r -= entry.weight
                r < 0
            }
            result.add(picked)
            remaining.remove(picked)
        }
        return result
    }
}
