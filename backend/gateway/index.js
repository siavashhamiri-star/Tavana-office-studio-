/**
 * TAVANA Studio — Secure AI Gateway
 * Reference Implementation for Firebase Cloud Functions v2 / Google Cloud Run.
 *
 * ARCHITECTURAL INVARIANT:
 * The Gemini API Key resides EXCLUSIVELY in Google Secret Manager or Server Environment.
 * It is NEVER transmitted to the client, logged, or returned in response payloads.
 */

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const { GoogleGenAI } = require("@google/genai");

if (!admin.apps.length) {
  admin.initializeApp();
}

// In-memory token bucket rate limiter per IP / App Instance (max 30 requests/min)
const rateLimitMap = new Map();
const RATE_LIMIT_WINDOW_MS = 60 * 1000;
const MAX_REQUESTS_PER_WINDOW = 30;

function checkRateLimit(clientId) {
  const now = Date.now();
  const clientData = rateLimitMap.get(clientId) || { count: 0, resetTime: now + RATE_LIMIT_WINDOW_MS };

  if (now > clientData.resetTime) {
    clientData.count = 1;
    clientData.resetTime = now + RATE_LIMIT_WINDOW_MS;
    rateLimitMap.set(clientId, clientData);
    return true;
  }

  if (clientData.count >= MAX_REQUESTS_PER_WINDOW) {
    return false;
  }

  clientData.count++;
  rateLimitMap.set(clientId, clientData);
  return true;
}

exports.tavanaAiGateway = onRequest(
  {
    cors: false,
    secrets: ["GEMINI_API_KEY"],
    maxInstances: 10,
    region: "us-central1"
  },
  async (req, res) => {
    // 1. Enforce POST method
    if (req.method !== "POST") {
      return res.status(405).json({ success: false, error: "Method Not Allowed" });
    }

    // 2. Client verification via Firebase App Check (if header provided)
    const appCheckToken = req.header("X-Firebase-AppCheck");
    if (appCheckToken) {
      try {
        await admin.appCheck().verifyToken(appCheckToken);
      } catch (err) {
        return res.status(401).json({ success: false, error: "Invalid App Check attestation token." });
      }
    }

    // 3. Apply Rate Limiting
    const clientIdentifier = req.ip || req.header("x-forwarded-for") || "anonymous";
    if (!checkRateLimit(clientIdentifier)) {
      return res.status(429).json({ success: false, error: "Rate limit exceeded. Please try again in 1 minute." });
    }

    // 4. Validate incoming vocal performance metrics (sanitized, zero-key contract)
    const { overallScore, pitchAccuracy, timingAccuracy, stabilityScore, detectedKey, languageCode } = req.body || {};

    if (overallScore === undefined || pitchAccuracy === undefined) {
      return res.status(400).json({ success: false, error: "Missing required vocal performance metrics." });
    }

    // 5. Access Server-Side Secret safely
    const serverApiKey = process.env.GEMINI_API_KEY;
    if (!serverApiKey) {
      return res.status(503).json({
        success: false,
        error: "Gateway configuration incomplete: Server secret GEMINI_API_KEY is not provisioned."
      });
    }

    try {
      const ai = new GoogleGenAI({ apiKey: serverApiKey });
      const prompt = `You are a world-class professional vocal coach for TAVANA Studio.
Evaluate this singer's performance based on deterministic acoustic analysis:
- Overall Score: ${overallScore}/100
- Pitch Accuracy: ${pitchAccuracy}/100
- Timing Accuracy: ${timingAccuracy}/100
- Sustained Stability: ${stabilityScore}/100
- Musical Key: ${detectedKey || "Unknown"}
- Preferred Language: ${languageCode || "fa"}

Provide brief, highly encouraging, and specific vocal advice.
Output in JSON format with:
- "feedback": 2 concise sentences of constructive vocal coaching in the requested language.
- "coachingTips": Array of 2 actionable physical vocal exercises (e.g. diaphragmatic support, lip trills).
- "vocalToneSuggestion": 1 short phrase characterizing their vocal placement.`;

      const response = await ai.models.generateContent({
        model: "gemini-2.5-flash",
        contents: prompt,
        config: {
          responseMimeType: "application/json",
          temperature: 0.4
        }
      });

      const parsedResponse = JSON.parse(response.text.trim());

      // 6. Return sanitized result without exposing any server credentials
      return res.status(200).json({
        success: true,
        feedback: parsedResponse.feedback || "Great performance! Keep practicing pitch stability.",
        coachingTips: parsedResponse.coachingTips || ["Focus on diaphragmatic breathing.", "Perform gentle lip trills before singing."],
        vocalToneSuggestion: parsedResponse.vocalToneSuggestion || "Resonant Chest-Mix",
        source: "SECURE_GATEWAY_GEMINI"
      });
    } catch (error) {
      console.error("AI Gateway processing error:", error.message);
      return res.status(500).json({
        success: false,
        error: "AI Gateway upstream failure. Client should fallback to local coach."
      });
    }
  }
);
