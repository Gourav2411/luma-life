package com.luma.ai

/**
 * Production transport seam. The API key never belongs here: Android sends an authenticated
 * request to the Luma backend, which performs Responses API orchestration server-side.
 */
class RemoteAssistantGateway(
    private val baseUrl: String,
    private val bearerTokenProvider: suspend () -> String,
) : AssistantGateway {
    override suspend fun turn(request: AssistantTurnRequest): AssistantTurnResponse {
        check(baseUrl.isNotBlank()) { "LUMA_API_BASE_URL is not configured." }
        bearerTokenProvider()
        error("Remote transport is disabled in the credential-free alpha build.")
    }
}
