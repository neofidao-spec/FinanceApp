package com.financeapp.data.repository

import com.financeapp.data.database.DailyQuestAssignmentDao
import com.financeapp.data.database.QuestTemplateDao
import com.financeapp.data.model.DailyQuestAssignment
import com.financeapp.data.model.QuestTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class QuestWithTemplate(
    val assignment: DailyQuestAssignment,
    val template: QuestTemplate
)

class QuestRepository @Inject constructor(
    private val assignmentDao: DailyQuestAssignmentDao,
    private val templateDao: QuestTemplateDao
) {
    suspend fun getTodayQuests(date: LocalDate): List<QuestWithTemplate> {
        val assignments = assignmentDao.getForDate(date)
        return assignments.mapNotNull { assignment ->
            val template = templateDao.getById(assignment.questTemplateId)
            template?.let { QuestWithTemplate(assignment, it) }
        }
    }

    suspend fun completeQuest(assignmentId: Long) {
        assignmentDao.markCompleted(assignmentId, LocalDateTime.now())
    }

    suspend fun getByDateAndTemplate(date: LocalDate, templateId: String): DailyQuestAssignment? {
        return assignmentDao.getByDateAndTemplate(date, templateId)
    }
}
