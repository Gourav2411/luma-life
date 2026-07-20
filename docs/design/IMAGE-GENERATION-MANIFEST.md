# Luma visual concept manifest

Generation mode: built-in image generation.

Taxonomy: `ui-mockup`.

## Shared visual direction

9:16 Android product design reference for Luma, an adaptive whole-life planner for Indian college students. Energetic Gen-Z editorial identity with a warm paper and dark-ink base; electric lime, cobalt, coral, aqua and violet accents; bold rounded geometry; Manrope-like typography; expressive hand-drawn Indian college-life details; high contrast and clear hierarchy. Avoid corporate dashboards, excessive gradients, glassmorphism, childish gamification, streak pressure and productivity-shaming language. Native implementation target is an 8dp Compose grid with Material accessibility.

## Generated gate-one concepts

- `luma-style-board.png` — color, typography, cards, chips, icons, navigation and interaction language
- `luma-today-anchor.png` — energy check-in, top outcomes, balanced capacity and whole-life timeline
- `luma-ask-anchor.png` — live voice waveform, transcript/context interpretation and proposal entry
- `luma-plan-review-anchor.png` — feasible status, selectable changes, before/after diff, explanations and approval control

These are visual review references. Native screen layouts and text are implemented in Jetpack Compose rather than embedded as bitmap UI.

## Bright premium v2

The second gate responds to the request for CRED-level confidence in a brighter theme without copying CRED branding, layouts, assets or trade dress.

- `luma-bright-premium-style-board-v2.png` — oversized editorial typography, luminous ivory canvas, acid hero surfaces, solid life-area color, crisp offset shadows, tactile controls, voice orb and motion guidance
- `luma-bright-today-anchor-v2.png` — “MAKE TODAY FIT.” hierarchy, energy selector, plan hero, balance strip, timeline, floating voice action and custom bright navigation

Implementation principles taken into Compose:

- Verb-led headlines instead of generic page titles
- Hard offset depth instead of soft corporate elevation
- Near-black actions with acid response states
- Strong solid color used to encode meaning, not decoration
- A clear tap → respond → confirm interaction rhythm
- Equal visual dignity for study, skills, health, sport, friends, dates and fun
