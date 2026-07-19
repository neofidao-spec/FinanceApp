package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "daily_quest_assignments")
data class DailyQuestAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questTemplateId: String,
    val assignedDate: LocalDate,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null
)
