# Bubblymarble

A personal fitness app for Android.

- v1 — **workouts**: exercise library, AI-generated plans, workout runner with timer, streaks, stats, Health Connect integration.
- v2 — **meals**: daily nutrition summary, ingredient search powered by Open Food Facts, AI photo logging via Gemini Vision.
- v3 — **measurements**: weight + body composition imported from Health Connect (Renpho et al.) plus a tape-measurement tracker for chest, waist, hips, biceps, thighs, calves, neck, shoulders.

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
:core:ai             — Gemini REST client + plan generator + Vision food photo recogniser
:core:foodapi        — Open Food Facts REST client (search + barcode lookup)
:core:notifications  — workout foreground service + reminder WorkManager job
:feature:onboarding
:feature:plans
:feature:workouts
:feature:stats
:feature:settings
:feature:meals
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

## Meals tracker

- **Manual logging**: search the local ingredient cache; on a thin local match,
  the editor falls back to Open Food Facts (`world.openfoodfacts.org`) and caches
  successful hits to the local DB so subsequent searches are instant and offline.
- **Photo logging**: pick a food photo and the editor sends it to Gemini Vision
  via the existing API key. The model returns a list of foods with portion-aware
  nutrition; tap a row to add it to the meal. Suggestions are persisted as
  `source = gemini-vision` ingredients so they can be reused later.
- **Daily summary**: kcal/protein/carbs/fat totals, day-by-day navigation, edit
  or delete previous meals.
- A barcode entry-point (`MealEditorViewModel.lookupBarcode`) is in place; a
  CameraX/ML Kit scanner UI can be wired to it without further plumbing.

## Measurements tracker

- **Sync from Health Connect**: one-tap import of the last 30 days of weight,
  body fat, lean mass, body water, bone mass, height, and BMR. Renpho (and any
  other smart scale that publishes to Health Connect) lands here automatically;
  no vendor SDK required.
- **Manual tape measurements**: chest, waist, hips, biceps (L/R), thighs (L/R),
  calves (L/R), neck, shoulders. Each row shows the latest value + date and a
  one-tap input to log a new entry.
- **Weight trend**: a small in-line sparkline over the most recent entries.
- **Dedup**: imports from Health Connect are idempotent — re-running a sync
  doesn't create duplicates (uniqueness is `(type, timestamp, source)`).

## Roadmap

- Wear OS companion app for in-workout HR + timer.
- Optional cloud sync if desired.
