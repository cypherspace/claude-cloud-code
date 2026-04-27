# OldFit Design System

> Personal home-fitness app for Android — workouts, meals, body measurements. Material 3, dark-mode-first, sky/teal palette.

---

## About the product

**OldFit** is a personal Android fitness app, built in Kotlin + Jetpack Compose with Material 3. It is **local-first** (Room DB, no cloud) with optional integrations. Three feature waves:

- **v1 Workouts** — exercise library, AI-generated plans (Gemini), workout runner with timer, streaks, stats, Health Connect read/write.
- **v2 Meals** — daily nutrition summary, ingredient search via Open Food Facts, AI photo logging via Gemini Vision, on-device barcode scanner (ML Kit).
- **v3 Measurements** — weight + body composition imported from Health Connect (e.g. Renpho), plus tape measurements (chest, waist, hips, biceps, thighs, calves, neck, shoulders).

### Surfaces in this kit

- **OldFit Mobile App** (Android, Material 3, dark + light) — only product surface in the codebase. The UI kit recreates Plans, Workouts (list + Runner), Meals, Measurements, Stats, Settings, Onboarding.

### Bottom nav (canonical order)

`Plans · Workouts · Meals · Body · Stats · Settings`

Icons (Material Symbols): `list_alt`, `fitness_center`, `restaurant`, `straighten`, `bar_chart`, `settings`.

---

## Sources & inputs

- **Codebase:** `cypherspace/claude-cloud-code` @ branch `claude/android-fitness-app-H3SpM` — imported under `core/` and `feature/` in this project.
- **Theme module:** `core/designsystem/src/main/java/io/bubblymarble/fitness/core/designsystem/theme/Theme.kt` — Material 3 light + dark color schemes.
- **Components module:** `core/designsystem/.../components/Components.kt` — `SectionHeader`, `StatCard`, `KeyValueRow`.
- **Launcher icon:** `app/src/main/res/drawable/ic_launcher_foreground.xml` — a circle with a vertical bar (the "fitness time" mark).
- **Strings:** `app/src/main/res/values/strings.xml` — copy reference.
- No Figma was attached. No marketing site exists yet. No screenshots provided. **Visuals derived from code only.**

---

## Index

- `README.md` — this file
- `colors_and_type.css` — design tokens
- `fonts/` — webfonts (Google Fonts links — no licensed fonts shipped)
- `assets/` — logo mark, favicon, brand SVGs
- `preview/` — small specimen cards for the Design System tab
- `ui_kits/app/` — OldFit Mobile App UI kit (`index.html` + JSX components)
- `core/`, `feature/`, `app/` — imported Kotlin source (read-only reference)
- `SKILL.md` — agent skill manifest

---

## CONTENT FUNDAMENTALS

The voice is **plain, terse, instructive**. No marketing fluff, no "let's get started" cheerleading. The app is a tool — it tells you what you did and asks for what it needs.

### Tone

- **Direct, slightly clinical.** "Sync last 30 days." "Generate plan." "Complete set."
- **Quietly factual when reporting progress.** "Streak updated. Nice work, #${id}." (from the Runner screen on completion). One short compliment, no exclamation point, then onward.
- **Never lectures or motivates.** No "you can do this", no "great job!", no streak-loss-shaming.
- **Second person.** "Tell us a bit about yourself so we can build a plan."
- **Sentences are short.** Often one clause.

### Casing & punctuation

- **Sentence case** for all UI: buttons, headers, nav, dialogs. ("Get started", not "Get Started".)
- **No exclamation points** anywhere.
- **Em-space middle dot (`·`)** is the canonical separator for inline metadata. Used everywhere stats are listed. Examples (verbatim from code):
  - `"3 exercises · AI"`
  - `"560 kcal · P 32g · C 48g · F 18g"`
  - `"82.4 kg · 12 Mar"`
- **Numerals as digits** in UI. Decimal places shown only when meaningful: `%.1f kg` for weight, `%.0f` for kcal.
- **Units are lowercase, no space:** `45g`, `800kcal`, `30s`, `7d`. (Exception: `"%.0f kcal"` does have a space — the codebase is inconsistent here; new code should follow `45g`-style for short units, `800 kcal` for kcal.)

### Vocabulary (verbatim from `strings.xml` and screen titles)

- **Plans** — generated workout programs.
- **Workouts** — today's sessions.
- **Meals** — daily food log.
- **Body** — measurements tab label (full title is "Measurements").
- **Stats** — progress dashboard.
- **Streak** — consecutive-day counter (current + longest).
- **Set / Rep / Weight / Rest** — workout primitives.
- **Sync** — pulling Health Connect data.
- **Log** — adding an entry (a meal, a measurement).

### Empty states (verbatim)

- Plans: `"No plans yet. Generate one to begin."`
- Workouts: `"No workouts yet. Generate a plan first."`
- Meals: `"No meals logged yet."`
- Tape measurement: `"No entries yet"`

### Status / completion (verbatim)

