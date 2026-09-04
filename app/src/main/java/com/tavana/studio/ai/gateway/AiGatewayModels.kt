package com.tavana.studio.ai.gateway

/**
 * Data structures for communication between the TAVANA Studio client and the
 * Server-Side Secure AI Gateway.
 *
 * CRITICAL SECURITY INVARIANT:
 * These structures MUST NEVER contain provider API keys (Gemini / OpenAI).
 * Authentication is performed server-side or via ephemeral App Check / Firebase Auth tokens.
 */
data class AiGatewayVocalRequest(
    val overallScore: Int,
    val pitchAccuracy: Int,
    val timingAccuracy: Int,
    val stabilityScore: Int,
    val detectedKey: String? = null,
    val languageCode: String = "fa",
    val contextNote: String? = null
)

data class AiGatewayVocalResponse(
    val success: Boolean,
    val feedback: String,
    val coachingTips: List<String> = emptyList(),
    val vocalToneSuggestion: String? = null,
    val source: String = "SECURE_GATEWAY_GEMINI"
)

enum class GatewayDeploymentStatus {
    /** Gateway interface & client adapter are fully wired; waiting for live server deploy */
    PREPARED,
    /** Live server-side gateway endpoint is responding and healthy */
    LIVE,
    /** Network is offline or host is unreachable; application falls back safely */
    OFFLINE_FALLBACK
}
