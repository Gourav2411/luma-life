# Luma 1.1 Android

Luma is an Android-first adaptive life planner for Indian college students. It combines goals, commitments, energy and real life, then proposes a feasible plan across academics, skills, health, relationships, fun and self-direction.

## Implemented foundation

- Kotlin 2.3 and Jetpack Compose multi-module architecture
- Four destinations: Today, Plans, Grow and Luma
- Five planning levels: Direction, Semester, Week, Day and Focus Session
- Splash, welcome, authentication entry, permissions and guided life setup
- Today outcomes, capacity, timeline, conflicts, energy check-in and quick capture
- Speech recognition with typed fallback and `en-IN` language hints
- Structured assistant contract and visible offline fallback
- Deterministic scheduling with waking windows, travel buffers and overlap prevention
- Before/after proposal review and partial approval
- Room-backed offline plan cache
- Focus timer, skill roadmap/evidence, reflection, calendar, memory and privacy screens
- Light/dark adaptive design system with accessible semantics
- Premium bright visual system with editorial type, acid hero surfaces, hard offset depth, tactile navigation and animated voice feedback
- No OpenAI, Supabase service-role or calendar secrets in the APK

The installable alpha offers an explicitly labeled product-demo path. Google OAuth, email OTP, cloud sync, realtime voice and Google Calendar require the companion service plus deployment credentials; they are not simulated locally.

## Modules

```text
app
core:model
core:planning
core:ai
core:data
core:designsystem
feature:today
feature:plans
feature:grow
feature:luma
```

## Build and test

Use JDK 17, Android SDK/Build Tools 36.0.0, Gradle 8.13 and AGP 8.13.2.

```bash
gradle :core:planning:test :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Trust boundary

GPT never writes plan blocks or calendar events. The cloud interpreter returns structured intent and constraints; the deterministic scheduler creates and validates a draft proposal; the explanation model explains only that validated proposal. The student must approve all or selected changes before the repository applies them. External calendar writes are queued only after approval.
