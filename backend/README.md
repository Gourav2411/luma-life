# Luma planning service

This service is the security boundary for identity, GPT reasoning and calendar writes. The Android app never receives OpenAI, Supabase service-role or Google Calendar secrets.

## Pipeline

1. Authenticate the Supabase bearer token and load only the caller's RLS-scoped context.
2. Use the extraction model through the Responses API. It must function-call for authenticated planning context, then return a strict `InterpretedTurn`.
3. Pass only structured requests to the deterministic scheduler.
4. Validate waking windows, overlaps, fixed/protected plans and travel buffers.
5. Ask the planning model to explain the validated proposal without changing it.
6. Persist the proposal as a draft. No plan or calendar write occurs here.
7. Apply only approved change IDs through one Postgres transaction.
8. Queue approved external-calendar changes in an idempotent outbox.

## Local setup

```bash
cp .env.example .env
npm install
npm test
npm run typecheck
npm run dev
```

Apply `supabase/migrations/202607200001_luma_foundation.sql` to a new Supabase project. Configure Google OAuth and email OTP in Supabase Auth. Calendar OAuth refresh tokens must be encrypted before insertion; production should use a managed KMS/envelope-encryption worker.

The model registry is environment-controlled so model identifiers can change without an APK release. Realtime voice is a conversational transport only; all plan creation still returns through the structured scheduler pipeline.