- Onboarding CTA while saving: `"Setting up…"` → `"Get started"`
- Plans CTA while running: `"Generating…"` → `"Generate plan"`
- Sync CTA while running: `"Syncing…"` → `"Sync last 30 days"`
- Workout completion: `"Session complete"` then `"Streak updated. Nice work, #42."`

### Emoji

- **Never used in product chrome.** Zero emoji in the codebase.
- User-generated content (notes, journal) is allowed to contain emoji — those belong to the user.

### Example copy

| Context | ❌ Avoid | ✅ Brand voice |
|---|---|---|
| Plan empty | "Ready to crush a plan? 💪" | "No plans yet. Generate one to begin." |
| Run finish | "AMAZING! You smashed it!" | "Session complete. Streak updated. Nice work, #42." |
| Meal log | "What did you eat today, champ?" | "No meals logged yet." |
| Sync btn | "Magically sync your data ✨" | "Sync last 30 days" |

---

## VISUAL FOUNDATIONS

### Palette

Direct from `core/designsystem/.../Theme.kt`. Material 3 dynamic colour is enabled on Android 12+, so on-device the user's wallpaper can override the brand seed — these tokens are the **fallback** scheme and the source of truth for prototypes / web / marketing.

#### Light scheme (`lightColorScheme`)

| Token | Use | Hex |
|---|---|---|
| `--m3-primary` | Brand / CTAs | `#0EA5E9` (sky-500) |
| `--m3-on-primary` | Text on primary | `#FFFFFF` |
| `--m3-secondary` | Accent / secondary action | `#14B8A6` (teal-500) |
| `--m3-background` | App background | `#F8FAFC` (slate-50) |
| `--m3-surface` | Cards / sheets | `#FFFFFF` |

#### Dark scheme (`darkColorScheme`) — **default at build time**

| Token | Use | Hex |
|---|---|---|
| `--m3-primary` | Brand / CTAs | `#38BDF8` (sky-400) |
| `--m3-on-primary` | Text on primary | `#002B3D` |
| `--m3-secondary` | Accent | `#2DD4BF` (teal-400) |
| `--m3-background` | App background | `#0B1220` (slate-950ish) |
| `--m3-surface` | Cards | `#111827` (slate-900) |

#### Launcher background

`#0EA5E9` (from `app/src/main/res/values/colors.xml`).

#### Feature accents (proposed extension — not yet in code)

The codebase doesn't colour-differentiate feature areas, but the brief calls for it. Reserved hues that sit comfortably alongside sky/teal:

| Feature | Token | Hex |
|---|---|---|
| Plans | `--feat-plans` | `#A78BFA` (violet-400) |
| Workouts | `--feat-workouts` | `#38BDF8` (sky — primary) |
| Meals | `--feat-meals` | `#34D399` (emerald-400) |
| Body | `--feat-body` | `#FBBF24` (amber-400) |
| Stats | `--feat-stats` | `#F472B6` (pink-400) |
| Settings | `--feat-settings` | `#94A3B8` (slate-400) |

**No bluish-purple gradients. No glow.** These accents only appear as small marks (icon tint, sparkline, single-pixel dot) — never as backgrounds.

#### Data viz

The Weight sparkline is hard-coded to `#0EA5E9` with a `4f` stroke (`MeasurementsScreen.kt`). Charts in general should use the **primary** colour for the active series and `--m3-secondary` for a comparison series.

---

### Typography

Material 3 typography defaults — **no custom font** is declared in the codebase, so the system falls back to the OEM default (typically Roboto on Android).

> **⚠️ Font substitution flag.** No font files were shipped. For web prototypes I'm loading **Inter** from Google Fonts as the closest free match to Roboto's metrics. If you want a distinct brand typeface (Inter Tight, Söhne, etc.), drop the woff2s into `fonts/` and update `colors_and_type.css`.

The Compose styles used in screens (mapped to Material 3 type scale defaults):

| Compose style | Approx px | Used for |
|---|---|---|
| `headlineSmall` | 24 / 32 | Screen titles ("Plans", "Stats", "Measurements") |
| `headlineMedium` | 28 / 36 | Big numerics on stat cards |
| `titleLarge` | 22 / 28 | Section heroes ("Rest", "Session complete") |
| `titleMedium` | 16 / 24 | Card titles, nav heading |
| `bodyLarge` | 16 / 24 | Tape-row label |
| `bodyMedium` | 14 / 20 | Body copy, helper text |
| `bodySmall` | 12 / 16 | Metadata / dot-separated stats |
| `labelLarge` | 14 / 20 | Small caps-ish labels (rare) |
| `labelMedium` | 12 / 16 | Stat-card labels ("kcal", "Protein") |

Numerals use the **default font's tabular figures** when available — no separate mono is wired up.

---

### Spacing

Compose dp values found in screens: `4, 8, 12, 16, 20, 24`. Effectively a **4dp base grid**. Standard screen padding is **16dp**, with **12dp gaps** between cards and **8dp gaps** between row chips.

### Backgrounds

- **Solid.** Background is `--m3-background` (`#0B1220` dark / `#F8FAFC` light). No gradients, no images, no patterns.
- No noise overlay, no grain.

