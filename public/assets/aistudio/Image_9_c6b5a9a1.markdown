---
name: Agro-Field Clarity
colors:
  surface: '#f8faf3'
  surface-dim: '#d9dbd4'
  surface-bright: '#f8faf3'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4ed'
  surface-container: '#edefe8'
  surface-container-high: '#e7e9e2'
  surface-container-highest: '#e1e3dc'
  on-surface: '#191c18'
  on-surface-variant: '#41493f'
  inverse-surface: '#2e312d'
  inverse-on-surface: '#eff2eb'
  outline: '#71796e'
  outline-variant: '#c0c9bc'
  surface-tint: '#2e6b34'
  primary: '#12521e'
  on-primary: '#ffffff'
  primary-container: '#2e6b34'
  on-primary-container: '#a7e9a5'
  inverse-primary: '#95d693'
  secondary: '#156589'
  on-secondary: '#ffffff'
  secondary-container: '#92d4fd'
  on-secondary-container: '#005d80'
  tertiary: '#5d421c'
  on-tertiary: '#ffffff'
  tertiary-container: '#775931'
  on-tertiary-container: '#fbd2a1'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b0f3ad'
  primary-fixed-dim: '#95d693'
  on-primary-fixed: '#002106'
  on-primary-fixed-variant: '#12521e'
  secondary-fixed: '#c5e7ff'
  secondary-fixed-dim: '#8dcef7'
  on-secondary-fixed: '#001e2d'
  on-secondary-fixed-variant: '#004c6a'
  tertiary-fixed: '#ffddb5'
  tertiary-fixed-dim: '#e7c090'
  on-tertiary-fixed: '#2a1800'
  on-tertiary-fixed-variant: '#5c421c'
  background: '#f8faf3'
  on-background: '#191c18'
  surface-variant: '#e1e3dc'
typography:
  display-lg:
    fontFamily: Barlow Condensed
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.5px
  display-sm:
    fontFamily: Barlow Condensed
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: 0px
  headline-lg:
    fontFamily: Barlow Condensed
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: 0.25px
  headline-md:
    fontFamily: Barlow Condensed
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: 0.25px
  headline-sm:
    fontFamily: Barlow Condensed
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-lg:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-md:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 16px
    fontWeight: '700'
    lineHeight: 22px
    letterSpacing: 0.15px
  body-lg:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 15px
    fontWeight: '700'
    lineHeight: 20px
    letterSpacing: 0.5px
  label-md:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Atkinson Hyperlegible Next
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  gutter-lg: 1.5rem
  margin: 1rem
  margin-sm: 0.75rem
  margin-lg: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system delivers a rugged, utility-first interface engineered specifically for ranchers, foremen, and field workers operating in open-air pastures under blinding sunlight. The brand aesthetic merges raw utilitarian fieldwork with modern agricultural intelligence. Rather than relying on fragile, low-contrast desktop paradigms, the UI projects resilience, immediate legibility, and high-trust clarity.

### Aesthetics & Tone
- **Field-Ready Utility:** Form follows immediate field comprehension. Structural elements use sturdy silhouettes, prominent states, and unmistakable visual hierarchy.
- **Sunlight Resilience:** High-contrast surfaces resist glare and washout. Subtle gray borders and washed-out pastels are eliminated in favor of crisp boundaries and deep ink tones.
- **Calm Authority:** Organic greens, earthen ochres, and sky blues root the software in the working landscape without distracting from critical livestock alerts.
- **Ergonomic Safety:** Designed for single-handed thumb use, gloved interactions, and moving utility vehicles (pickups, ATVs, horseback).

## Colors

The palette is tuned specifically for optical contrast under outdoor ambient lux (>50,000 lux). Pure pastels and delicate gradients are banned. Every state color satisfies minimum WCAG AA contrast (≥ 4.5:1) against both the `#FFFFFF` card surface and `#F3F5EE` canvas background.

