# Luma v0.2 — Archived Prototype Reference

Luma is a native Android MVP for a voice-first, whole-life companion for college students. It treats academics, employable skills, health, relationships, fun, and self-direction as one connected planning problem.

## What works in this prototype

- Branded launch splash and a clear value-proposition welcome screen
- Local prototype account creation, login, and guest access
- Three-step setup that explains Luma before showing the dashboard
- First-run life-direction and whole-life priorities onboarding
- A realistic day mixing classes, portfolio work, friends, sport, dating, and gaming
- Energy-aware replanning
- Android speech recognition and text-to-speech
- Typed fallback for devices without voice recognition
- Local intent handling for exams, low energy, workouts, gaming, going out, and personal goals
- Add, protect, move, complete, and remove daily events
- Six-dimension weekly growth view
- Local-only profile storage

## Build

Use JDK 17, Android SDK 36, Android Build Tools 36.0.0, Gradle 8.13, and Android Gradle Plugin 8.13.2:

```bash
gradle :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Product boundary

The included planning intelligence is deliberately local and deterministic so the APK can be demonstrated without an API key. A production version should put model calls behind a consent-based service, encrypt sensitive data, support calendar integrations, add crisis-safety routing, and make every inferred preference visible and editable.
