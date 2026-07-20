package com.luma.ai

import com.luma.model.EnergyLevel
import com.luma.model.LifeArea
import com.luma.model.MemoryFact
import com.luma.planning.DeterministicScheduler
import com.luma.planning.SchedulingRequest
import java.time.Instant
import java.util.Locale
import java.util.UUID

/**
 * Credential-free fallback used by the installable alpha. It exercises the same structured
 * contracts and deterministic scheduler as the cloud path, while clearly reporting that the
 * response was generated offline.
 */
class DemoAssistantGateway(
    private val scheduler: DeterministicScheduler,
) : AssistantGateway {
    override suspend fun turn(request: AssistantTurnRequest): AssistantTurnResponse {
        val extraction = extract(request.input.text)
        val contexts = buildList {
            extraction.deadline?.let { add(ExtractedContext("Exam", it, LifeArea.ACADEMICS)) }
            extraction.constraints.filter { it.contains("football", true) }.forEach {
                add(ExtractedContext("Football", "6:00 PM", LifeArea.HEALTH))
            }
            extraction.constraints.filter { it.contains("date", true) }.forEach {
                add(ExtractedContext("Date", "8:30 PM", LifeArea.RELATIONSHIPS))
            }
            extraction.energy?.let { add(ExtractedContext("Energy", it.displayName(), LifeArea.HEALTH)) }
        }

        val schedulingRequests = buildList {
            if (extraction.lifeArea == LifeArea.ACADEMICS || extraction.deadline != null) {
                add(
                    SchedulingRequest(
                        title = extraction.goal ?: "Exam review",
                        date = request.anchorDate,
                        durationMinutes = extraction.durationMinutes ?: 35,
                        lifeArea = LifeArea.ACADEMICS,
                        preferredStartMinute = extraction.preferredStartMinute,
                        latestEndMinute = 22 * 60,
                        energy = extraction.energy ?: EnergyLevel.STEADY,
                    ),
                )
            } else {
                add(
                    SchedulingRequest(
                        title = extraction.goal ?: "Portfolio focus sprint",
                        date = request.anchorDate,
                        durationMinutes = extraction.durationMinutes ?: 45,
                        lifeArea = extraction.lifeArea ?: LifeArea.SKILLS,
                        preferredStartMinute = extraction.preferredStartMinute,
                        energy = extraction.energy ?: EnergyLevel.STEADY,
                    ),
                )
            }
        }

        val proposal = scheduler.createProposal(
            existing = request.existingItems,
            requests = schedulingRequests,
            assumptions = extraction.constraints.map(::normaliseAssumption),
        )
        val message = if (proposal.isFeasible) {
            "I found a conflict-free option. I kept protected plans unchanged and prepared a review before anything is applied."
        } else {
            "The full request does not fit safely yet. I’ve kept your protected plans and surfaced the trade-off I need you to resolve."
        }
        val memoryCandidates = if (request.input.text.contains("always", ignoreCase = true)) {
            listOf(
                MemoryFact(
                    id = UUID.randomUUID().toString(),
                    category = "planning_preference",
                    statement = request.input.text,
                    isConfirmed = false,
                    isSensitive = false,
                    createdAt = Instant.now().toString(),
                ),
            )
        } else {
            emptyList()
        }

        return AssistantTurnResponse(
            assistantMessage = message,
            clarification = proposal.unresolvedQuestions.firstOrNull(),
            extractedContext = contexts,
            proposal = proposal,
            memoryCandidates = memoryCandidates,
            traceId = "offline-${UUID.randomUUID()}",
            usedOfflineFallback = true,
        )
    }

    private fun extract(text: String): IntentExtraction {
        val lower = text.lowercase(Locale.ROOT)
        val energy = when {
            "exhaust" in lower || "tired" in lower || "low energy" in lower -> EnergyLevel.LOW
            "overwhelm" in lower -> EnergyLevel.OVERWHELMED
            "energ" in lower -> EnergyLevel.HIGH
            else -> EnergyLevel.STEADY
        }
        val area = when {
            listOf("exam", "study", "class", "assignment").any(lower::contains) -> LifeArea.ACADEMICS
            listOf("portfolio", "project", "internship", "skill").any(lower::contains) -> LifeArea.SKILLS
            listOf("gym", "football", "workout", "walk").any(lower::contains) -> LifeArea.HEALTH
            listOf("date", "friend", "family").any(lower::contains) -> LifeArea.RELATIONSHIPS
            listOf("game", "gaming", "movie").any(lower::contains) -> LifeArea.FUN
            else -> LifeArea.SKILLS
        }
        val contexts = buildList {
            if ("football" in lower) add("Football at 6:00 PM")
            if ("date" in lower) add("Date at 8:30 PM")
            if ("gaming" in lower || "game" in lower) add("Gaming is protected fun")
        }
        val deadline = when {
            "exam" in lower && "friday" in lower -> "Friday"
            "exam" in lower -> "Upcoming"
            else -> null
        }
        val preferred = Regex("""\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""")
            .find(lower)
            ?.let { match ->
                val hour = match.groupValues[1].toInt()
                val minute = match.groupValues[2].ifBlank { "0" }.toInt()
                ((hour % 12) + if (match.groupValues[3] == "pm") 12 else 0) * 60 + minute
            }

        return IntentExtraction(
            goal = if (area == LifeArea.ACADEMICS) "Exam review" else null,
            deadline = deadline,
            preferredStartMinute = preferred,
            durationMinutes = if (energy == EnergyLevel.LOW) 35 else 45,
            lifeArea = area,
            energy = energy,
            constraints = contexts,
            uncertainty = emptyList(),
        )
    }

    private fun normaliseAssumption(value: String): String = when {
        value.contains("football", true) -> "Football ends at 7:15 PM"
        value.contains("date", true) -> "Date night starts at 8:30 PM"
        else -> value
    }

    private fun EnergyLevel.displayName(): String =
        name.lowercase().replaceFirstChar(Char::uppercase)
}