### Animation

Material 3 defaults. The only explicit timing in code is `kotlinx.coroutines.delay(1500)` on the Runner's "Session complete" before auto-finishing — i.e. **1.5 s of pause for the user to read the completion message**, then exit.
- Easing: rely on M3 motion (`MotionScheme.standard()` — ~200–250 ms emphasized-decelerate).
- No bounces, no springs, no celebratory animation.
- **Number tween:** the codebase doesn't ramp numbers — they update instantly. New designs may add a 400 ms ease-out tween for hero numerics, but it's not currently brand.

### Hover, press, focus

- **Compose ripple** is the canonical press feedback (default M3 `Indication`). On web, mimic with a `0.06` opacity overlay matching primary.
- **Hover** (web only): surface lightens by ~4% (`#1A2433` from `#111827`).
- **Focus** (a11y): 2 dp ring in `--m3-primary`, 2 dp offset.
- **Disabled:** Material 3 default (~38% alpha).

### Borders

- Cards use **no border** by default — they sit on the surface tone with **1 dp elevation** (which renders as a tonal-elevation tint, not a shadow, on M3 dark).
- **Dividers:** `HorizontalDivider` (default M3 — 1 dp at outline-variant tint).
- **Outlined inputs:** M3 default outline tint.
- **No double borders.**

### Corners

- Cards: M3 `medium` (≈ **12 dp**).
- Buttons: M3 default (`Button` is fully rounded **stadium / pill** — `RoundedCornerShape(50%)` effectively).
- FAB / ExtendedFAB: M3 default (rounded square).
- Text fields (`OutlinedTextField`): **4 dp** corners.
- Avatar/icon containers: full circle.

### Elevation / shadow

Material 3 **tonal elevation**: cards at `1.dp` and `2.dp` (used in StatCard). On dark mode this renders as a **lighter surface tone**, not a drop shadow.
- `elev-1` = `#111827` ⇢ subtle tint up
- `elev-2` = noticeably lighter tint
- No bottom shadow, no glow, no inner shadow.

### Transparency & blur

- Bottom `NavigationBar` uses M3 default (opaque tonal tint). **No backdrop blur.**
- No glass panels, no translucent overlays anywhere in code.

### Cards

- Background: `--m3-surface` (`#111827` dark).
- Padding: **16 dp**.
- Radius: **12 dp**.
- Title in `titleMedium`, supporting text in `bodySmall`.
- **No coloured left border.** (The "rounded corners + accent left-border" cliché is explicitly **not** part of this brand.)

### Imagery

- The product currently uses **no photography**. Every screen is text + icons + numbers.
- If/when imagery is added (marketing, blog, onboarding), guidance: warm-toned, daylight, real homes/kitchens, low saturation, slight grain. No stock-photo gym shots.

### Layout rules

- **Mobile first.** 360–430 dp wide.
- **Single column** with cards stacked at 8–12 dp gap.
- **Bottom NavigationBar**, six tabs.
- **Top of each screen:** title in `headlineSmall`, no app bar / no back chevron unless the route is push-navigated (Runner, Meal Editor, Scanner — these have system back).
- **FAB** used on Meals (`ExtendedFloatingActionButton` "Log meal").

---

## ICONOGRAPHY

The codebase uses **Material Symbols** via `androidx.compose.material.icons.Icons.Default.*`. These are baked into Compose Material — no SVG assets shipped.

Icons referenced in screens:
- Navigation tabs: `ListAlt`, `FitnessCenter`, `Restaurant`, `Straighten`, `BarChart`, `Settings`.
- Meals screen: `ChevronLeft`, `ChevronRight`, `Delete`.

For web prototypes, use **Material Symbols** (Google Fonts CDN) — same family, identical names. Default settings: filled style, weight 400, optical size 24, no fill on inactive states (filled on selected nav tab).

```html
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons" />
<span class="material-icons">fitness_center</span>
```

- **Default size:** 24 dp in nav, 20 dp in inline rows, 16 dp in chips.
- **Stroke / fill:** Material default (filled solids on Android; outlined acceptable on web for selected/unselected pairing).
- **Tint:** inherits `currentColor` (i.e. follows surrounding text colour).

### Custom marks

The launcher foreground (`ic_launcher_foreground.xml`) is a simple **circle outline + vertical bar** — read as a stopwatch / "fitness time" glyph. White on the `#0EA5E9` launcher background. This is the only branded mark in the codebase. A simplified SVG version is shipped in `assets/logo.svg`.

### When emoji?
- **Never** in product chrome.
- Acceptable in user-generated text only.

### Unicode glyphs?
- **Middle dot `·`** is canonical for inline metadata separators (already pervasive in the codebase). Use it.
- Arrows (→, ←) acceptable in body copy where useful.
- Avoid other unicode glyphs as primary icons.

### Custom illustration?
- **None in the codebase.** If added, single-stroke, currentColor, no fills, no gradients, no shadows.

---

## SKILL.md

See `SKILL.md` for the agent-skill manifest used when this folder is loaded as a Claude skill.
