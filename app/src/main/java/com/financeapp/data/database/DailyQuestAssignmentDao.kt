package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.financeapp.data.model.DailyQuestAssignment
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface DailyQuestAssignmentDao {
    @Insert
    suspend fun insert(assignment: DailyQuestAssignment)

    @Query("SELECT * FROM daily_quest_assignments WHERE assignedDate = :date")
    suspend fun getForDate(date: LocalDate): List<DailyQuestAssignment>

    @Query("SELECT questTemplateId FROM daily_quest_assignments WHERE assignedDate >= :sinceDate")
    suspend fun getQuestIdsSince(sinceDate: LocalDate): List<String>

    @Query("UPDATE daily_quest_assignments SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, completedAt: LocalDateTime)

    @Query("SELECT * FROM daily_quest_assignments WHERE assignedDate = :date AND questTemplateId = :templateId")
    suspend fun getByDateAndTemplate(date: LocalDate, templateId: String): DailyQuestAssignment?
}
