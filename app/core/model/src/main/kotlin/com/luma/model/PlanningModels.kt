package com.luma.model

import kotlinx.serialization.Serializable

@Serializable
enum class LifeArea {
    ACADEMICS,
    SKILLS,
    HEALTH,
    RELATIONSHIPS,
    FUN,
    SELF_DIRECTION,
}

@Serializable
enum class Flexibility {
    FIXED,
    FLEXIBLE,
    PROTECTED,
}

@Serializable
enum class PlanSource {
    USER,
    CALENDAR,
    LUMA,
}

@Serializable
enum class PlanStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    SKIPPED,
    RESCHEDULED,
    ARCHIVED,
}

@Serializable
enum class PlanHorizon {
    DIRECTION,
    SEMESTER,
    WEEK,
    DAY,
    SESSION,
}

@Serializable
enum class EnergyLevel {
    LOW,
    STEADY,
    HIGH,
    OVERWHELMED,
}

@Serializable
enum class ChangeAction {
    CREATE,
    MOVE,
    RESIZE,
    DELETE,
    PROTECT,
}

@Serializable
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val locale: String = "en-IN",
    val timeZone: String = "Asia/Kolkata",
    val collegeStage: String = "Undergraduate",
    val sleepStartMinute: Int = 22 * 60 + 30,
    val sleepEndMinute: Int = 6 * 60 + 30,
    val preferredFocusMinutes: Int = 45,
)

@Serializable
data class LifeAreaPreference(
    val area: LifeArea,
    val protectionWeight: Int,
    val isProtected: Boolean,
)

@Serializable
data class Direction(
    val id: String,
    val statement: String,
    val horizonMonths: Int = 12,
    val lifeAreas: Set<LifeArea>,
    val status: PlanStatus = PlanStatus.ACTIVE,
)

@Serializable
data class SemesterPlan(
    val id: String,
    val label: String,
    val startDate: String,
    val endDate: String,
    val directionId: String,
    val outcomes: List<SemesterOutcome>,
    val status: PlanStatus = PlanStatus.ACTIVE,
)

@Serializable
data class SemesterOutcome(
    val id: String,
    val title: String,
    val lifeArea: LifeArea,
    val evidenceTarget: String,
    val progressPercent: Int,
)

@Serializable
data class Goal(
    val id: String,
    val title: String,
    val lifeArea: LifeArea,
    val parentId: String?,
    val targetDate: String?,
    val status: PlanStatus = PlanStatus.ACTIVE,
)

@Serializable
data class PlanItem(
    val id: String,
    val title: String,
    val date: String,
    val startMinute: Int?,
    val durationMinutes: Int,
    val lifeArea: LifeArea,
    val flexibility: Flexibility,
    val source: PlanSource,
    val status: PlanStatus,
    val energy: EnergyLevel,
    val parentGoalId: String? = null,
    val parentLabel: String? = null,
    val travelBeforeMinutes: Int = 0,
    val travelAfterMinutes: Int = 0,
    val notes: String? = null,
) {
    val endMinute: Int?
        get() = startMinute?.plus(durationMinutes)
}

@Serializable
data class DayPlan(
    val id: String,
    val date: String,
    val topOutcomeIds: List<String>,
    val items: List<PlanItem>,
    val energy: EnergyLevel,
    val capacityMinutes: Int,
    val syncVersion: Long,
)

@Serializable
data class WeekPlan(
    val id: String,
    val startDate: String,
    val priorities: List<String>,
    val capacityMinutes: Int,
    val committedMinutes: Int,
    val days: List<DayPlan>,
)

@Serializable
data class FocusStep(
    val id: String,
    val title: String,
    val isComplete: Boolean,
)

@Serializable
data class FocusSession(
    val id: String,
    val planItemId: String,
    val outcome: String,
    val durationMinutes: Int,
    val steps: List<FocusStep>,
    val evidencePrompt: String,
)

@Serializable
data class SkillRoadmap(
    val id: String,
    val title: String,
    val targetRole: String,
    val progressPercent: Int,
    val milestones: List<SkillMilestone>,
)

@Serializable
data class SkillMilestone(
    val id: String,
    val title: String,
    val evidence: String,
    val status: PlanStatus,
)

@Serializable
data class MemoryFact(
    val id: String,
    val category: String,
    val statement: String,
    val isConfirmed: Boolean,
    val isSensitive: Boolean,
    val createdAt: String,
)

@Serializable
data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val createdAt: String,
)

@Serializable
data class PlanChange(
    val id: String,
    val action: ChangeAction,
    val targetId: String?,
    val before: PlanItem?,
    val after: PlanItem?,
    val reason: String,
    val affectedLifeAreas: Set<LifeArea>,
    val risk: RiskLevel,
    val isSelected: Boolean = true,
)

@Serializable
data class PlanProposal(
    val id: String,
    val horizon: PlanHorizon,
    val summary: String,
    val assumptions: List<String>,
    val changes: List<PlanChange>,
    val unresolvedQuestions: List<String>,
    val confidence: Double,
    val isFeasible: Boolean,
    val validationMessages: List<String>,
)

@Serializable
data class Reflection(
    val id: String,
    val date: String,
    val energy: EnergyLevel,
    val workedWell: String,
    val needsAdjustment: String,
    val evidenceIds: List<String>,
)
