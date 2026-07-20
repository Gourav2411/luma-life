package com.luma.ai

import com.luma.model.EnergyLevel
import com.luma.model.LifeArea
import com.luma.model.MemoryFact
import com.luma.model.PlanHorizon
import com.luma.model.PlanItem
import com.luma.model.PlanProposal
import kotlinx.serialization.Serializable

@Serializable
data class AssistantInput(
    val type: String = "text",
    val text: String,
)

@Serializable
data class AssistantTurnRequest(
    val conversationId: String,
    val input: AssistantInput,
    val horizon: PlanHorizon,
    val clientContextVersion: Long,
    val anchorDate: String,
    val existingItems: List<PlanItem>,
)

@Serializable
data class ExtractedContext(
    val label: String,
    val value: String,
    val lifeArea: LifeArea?,
)

@Serializable
data class AssistantTurnResponse(
    val assistantMessage: String,
    val clarification: String? = null,
    val extractedContext: List<ExtractedContext>,
    val proposal: PlanProposal? = null,
    val memoryCandidates: List<MemoryFact> = emptyList(),
    val traceId: String,
    val usedOfflineFallback: Boolean,
)

interface AssistantGateway {
    suspend fun turn(request: AssistantTurnRequest): AssistantTurnResponse
}

@Serializable
data class IntentExtraction(
    val goal: String?,
    val deadline: String?,
    val preferredStartMinute: Int?,
    val durationMinutes: Int?,
    val lifeArea: LifeArea?,
    val energy: EnergyLevel?,
    val constraints: List<String>,
    val uncertainty: List<String>,
)
