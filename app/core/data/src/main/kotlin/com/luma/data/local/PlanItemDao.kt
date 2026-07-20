package com.luma.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanItemDao {
    @Query("SELECT * FROM plan_items WHERE date = :date ORDER BY startMinute")
    fun observeForDate(date: String): Flow<List<PlanItemEntity>>

    @Query("SELECT * FROM plan_items WHERE date = :date ORDER BY startMinute")
    suspend fun getForDate(date: String): List<PlanItemEntity>

    @Query("SELECT COUNT(*) FROM plan_items")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(items: List<PlanItemEntity>)

    @Query("DELETE FROM plan_items WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM plan_items")
    suspend fun clear()
}
