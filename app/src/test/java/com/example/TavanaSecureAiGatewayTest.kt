package com.example

import com.tavana.studio.ai.gateway.AiGatewayVocalRequest
import com.tavana.studio.ai.gateway.AiGatewayVocalResponse
import com.tavana.studio.ai.gateway.HttpSecureAiGateway
import com.tavana.studio.ai.gateway.SecureAiGateway
import com.tavana.studio.foundation.offline.OfflineSafetyGuard
import com.tavana.studio.foundation.offline.StudioFeature
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TavanaSecureAiGatewayTest {

    @Test
    fun testGatewayContract_NeverExposesProviderKeys() {
        // Verify contract payload has zero provider keys
        val request = AiGatewayVocalRequest(
            overallScore = 88,
            pitchAccuracy = 92,
            timingAccuracy = 84,
            stabilityScore = 80,
            detectedKey = "A Minor",
            languageCode = "fa"
        )

        assertEquals(88, request.overallScore)
        assertEquals("fa", request.languageCode)
        // Ensure data class definition does not carry secret keys
        val fieldNames = AiGatewayVocalRequest::class.java.declaredFields.map { it.name }
        assertFalse("Request must not carry API keys", fieldNames.any { it.contains("apiKey", ignoreCase = true) })
    }

    @Test
    fun testGatewayAdapter_UnconfiguredGracefullyFailsWithoutCrash() = runBlocking {
        // When unconfigured or placeholder endpoint is passed, it must fail safely
        val unconfiguredGateway = HttpSecureAiGateway(gatewayUrl = "")
        assertFalse(unconfiguredGateway.isConfigured())

        val request = AiGatewayVocalRequest(
            overallScore = 80,
            pitchAccuracy = 85,
            timingAccuracy = 75,
            stabilityScore = 80
        )

        val result = unconfiguredGateway.requestVocalCoachFeedback(request)
        assertTrue("Unconfigured gateway should return failure result rather than throwing", result.isFailure)
    }

    @Test
    fun testOfflineFallback_ExecutesSafelyWhenOffline() {
        val isOnline = false
        var fallbackCalled = false
        var cloudCalled = false

        val response = OfflineSafetyGuard.executeSafe(
            isOnline = isOnline,
            onLocalFallback = {
                fallbackCalled = true
                "Local Deterministic Coach"
            },
            onCloudAction = {
                cloudCalled = true
                "Cloud AI Feedback"
            }
        )

        assertTrue(fallbackCalled)
        assertFalse(cloudCalled)
        assertEquals("Local Deterministic Coach", response)
    }

    @Test
    fun testMockSecureGateway_SimulatedSuccess() = runBlocking {
        val mockGateway = object : SecureAiGateway {
            override suspend fun requestVocalCoachFeedback(
                request: AiGatewayVocalRequest,
                authToken: String?,
                appCheckToken: String?
            ): Result<AiGatewayVocalResponse> {
                return Result.success(
                    AiGatewayVocalResponse(
                        success = true,
                        feedback = "صدای شما بسیار دلنشین است و گام‌ها با دقت بالایی اجرا شده‌اند.",
                        coachingTips = listOf("تمرین تنفس دیافراگمی", "کنترل رزونانس سینه"),
                        source = "SECURE_GATEWAY_GEMINI"
                    )
                )
            }

            override fun getGatewayEndpoint(): String = "https://gateway.tavana.studio/api/v1/ai/coach"
            override fun isConfigured(): Boolean = true
        }

        val request = AiGatewayVocalRequest(
            overallScore = 95,
            pitchAccuracy = 96,
            timingAccuracy = 94,
            stabilityScore = 90,
            detectedKey = "D Major",
            languageCode = "fa"
        )

        val result = mockGateway.requestVocalCoachFeedback(request)
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("SECURE_GATEWAY_GEMINI", response?.source)
        assertEquals(2, response?.coachingTips?.size)
    }
}
