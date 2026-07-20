package com.luma.collegelife

import com.luma.model.DayPlan
import com.luma.model.Direction
import com.luma.model.EnergyLevel
import com.luma.model.Flexibility
import com.luma.model.FocusSession
import com.luma.model.FocusStep
import com.luma.model.LifeArea
import com.luma.model.PlanItem
import com.luma.model.PlanSource
import com.luma.model.PlanStatus
import com.luma.model.SemesterOutcome
import com.luma.model.SemesterPlan
import com.luma.model.SkillMilestone
import com.luma.model.SkillRoadmap
import com.luma.model.WeekPlan
import java.time.LocalDate

object DemoData {
    val today: String
        get() = LocalDate.now().toString()

    fun dayItems(date: String = today): List<PlanItem> = listOf(
        item("class-dsa", "Data Structures class", date, 9 * 60, 60, LifeArea.ACADEMICS, Flexibility.FIXED, PlanSource.CALENDAR, EnergyLevel.STEADY, "Semester fundamentals"),
        item("exam-review", "Review graphs for Friday exam", date, 10 * 60 + 30, 45, LifeArea.ACADEMICS, Flexibility.FLEXIBLE, PlanSource.USER, EnergyLevel.HIGH, "Score through understanding"),
        item("lunch", "Lunch + reset", date, 13 * 60, 45, LifeArea.HEALTH, Flexibility.PROTECTED, PlanSource.USER, EnergyLevel.LOW, "Keep energy steady"),
        item("portfolio", "Portfolio case-study sprint", date, 15 * 60, 45, LifeArea.SKILLS, Flexibility.FLEXIBLE, PlanSource.LUMA, EnergyLevel.STEADY, "Internship-ready portfolio"),
        item("football", "Football with hostel crew", date, 18 * 60, 75, LifeArea.HEALTH, Flexibility.PROTECTED, PlanSource.CALENDAR, EnergyLevel.HIGH, "Move and stay connected", travelBefore = 15, travelAfter = 15),
        item("date-night", "Date night", date, 20 * 60 + 30, 90, LifeArea.RELATIONSHIPS, Flexibility.PROTECTED, PlanSource.USER, EnergyLevel.STEADY, "Be present with people who matter"),
    )

    val direction = Direction(
        id = "direction-1",
        statement = "Become a thoughtful product engineer without shrinking the rest of my life.",
        lifeAreas = LifeArea.entries.toSet(),
    )

    val semester = SemesterPlan(
        id = "semester-1",
        label = "Monsoon semester · Year 2",
        startDate = "Jul 2026",
        endDate = "Nov 2026",
        directionId = direction.id,
        outcomes = listOf(
            SemesterOutcome("outcome-dsa", "Own DSA fundamentals", LifeArea.ACADEMICS, "Explain and solve 40 representative problems", 42),
            SemesterOutcome("outcome-product", "Ship one real product", LifeArea.SKILLS, "Deployed app plus a written case study", 35),
            SemesterOutcome("outcome-energy", "Build a sustainable week", LifeArea.HEALTH, "Four weeks with sleep and sport protected", 58),
        ),
    )

    fun week(items: List<PlanItem>): WeekPlan {
        val start = LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())
        val days = (0L..4L).map { offset ->
            val date = start.plusDays(offset).toString()
            DayPlan(
                id = "day-$date",
                date = date,
                topOutcomeIds = items.take(3).map(PlanItem::id),
                items = if (date == today) items else emptyList(),
                energy = EnergyLevel.STEADY,
                capacityMinutes = 8 * 60,
                syncVersion = 1,
            )
        }
        return WeekPlan(
            id = "week-$start",
            startDate = start.toString(),
            priorities = listOf(
                "Prepare calmly for Friday’s graphs exam",
                "Ship the portfolio problem statement",
                "Protect football, one social evening and recovery",
            ),
            capacityMinutes = 38 * 60,
            committedMinutes = 27 * 60,
            days = days,
        )
    }

    val focusSession = FocusSession(
        id = "focus-portfolio",
        planItemId = "portfolio",
        outcome = "Draft a clear portfolio problem statement",
        durationMinutes = 45,
        steps = listOf(
            FocusStep("step-1", "Write the user and problem in one sentence", true),
            FocusStep("step-2", "Add three observations from research", false),
            FocusStep("step-3", "Choose one screenshot as evidence", false),
        ),
        evidencePrompt = "Attach the draft or paste its link. Progress is evidence, not a streak.",
    )

    val roadmap = SkillRoadmap(
        id = "roadmap-product",
        title = "Product engineering",
        targetRole = "Frontend / product engineering internship",
        progressPercent = 38,
        milestones = listOf(
            SkillMilestone("milestone-1", "Build accessible UI foundations", "Component library and accessibility audit", PlanStatus.COMPLETED),
            SkillMilestone("milestone-2", "Ship an end-to-end product", "Working deployment with analytics", PlanStatus.ACTIVE),
            SkillMilestone("milestone-3", "Tell the product story", "Case study explaining decisions and trade-offs", PlanStatus.DRAFT),
        ),
    )

    private fun item(
        id: String,
        title: String,
        date: String,
        start: Int,
        duration: Int,
        area: LifeArea,
        flexibility: Flexibility,
        source: PlanSource,
        energy: EnergyLevel,
        parent: String,
        travelBefore: Int = 0,
        travelAfter: Int = 0,
    ) = PlanItem(
        id = id,
        title = title,
        date = date,
        startMinute = start,
        durationMinutes = duration,
        lifeArea = area,
        flexibility = flexibility,
        source = source,
        status = PlanStatus.ACTIVE,
        energy = energy,
        parentGoalId = "goal-${area.name.lowercase()}",
        parentLabel = parent,
        travelBeforeMinutes = travelBefore,
        travelAfterMinutes = travelAfter,
    )
}
