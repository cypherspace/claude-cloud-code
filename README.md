# Bubblymarble

A personal fitness app for Android. v1 covers the **workouts** pillar:
exercise library, AI-generated plans, workout runner with timer, streaks, stats,
and Health Connect integration. Meals and measurements are scaffolded for later
phases.

## Stack

- Kotlin 2.1, Jetpack Compose (Material 3)
- Hilt for DI, Navigation Compose
- Room (local-only DB) + DataStore + EncryptedSharedPreferences
- Health Connect (`androidx.health.connect:connect-client`)
- Google Gemini API for plan generation (BYO key)
- WorkManager for daily reminders + streak rollover
- Vico for charts

`minSdk = 28`, `targetSdk = 35`, Java 17.

## Module layout

```
:app
:core:common         — dispatchers, Outcome, TimeSource
:core:data           — Room DB, DAOs, repositories, streak engine, secure prefs
:core:designsystem   — theme, reusable Compose components
:core:health         — Health Connect facade (read + write)
:core:ai             — Gemini REST client + plan generator + offline fallback
:core:notifications  — workout foreground service + reminder WorkManager job
:feature:onboarding
:feature:plans
:feature:workouts
:feature:stats
:feature:settings
:feature:meals          (placeholder for phase 2)
:feature:measurements   (placeholder for phase 3)
```

## First-time setup

This is a greenfield project. Before the first build:

1. Open the project in Android Studio (Hedgehog or newer). It will offer to
   generate the Gradle wrapper jar — accept. Alternatively, with Gradle 8.11+
   installed: `gradle wrapper`.
2. Add a `local.properties` at repo root with your Android SDK path. Optionally:

   ```properties
   sdk.dir=/path/to/Android/Sdk
   GEMINI_API_KEY=your-key-here
   ```

   `GEMINI_API_KEY` is baked into `BuildConfig` for development convenience; the
   user-facing flow stores the key in `EncryptedSharedPreferences` after first
   entry in the Settings screen.
3. Install Health Connect on your test device or emulator (it's a separate
   app from the Play Store). The first launch will show the granular permission
   sheet for HR, SpO2, steps, distance, calories, exercise sessions.

## Build

```
./gradlew :app:assembleDebug
./gradlew :core:data:testDebugUnitTest :core:ai:testDebugUnitTest
```

## Verification

- **Unit tests** — `:core:data` covers the streak engine and entity↔domain
  mappers; `:core:ai` covers the plan parser, the fallback generator, and
  unknown-id filtering.
- **Smoke flow on device:** complete onboarding → generate a plan from the
  Plans tab → start a workout → log sets → finish → verify (a) streak
  increments in Stats, (b) an `ExerciseSessionRecord` shows up in the Health
  Connect app.

## Exercise library

The bundled `app/src/main/assets/exercises/exercises.json` is a small starter
set covering the major movement patterns. To swap in the full
[free-exercise-db](https://github.com/yuhonas/free-exercise-db) (MIT-licensed,
~800 exercises with images), drop its `exercises.json` into the same path and
adapt the `SeedExercise` schema. The seeder is idempotent and reads on first
launch only.

## Roadmap

- Phase 2 — Meals: Open Food Facts integration, barcode scan, AI photo logging via Gemini Vision.
- Phase 3 — Measurements: Health Connect read for weight + body composition (Renpho already publishes there), tape-measure manual entries.
- Future — Wear OS companion, multi-device cloud sync if desired.