### Functional Roles
- **Primary (`#2E6B34` - Verde Pasto):** Primary action buttons, prominent toggles, core brand emphasis.
- **Primary Container / Selected (`#CDE8C8` - Verde Brote):** Active chip fills, selected navigation highlights, contextual cards.
- **Primary Text & Active Icons (`#17361C` - Verde Monte):** Deep forest tint used for high-emphasis headlines and active icon glyphs on light containers.
- **Secondary / Info (`#1F6B8F` - Azul Río):** Geofence boundaries, water source markers, secondary action triggers, and informational callouts.
- **Tertiary / Earth (`#7A5C34` - Tierra):** Herd attributes, pasture division tags, animal breed data, and neutral earthen grounding.
- **Canvas Base (`#F3F5EE` - Hueso):** Anti-glare tinted background reducing ocular fatigue under full sunlight compared to raw white.
- **Surfaces (`#FFFFFF` - Blanco):** Elevated card containers, modal sheets, and structured data blocks.
- **Text Main (`#1A2118` - Tinta):** High-density dark green-black for primary text, values, and counters.
- **Text Secondary (`#4A5546` - Piedra):** Deep muted moss-slate for structural labels, timestamps, and aux metadata.
- **Structural Border (`#B9C2AC`):** 1.5dp structural line boundary across inputs, cards, and segmented elements.

### Animal State Semantics
Each animal status combines an authoritative chromatic cue with an invariant symbol to safeguard accessibility across color-blind operators:
- **Dentro de Zona (`#2E7D32`):** Normal status; paired with `check_circle`.
- **Fuera de Zona (`#C62828`):** Critical alert; geofence breach; paired with `warning`.
- **Batería Baja (`#F9A825`):** Hardware warning; paired with `battery_alert`. Text paired with dark ink `#1A2118` for contrast.
- **Quieto / Inactivo (`#7B4FA3`):** Prolonged inactivity / health check; paired with `hourglass_top`.
- **Sin Señal (`#6B7280`):** Collar telemetry missing; paired with `signal_disconnected`.

## Typography

The typography pairings are chosen for extreme legibility under daylight wash and vibration:
- **Barlow Condensed:** Used exclusively for numeric counters, large livestock counts, herd identifiers, and structural section headers. Its condensed proportions allow 5-figure numbers and ear-tag IDs to stay large without wrapping horizontally on phone displays.
- **Atkinson Hyperlegible Next:** Used for all prose, data labels, button triggers, and status descriptions. Its distinctive glyph distinctions (slashed zeroes, differentiated capital `I`, lowercase `l`, and digit `1`) eliminate reading errors during data collection and emergency herd checks.

### Rules
- Never use font weights lighter than `400` anywhere in the app.
- Key telemetry (temperatures, battery percentage, cattle headcounts) must use `display-sm` or `headline-lg` with `Barlow Condensed` bold.
- Button labels always resolve to uppercase or title-case `label-lg` with open letter-spacing.

## Layout & Spacing

Field operations require intentional spacing to protect against touch errors caused by rough hands, gloves, or vehicle movement.

### Touch Targets & Thumb Zone
- **Primary Interactive Target Rule:** All tap targets must be a minimum of `48dp × 48dp`. Primary action triggers enforce a minimum height of `52dp`.
- **Thumb Zone Layout:** Critical operational triggers (collar pairing, geofence panic, status filtering, list view swaps) must anchor within the bottom 40% of the viewport. Header zones are reserved for passive counts and orientation.

### Grid & Breakpoints
- **Compact (Mobile: < 600dp):** Single column fluid container, `16dp` horizontal outer margin, `16dp` gutter. Map controls anchor right-bottom at least `24dp` off screen borders.
- **Medium (Tablet / In-Cab Display: 600dp - 840dp):** 8-column layout, `24dp` margins and gutters. Master-detail views split 4:4 (e.g., pasture map on the left, livestock inspection cards on the right).
- **Expanded (Rugged Toughbooks: > 840dp):** 12-column layout, `32dp` gutters, max content width restricted to `1200dp` centered.

## Elevation & Depth

Subtle ambient drop-shadows fail completely under intense outdoor illumination. Elevation in this system is therefore achieved via **high-contrast surface bounding borders and tonal shifts** rather than blur radii.

