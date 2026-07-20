package com.luma.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luma.model.EnergyLevel
import com.luma.model.Flexibility
import com.luma.model.LifeArea
import com.luma.model.PlanItem
import com.luma.model.PlanSource
import com.luma.model.PlanStatus

@Entity(tableName = "plan_items")
data class PlanItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val startMinute: Int?,
    val durationMinutes: Int,
    val lifeArea: String,
    val flexibility: String,
    val source: String,
    val status: String,
    val energy: String,
    val parentGoalId: String?,
    val parentLabel: String?,
    val travelBeforeMinutes: Int,
    val travelAfterMinutes: Int,
    val notes: String?,
)

fun PlanItemEntity.toDomain() = PlanItem(
    id = id,
    title = title,
    date = date,
    startMinute = startMinute,
    durationMinutes = durationMinutes,
    lifeArea = LifeArea.valueOf(lifeArea),
    flexibility = Flexibility.valueOf(flexibility),
    source = PlanSource.valueOf(source),
    status = PlanStatus.valueOf(status),
    energy = EnergyLevel.valueOf(energy),
    parentGoalId = parentGoalId,
    parentLabel = parentLabel,
    travelBeforeMinutes = travelBeforeMinutes,
    travelAfterMinutes = travelAfterMinutes,
    notes = notes,
)

fun PlanItem.toEntity() = PlanItemEntity(
    id = id,
    title = title,
    date = date,
    startMinute = startMinute,
    durationMinutes = durationMinutes,
    lifeArea = lifeArea.name,
    flexibility = flexibility.name,
    source = source.name,
    status = status.name,
    energy = energy.name,
    parentGoalId = parentGoalId,
    parentLabel = parentLabel,
    travelBeforeMinutes = travelBeforeMinutes,
    travelAfterMinutes = travelAfterMinutes,
    notes = notes,
)
