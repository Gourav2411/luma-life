# Luma Life

Luma Life is an Android-first adaptive life planner for Indian college students.
It brings academics, skill development, health, relationships, rest and fun into
one realistic plan while keeping the student in control of every change.

## Product promise

Luma understands what is happening, combines it with the student's goals and
commitments, and proposes a feasible plan across five horizons:

1. Direction
2. Semester
3. Week
4. Day
5. Focus session

Every calendar-changing action follows **propose, explain, approve**. GPT
interprets and explains; a deterministic scheduler validates constraints and
creates the actual proposal.

## Repository

```text
app/       Kotlin and Jetpack Compose Android application
backend/   TypeScript planning service and Supabase migration
docs/      Approved visual-direction references
```

See [app/README.md](app/README.md) for Android setup and
[backend/README.md](backend/README.md) for service setup.

## Current foundation

- Premium bright Compose design system with Today, Plans, Grow and Luma
- Five-level planning hierarchy and segmented planning experience
- Deterministic interval scheduler with hard-constraint validation
- Structured assistant contracts and explicit proposal approval
- Room-backed offline plan cache
- Speech input with typed fallback
- Skill roadmaps, focus sessions and reflection flows
- Server-side OpenAI model registry and Supabase/Postgres foundation
- No service secrets embedded in the Android application

## Validation

The current alpha has been validated with:

- Android debug assembly
- Deterministic scheduler unit tests
- Android lint
- TypeScript tests and type checking

## Status

Luma Life is a production-foundation alpha. Cloud identity, deployed
synchronization, realtime voice, and Google Calendar credentials still require
environment-specific service configuration before public release.