### Surface Tiers
- **Canvas Base (`#F3F5EE`):** Ground level. No border.
- **Surface Level 1 (Cards, List Items - `#FFFFFF`):** 1.5dp solid border (`#B9C2AC`). No shadow in bright sunlight mode.
- **Surface Level 2 (Selected / Active Items - `#CDE8C8`):** 2dp solid border (`#2E6B34`).
- **Surface Level 3 (Bottom Sheets & Dialogs - `#FFFFFF`):** 28dp top corner radius, topped with an opaque scrim (`#1A2118` with 60% opacity) and an elevated high-contrast boundary line (`#B9C2AC`).
- **Hardware Floating Controls (FABs, Map Zoom Tools):** Crisp, hard-edge tactile contour with a subtle 2dp offset shadow (`rgba(23, 54, 28, 0.20)`), ensuring immediate visibility over satellite imagery or topo maps.

## Shapes

The geometric vocabulary balances modern Android Material 3 primitives with solid physical hardware metaphors. 

- **Cards & Data Modules:** `12dp` rounded corners (`rounded-lg` level).
- **Buttons & Action Bars:** `12dp` rounded corners, matching the container profile for visual alignment.
- **Chips & Filter Badges:** Fully pill-shaped (`20dp` radius) to separate them immediately from structural card blocks.
- **Bottom Sheets & Floating Drawers:** `28dp` top-left and top-right radii for smooth, natural gestures.
- **Icon Enclosures:** Hexagonal or squircle geometry (`8dp` radius) for status glyph backgrounds.

## Components

### Buttons
- **Primary Field Button:** Min-height `52dp`. Background `#2E6B34`, text `#FFFFFF`, border none, corner radius `12dp`. Bold uppercase label `15px` Atkinson Hyperlegible.
- **Secondary Outlined Button:** Min-height `52dp`. Background `#FFFFFF`, text `#17361C`, 2dp solid border `#2E6B34`.
- **Destructive / Panic Button:** Background `#C62828`, text `#FFFFFF`, icon `warning`, min-height `52dp`.

### Status Chips & Filters
- **Default Chip:** Height `40dp`. Background `#FFFFFF`, text `#4A5546`, 1.5dp border `#B9C2AC`, corner radius `20dp`.
- **Active / Selected Chip:** Height `40dp`. Background `#CDE8C8`, text `#17361C`, 2dp border `#2E6B34`, leading check icon `18dp`.
- **Status Badge (Non-interactive):** Height `32dp`. Opaque background derived from animal state (e.g., `#F9A825` for low battery), paired with high-contrast text and a left-aligned state icon.

### Livestock Cards (Card Components)
- Structured on `#FFFFFF` base, `12dp` border radius, `1.5dp` solid `#B9C2AC` outline, `16dp` inner padding.
- **Header:** Prominent ear-tag number in `Barlow Condensed Bold 24px` (`#1A2118`), paired with the right-aligned status badge.
- **Body Grid:** Two-column telemetry matrix (battery, last movement, temperature) displayed with uppercase micro-labels (`#4A5546`) and prominent value readouts.
- **Active Tap State:** Transitions border to `#2E6B34` (2dp) and background to subtle `#F3F5EE`.

### Inputs & Search
- Min-height `56dp`. Fill `#FFFFFF`, border `1.5dp` `#B9C2AC`, corner radius `12dp`.
- In-focus state expands border to `2dp` `#2E6B34`. Label text `#1A2118`. Trailing quick-clear button target `48dp × 48dp`.

### Bottom Navigation & App Bars
- **Top App Bar:** Height `64dp`. Background `#F3F5EE`. Primary title in `Barlow Condensed` `24px` bold with `#17361C`. Action icon targets minimum `48dp × 48dp`.
- **Bottom Navigation Bar:** Height `80dp`. Background `#FFFFFF` with a top `1.5dp` border `#B9C2AC`. Navigation items feature a pill highlight `#CDE8C8` for the active icon with `#17361C` icon/text fill.