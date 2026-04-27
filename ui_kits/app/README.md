# Bubblymarble — Mobile App UI kit

Click-thru prototype of the Bubblymarble Android app. Mirrors the structure of the connected codebase (`feature/*` Compose screens) — visuals only, no real logic.

## Files
- `index.html` — full prototype (onboarding → 6-tab navigation → workout runner)
- `Components.jsx` — primitives (`BMButton`, `BMCard`, `BMStatCard`, `BMChip`, `BMTextField`, `BMNavBar`, `BMScreenTitle`, `BMSectionHeader`)
- `Screens.jsx` — feature screens (`OnboardingScreen`, `PlansScreen`, `WorkoutsScreen`, `RunnerScreen`, `MealsScreen`, `BodyScreen`, `StatsScreen`, `SettingsScreen`)
- `android-frame.jsx` — device bezel + status bar + gesture nav

## Click paths
- Onboarding: name + goal + experience + equipment + sessions/week → **Get started**
- Plans: **Generate plan** (1.1 s spinner) → 2 plans appear → switch to **Workouts**
- Workouts: tap **Start** → Runner screen
- Runner: **Complete set** or **Cancel workout** → back to tab
- Meals / Body / Stats / Settings: static demo data
