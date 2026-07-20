package com.luma.planning

import com.luma.model.EnergyLevel
import com.luma.model.Flexibility
import com.luma.model.LifeArea
import com.luma.model.PlanItem
import com.luma.model.PlanSource
import com.luma.model.PlanStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeterministicSchedulerTest {
    private val scheduler = DeterministicScheduler()

    @Test
    fun `exam football date and low energy yields feasible protected plan`() {
        val date = "2026-07-20"
        val existing = listOf(
            item("Football", date, 18 * 60, 75, Flexibility.PROTECTED, LifeArea.HEALTH),
            item("Date night", date, 20 * 60 + 30, 90, Flexibility.PROTECTED, LifeArea.RELATIONSHIPS),
        )
        val proposal = scheduler.createProposal(
            existing = existing,
            requests = listOf(
                SchedulingRequest(
                    title = "Exam review",
                    date = date,
                    durationMinutes = 35,
                    lifeArea = LifeArea.ACADEMICS,
                    preferredStartMinute = 19 * 60 + 30,
                    latestEndMinute = 20 * 60 + 20,
                    energy = EnergyLevel.LOW,
                ),
            ),
            assumptions = listOf("Football ends at 7:15 PM"),
        )

        assertTrue(proposal.isFeasible)
        assertTrue(proposal.changes.single().after!!.endMinute!! <= 20 * 60 + 20)
        assertTrue(scheduler.validate(existing + proposal.changes.mapNotNull { it.after }).isValid)
    }

    @Test
    fun `impossible workload returns unresolved question`() {
        val date = "2026-07-20"
        val existing = listOf(
            item("All-day class", date, 6 * 60, 16 * 60, Flexibility.FIXED, LifeArea.ACADEMICS),
        )
        val proposal = scheduler.createProposal(
            existing,
            listOf(
                SchedulingRequest(
                    title = "Portfolio sprint",
                    date = date,
                    durationMinutes = 60,
                    lifeArea = LifeArea.SKILLS,
                    preferredStartMinute = null,
                ),
            ),
        )
        assertFalse(proposal.isFeasible)
        assertTrue(proposal.unresolvedQuestions.isNotEmpty())
    }

    @Test
    fun `overlap validation includes travel buffers`() {
        val date = "2026-07-20"
        val first = item("Lecture", date, 9 * 60, 60, Flexibility.FIXED, LifeArea.ACADEMICS)
            .copy(travelAfterMinutes = 20)
        val second = item("Gym", date, 10 * 60 + 10, 45, Flexibility.FLEXIBLE, LifeArea.HEALTH)
        assertFalse(scheduler.validate(listOf(first, second)).isValid)
    }

    @Test
    fun `same time on different dates is not a conflict`() {
        val firstDay = item(
            "Monday class",
            "2026-07-20",
            9 * 60,
            60,
            Flexibility.FIXED,
            LifeArea.ACADEMICS,
        )
        val secondDay = item(
            "Tuesday class",
            "2026-07-21",
            9 * 60,
            60,
            Flexibility.FIXED,
            LifeArea.ACADEMICS,
        )

        assertTrue(scheduler.validate(listOf(firstDay, secondDay)).isValid)
    }

    private fun item(
        title: String,
        date: String,
        start: Int,
        duration: Int,
        flexibility: Flexibility,
        area: LifeArea,
    ) = PlanItem(
        id = title,
        title = title,
        date = date,
        startMinute = start,
        durationMinutes = duration,
        lifeArea = area,
        flexibility = flexibility,
        source = PlanSource.USER,
        status = PlanStatus.ACTIVE,
        energy = EnergyLevel.STEADY,
    )
}
