package com.tavana.studio.ai.gateway

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Contract defining the client-to-gateway AI communication.
 *
 * The client NEVER holds or passes Gemini provider keys.
 * All communication terminates at the trusted TAVANA Secure AI Gateway (Cloud Function / Cloud Run).
 */
interface SecureAiGateway {
    /**
     * Dispatches singing/pitch metrics to the Secure AI Gateway for personalized AI vocal coaching feedback.
     */
    suspend fun requestVocalCoachFeedback(
        request: AiGatewayVocalRequest,
        authToken: String? = null,
        appCheckToken: String? = null
    ): Result<AiGatewayVocalResponse>

    /**
     * Returns the active gateway URL endpoint.
     */
    fun getGatewayEndpoint(): String

    /**
     * Checks if the gateway URL has been configured.
     */
    fun isConfigured(): Boolean
}

/**
 * Production-ready implementation of [SecureAiGateway] communicating over HTTPS.
 *
 * Security Characteristics:
 * 1. Zero Provider Keys: No Gemini API Key exists in the client binary or request body.
 * 2. Header-based Security: Accepts ephemeral Firebase App Check and/or user auth tokens.
 * 3. Graceful Degradation: If server is unavailable, returns [Result.failure] without throwing unhandled exceptions.
 */
class HttpSecureAiGateway(
    private val gatewayUrl: String = DEFAULT_GATEWAY_URL,
    private val httpClient: OkHttpClient = defaultClient()
) : SecureAiGateway {

    override fun getGatewayEndpoint(): String = gatewayUrl

    override fun isConfigured(): Boolean {
        return gatewayUrl.isNotBlank() && gatewayUrl.startsWith("https://") && !gatewayUrl.contains("placeholder")
    }

    override suspend fun requestVocalCoachFeedback(
        request: AiGatewayVocalRequest,
        authToken: String?,
        appCheckToken: String?
    ): Result<AiGatewayVocalResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("AI Gateway is not deployed or endpoint is unconfigured. Transparently falling back to local deterministic coach.")
            )
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("overallScore", request.overallScore)
                put("pitchAccuracy", request.pitchAccuracy)
                put("timingAccuracy", request.timingAccuracy)
                put("stabilityScore", request.stabilityScore)
                put("detectedKey", request.detectedKey ?: "Unknown")
                put("languageCode", request.languageCode)
                put("contextNote", request.contextNote ?: "")
            }

            val requestBody = jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE)

            val httpRequestBuilder = Request.Builder()
                .url(gatewayUrl)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Tavana-Client-Platform", "Android")

            // Attach App Check token if available
            appCheckToken?.let { token ->
                httpRequestBuilder.header("X-Firebase-AppCheck", token)
            }

            // Attach Firebase user auth token if available
            authToken?.let { token ->
                httpRequestBuilder.header("Authorization", "Bearer $token")
            }

            httpClient.newCall(httpRequestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Gateway responded with HTTP ${response.code}: ${response.message}")
                    )
                }

                val responseBodyString = response.body?.string()
                    ?: return@withContext Result.failure(IOException("Empty response body from AI Gateway"))

                val responseJson = JSONObject(responseBodyString)
                val success = responseJson.optBoolean("success", true)
                val feedback = responseJson.optString("feedback", "")
                val vocalToneSuggestion = if (responseJson.has("vocalToneSuggestion")) responseJson.optString("vocalToneSuggestion") else null

                val tips = mutableListOf<String>()
                val tipsArray = responseJson.optJSONArray("coachingTips")
                if (tipsArray != null) {
                    for (i in 0 until tipsArray.length()) {
                        tips.add(tipsArray.getString(i))
                    }
                }

                val parsed = AiGatewayVocalResponse(
                    success = success,
                    feedback = feedback,
                    coachingTips = tips,
                    vocalToneSuggestion = vocalToneSuggestion,
                    source = responseJson.optString("source", "SECURE_GATEWAY_GEMINI")
                )

                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_GATEWAY_URL = "https://gateway.tavana.studio/api/v1/ai/coach"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        }
    }
}
