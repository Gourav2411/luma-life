package com.luma.data

import com.luma.data.local.PlanItemDao
import com.luma.data.local.toDomain
import com.luma.data.local.toEntity
import com.luma.model.ChangeAction
import com.luma.model.PlanItem
import com.luma.model.PlanProposal
import com.luma.model.PlanStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlanRepository(
    private val dao: PlanItemDao,
) {
    fun observeDay(date: String): Flow<List<PlanItem>> =
        dao.observeForDate(date).map { items -> items.map { it.toDomain() } }

    suspend fun getDay(date: String): List<PlanItem> =
        dao.getForDate(date).map { it.toDomain() }

    suspend fun isEmpty(): Boolean = dao.count() == 0

    suspend fun save(items: List<PlanItem>) {
        dao.upsert(items.map(PlanItem::toEntity))
    }

    suspend fun applyApprovedProposal(
        proposal: PlanProposal,
        acceptedChangeIds: Set<String>,
    ) {
        val accepted = proposal.changes.filter { it.id in acceptedChangeIds }
        val deletes = accepted.filter { it.action == ChangeAction.DELETE }.mapNotNull { it.targetId }
        if (deletes.isNotEmpty()) dao.delete(deletes)

        val updates = accepted.mapNotNull { change ->
            change.after?.copy(status = PlanStatus.ACTIVE)
        }
        if (updates.isNotEmpty()) dao.upsert(updates.map(PlanItem::toEntity))
    }
}
