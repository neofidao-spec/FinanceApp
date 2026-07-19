package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financeapp.data.model.QuestTemplate

@Dao
interface QuestTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<QuestTemplate>)

    @Query("SELECT * FROM quest_templates")
    suspend fun getAll(): List<QuestTemplate>

    @Query("SELECT * FROM quest_templates WHERE id = :id")
    suspend fun getById(id: String): QuestTemplate?

    @Query("SELECT COUNT(*) FROM quest_templates")
    suspend fun count(): Int
}
