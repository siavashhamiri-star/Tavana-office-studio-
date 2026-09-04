# TAVANA Studio — Secure AI Gateway Deployment Guide

## Architecture Overview
```
[User / Android APK]
         │
         │  (Metrics & Language only; No Provider API Key)
         ▼
[TAVANA Secure AI Gateway (Firebase Cloud Functions / Cloud Run)]
   ├── App Check verification (reCAPTCHA Enterprise / Play Integrity)
   ├── Rate Limiting (30 requests/minute per client)
   └── Server Secret Manager (GEMINI_API_KEY never leaves server)
         │
         ▼
[Google Gemini API Provider]
```

## Current Deployment Status: `PREPARED (PENDING CREDENTIALS)`

The Android client and backend code are 100% prepared and decoupled:
- **Client Side:** No API Key is embedded or required in the APK/AAB.
- **Server Side:** Cloud Function implementation is ready in `backend/gateway/index.js`.

### Why Gateway cannot be auto-deployed right now (Blockers):
1. **Google Cloud / Firebase Project Association:** No active GCP service account key or Firebase CLI session is logged in within this container.
2. **Secret Manager Key:** The production `GEMINI_API_KEY` must be saved in your Google Cloud Secret Manager (`firebase functions:secrets:set GEMINI_API_KEY`).

### Step-by-Step Deployment (When ready):
```bash
cd backend/gateway
npm install
firebase login
firebase use <YOUR-FIREBASE-PROJECT-ID>
firebase functions:secrets:set GEMINI_API_KEY
firebase deploy --only functions:tavanaAiGateway
```
After deployment, set `AI_GATEWAY_URL` in your GitHub Repository Secrets or `.env`.
