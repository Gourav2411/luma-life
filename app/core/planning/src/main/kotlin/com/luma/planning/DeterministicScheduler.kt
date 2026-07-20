package com.luma.planning

import com.luma.model.ChangeAction
import com.luma.model.EnergyLevel
import com.luma.model.Flexibility
import com.luma.model.LifeArea
import com.luma.model.PlanChange
import com.luma.model.PlanHorizon
import com.luma.model.PlanItem
import com.luma.model.PlanProposal
import com.luma.model.PlanSource
import com.luma.model.PlanStatus
import com.luma.model.RiskLevel
import java.util.UUID
import kotlin.math.abs

data class SchedulingRequest(
    val title: String,
    val date: String,
    val durationMinutes: Int,
    val lifeArea: LifeArea,
    val preferredStartMinute: Int?,
    val earliestStartMinute: Int = 6 * 60,
    val latestEndMinute: Int = 22 * 60 + 30,
    val energy: EnergyLevel = EnergyLevel.STEADY,
    val parentGoalId: String? = null,
)

data class SchedulerPreferences(
    val dayStartMinute: Int = 6 * 60,
    val dayEndMinute: Int = 22 * 60 + 30,
    val focusPeakStartMinute: Int = 9 * 60,
    val focusPeakEndMinute: Int = 13 * 60,
    val minimumBufferMinutes: Int = 10,
)

data class ValidationResult(
    val isValid: Boolean,
    val messages: List<String>,
)

class DeterministicScheduler(
    private val preferences: SchedulerPreferences = SchedulerPreferences(),
) {
    fun createProposal(
        existing: List<PlanItem>,
        requests: List<SchedulingRequest>,
        assumptions: List<String> = emptyList(),
    ): PlanProposal {
        val existingValidation = validate(existing)
        if (!existingValidation.isValid) {
            return PlanProposal(
                id = uuid(),
                horizon = PlanHorizon.DAY,
                summary = "Your current plan already contains a hard conflict.",
                assumptions = assumptions,
                changes = emptyList(),
                unresolvedQuestions = existingValidation.messages,
                confidence = 0.0,
                isFeasible = false,
                validationMessages = existingValidation.messages,
            )
        }

        val working = existing.toMutableList()
        val changes = mutableListOf<PlanChange>()
        val unresolved = mutableListOf<String>()

        requests.sortedWith(compareByDescending<SchedulingRequest> { it.lifeArea == LifeArea.ACADEMICS }
            .thenByDescending { it.durationMinutes })
            .forEach { request ->
                val slot = findBestSlot(working, request)
                if (slot == null) {
                    unresolved += "No feasible ${request.durationMinutes}-minute slot was found for ${request.title}."
                    return@forEach
                }
                val created = PlanItem(
                    id = uuid(),
                    title = request.title,
                    date = request.date,
                    startMinute = slot,
                    durationMinutes = request.durationMinutes,
                    lifeArea = request.lifeArea,
                    flexibility = Flexibility.FLEXIBLE,
                    source = PlanSource.LUMA,
                    status = PlanStatus.DRAFT,
                    energy = request.energy,
                    parentGoalId = request.parentGoalId,
                )
                working += created
                changes += PlanChange(
                    id = uuid(),
                    action = ChangeAction.CREATE,
                    targetId = null,
                    before = null,
                    after = created,
                    reason = reasonFor(request, slot),
                    affectedLifeAreas = setOf(request.lifeArea),
                    risk = RiskLevel.LOW,
                )
            }

        val finalValidation = validate(working)
        val feasible = unresolved.isEmpty() && finalValidation.isValid
        return PlanProposal(
            id = uuid(),
            horizon = PlanHorizon.DAY,
            summary = if (feasible) {
                "A feasible plan that protects fixed commitments and leaves recovery space."
            } else {
                "The full request does not fit yet. Review the unresolved trade-offs."
            },
            assumptions = assumptions,
            changes = changes,
            unresolvedQuestions = unresolved,
            confidence = if (feasible) 0.92 else 0.58,
            isFeasible = feasible,
            validationMessages = finalValidation.messages,
        )
    }

    fun validate(items: List<PlanItem>): ValidationResult {
        val messages = mutableListOf<String>()
        val scheduled = items.filter { it.startMinute != null && it.status != PlanStatus.ARCHIVED }

        scheduled.groupBy(PlanItem::date).values.forEach { dayItems ->
            dayItems.sortedBy(PlanItem::startMinute).zipWithNext().forEach { (first, second) ->
                val firstEnd = first.endMinute ?: return@forEach
                val secondStart = second.startMinute ?: return@forEach
                if (firstEnd + first.travelAfterMinutes + second.travelBeforeMinutes > secondStart) {
                    messages += "${first.title} overlaps ${second.title} including travel buffers."
                }
            }
        }
        scheduled.forEach { item ->
            val start = item.startMinute ?: return@forEach
            val end = item.endMinute ?: return@forEach
            if (start < preferences.dayStartMinute || end > preferences.dayEndMinute) {
                messages += "${item.title} falls outside the confirmed waking window."
            }
            if (item.durationMinutes <= 0) {
                messages += "${item.title} has an invalid duration."
            }
        }
        return ValidationResult(messages.isEmpty(), messages)
    }

    private fun findBestSlot(existing: List<PlanItem>, request: SchedulingRequest): Int? {
        val earliest = maxOf(request.earliestStartMinute, preferences.dayStartMinute)
        val latestStart = minOf(request.latestEndMinute, preferences.dayEndMinute) - request.durationMinutes
        if (latestStart < earliest) return null

        return generateSequence(earliest) { it + 15 }
            .takeWhile { it <= latestStart }
            .filter { candidate -> canPlace(existing, request.date, candidate, request.durationMinutes) }
            .minByOrNull { candidate -> score(candidate, request) }
    }

    private fun canPlace(
        existing: List<PlanItem>,
        date: String,
        start: Int,
        duration: Int,
    ): Boolean {
        val end = start + duration
        return existing.none { item ->
            val itemStart = item.startMinute
            if (item.date != date || itemStart == null || item.status == PlanStatus.ARCHIVED) {
                false
            } else {
                val blockedStart = itemStart - item.travelBeforeMinutes - preferences.minimumBufferMinutes
                val blockedEnd = (item.endMinute ?: itemStart) +
                    item.travelAfterMinutes + preferences.minimumBufferMinutes
                start < blockedEnd && end > blockedStart
            }
        }
    }

    private fun score(start: Int, request: SchedulingRequest): Int {
        val preferredPenalty = request.preferredStartMinute?.let { abs(start - it) } ?: 0
        val energyPenalty = when (request.energy) {
            EnergyLevel.HIGH ->
                if (start in preferences.focusPeakStartMinute..preferences.focusPeakEndMinute) 0 else 90
            EnergyLevel.LOW,
            EnergyLevel.OVERWHELMED,
            -> if (start >= 17 * 60) 15 else 45
            EnergyLevel.STEADY -> 0
        }
        val latePenalty = if (start >= 20 * 60) 120 else 0
        return preferredPenalty + energyPenalty + latePenalty
    }

    private fun reasonFor(request: SchedulingRequest, start: Int): String {
        val preferred = request.preferredStartMinute
        return if (preferred != null && start == preferred) {
            "Matches your preferred time and does not conflict with protected plans."
        } else {
            "Uses the best conflict-free slot while preserving fixed plans and buffers."
        }
    }

    private fun uuid(): String = UUID.randomUUID().toString()
}
