package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QuestCategory {
    PENCATATAN, BUDGETING, EKSPLORASI, DISIPLIN, REVIEW
}

@Entity(tableName = "quest_templates")
data class QuestTemplate(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: QuestCategory,
    val xpReward: Int,
    val weight: Int,
    val requiresCondition: String? = null
)
