# BigDog Launcher (B2) — Custom Android TV Home Screen

BigDog Launcher (design code **B2**) is a fast, clean, ad-free custom Android TV home screen engineered to replace the stock Xiaomi TV interface. Designed for 10-foot D-pad navigation at razor-sharp 4K (`xxhdpi`/`xxxhdpi`) scaling, it combines a calm dark aesthetic (`#101214`) with signature mint-emerald focus rings (`#8FD6B0`), a dedicated **Movie Box Pro** hero shelf, installed app banners, live TV hardware input pills, a slide-over Quick Settings drawer, and an Accessibility-backed Home button interceptor for Xiaomi firmware.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The decisions below reflect your confirmed preferences from our clarification step and govern both the immediate Milestone 1 build and the iterative milestones that follow.

- **Confirmed Decision 1 — Iterative Delivery Starting with Milestone 1 (Minimal D-pad Skeleton)**: We will build **Milestone 1** first so you can sideload the APK via ADB, verify that BigDog Launcher appears in the TV app list with its custom BigDog neon-mint emblem and 320×180 TV banner, and confirm crisp 4-way D-pad focus movement, 1.06× scale animation, and `OK`/Select key handling across a structured B2 skeleton before layering on subsequent milestones.
- **Confirmed Decision 2 — Xiaomi Home Button Interception (`AccessibilityService`)**: Per your explicit confirmation, the architecture includes a dedicated `AccessibilityService` (`BIND_ACCESSIBILITY_SERVICE`) alongside `CATEGORY_HOME`, `CATEGORY_DEFAULT`, and `CATEGORY_LEANBACK_LAUNCHER` intent filters, plus a companion PowerShell ADB setup script that appends the service cleanly without wiping existing accessibility services. *(Note: `BIND_ACCESSIBILITY_SERVICE` is not eligible for Google Play Creator Program distribution, which matches your sideloaded personal-use APK target.)*
- **Confirmed Decision 3 — Movie Box Pro Hero Tile Target**: Uses the default Movie Box Pro package identifier with an in-app fallback prompt and package selector dialog if the package is not yet installed on the TV.
- **Confirmed Decision 4 — Watch Panel Data Source**: Populates the **Continue**, **Next episode**, and **To Watch** tabs using curated sample thumbnails and an interactive, user-editable **To Watch** list stored locally via DataStore.

---

## 1. Overview & Core Concept

- **What It Does**: Replaces the ad-heavy Xiaomi TV stock launcher with an organised, zero-clutter 10-foot interface. In **Milestone 1**, it delivers the foundational Android TV launcher skeleton: full Leanback/Home manifest declarations, touchscreen-disabled hardware declarations, the custom BigDog Launcher icon and TV banner, the B2 colour and typography system (`Space Grotesk` + `Inter`), and a responsive D-pad focus test grid arranged in the exact B2 spatial zones (Side Rail, Header Bar, Hero & Watch Zone, Apps Row, and Inputs Row) with live focus telemetry so remote navigation can be validated immediately on the TV.
- **Target Audience / Persona**: A single Xiaomi TV owner sideloading a debug APK over wireless ADB from Windows PowerShell who wants instant boot-to-home reliability, zero ads, and one-click access to media apps and hardware inputs.
- **Key Value**: Sub-frame D-pad responsiveness, deterministic focus restoration, unmistakable focus rings (`3dp` `#8FD6B0` border + `1.06x` spring scale), and a disciplined milestone-by-milestone engineering progression.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme (B2 Specification)

- **Aesthetic Direction**: Calm, high-contrast nocturnal cinema UI (`B2`). Surfaces are layered by luminance rather than heavy drop shadows or expensive blurs, keeping frame times locked at 60 fps on modest TV hardware while rendering with crisp vector and `dp` precision on 1080p and 4K displays.
- **Color Palette & Atmospheric Tokens**:
  - **Primary Background (`--background`)**: `#101214` (Deep obsidian charcoal)
  - **Side Rail Surface (`--surface-rail`)**: `#0B0D0F` (Ultra-dark structural rail, `88dp` wide)
  - **Tile Surface (`--surface-tile`)**: `#1A1D21` (Default resting card and pill surface)
  - **Raised / Active Surface (`--surface-raised`)**: `#1F252B` (Focused or selected navigation surface)
  - **Quick Settings Drawer Surface (`--surface-drawer`)**: `#15181B` (`460dp` slide-over panel)
  - **Main Text (`--foreground`)**: `#EEF0F2` (Crisp off-white for primary titles and clock)
  - **Quiet Text (`--muted-foreground`)**: `#9AA3AD` (Secondary metadata, subtitles, and dates)
  - **Primary Accent (`--accent`)**: `#8FD6B0` (Signature BigDog mint-emerald for `3dp` focus rings, active rail indicator bars, connected status dots, and primary play buttons)
  - **Disconnected Dot (`--status-idle`)**: `#4A525C` (Muted slate for inactive HDMI/AV inputs)
- **Typography & Hierarchy**:
  - **Display & Headings (`Space Grotesk`, Medium 500 & Bold 700)**: Used for the `36sp` live digital clock, the `30sp` Movie Box Pro hero title, section headers, and Quick Settings header.
  - **Body & Controls (`Inter`, Regular 400, Medium 500, SemiBold 600)**: Used for `15sp` header dates, `13sp` tile titles, `12sp` uppercase tracking section labels (`APPS`, `INPUTS`), and `11sp` source/status captions.
- **Component Sizing & Spatial Rhythm (Baseline 1280×720 dp grid, scaling cleanly to 1080p/4K)**:
  - **Side Rail**: `88dp` wide; `52×52dp` navigation buttons with `24dp` icons (Home, Movies, All Apps, Inputs at top; Search, Settings at bottom); active item features `#1F252B` background and a `4dp` wide `#8FD6B0` left accent indicator.
  - **Header**: Left-aligned `36sp` clock and `15sp` quiet date; right-aligned `44dp` tall Wi-Fi status pill and `44×44dp` circular utility buttons (Bluetooth, Cast, Quick Settings).
  - **Hero Tile**: `440×280dp`, `20dp` corner radius, rich dark-emerald gradient, `3dp` `#8FD6B0` focus ring when focused, `52dp` round `#8FD6B0` play button.
  - **Watch Panel**: Right of Hero; three `30dp` tall pill tabs (`Continue`, `Next episode`, `To Watch`) above a 3-column × 2-row grid of 16:9 thumbnails (`12dp` corner radius, bottom gradient scrim, `13sp` title, `11sp` subtitle, and `3dp` bottom accent progress bar).
  - **Apps Row**: `12sp` uppercase `APPS` header above seven `144×92dp` cards (`16dp` corner radius) on `#1A1D21`, ending with a dashed-border `All apps` card.
  - **Inputs Row**: `12sp` uppercase `INPUTS` header above six `58dp` tall pills (`HDMI 1`, `HDMI 2`, `HDMI 3`, `AV`, `USB`, `Screen cast`) with `#8FD6B0` connected or `#4A525C` disconnected indicator dots.

### Interactive Feedback & D-Pad Motion

- **Focus Ring & Scale**: Every focusable component uses a unified TV focus modifier that applies a `1.06x` scale animation (`spring(stiffness = Spring.StiffnessMediumLow)`) and draws a crisp `3dp` `#8FD6B0` border around the component's rounded shape (`12dp` to `20dp` radius).
- **Deterministic Initial Focus**: On launch, focus automatically lands on the primary Hero zone (or active Side Rail item) so the user never has to press D-pad arrows multiple times just to see where focus is located.
- **Key Press Feedback**: Pressing D-pad Center (`KEYCODE_DPAD_CENTER` / `KEYCODE_ENTER`) triggers an immediate visual press state and updates the on-screen diagnostic status banner in Milestone 1 (and launches the target intent in subsequent milestones).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Milestone 1 Focus Skeleton Embedded in the Real B2 Spatial Grid**:
  - *Chosen Approach*: Rather than building a throwaway single-button "Hello World" for Milestone 1, we construct the actual B2 spatial skeleton (Side Rail, Top Header, Hero Card slot, Watch Grid slots, Apps Row slots, and Inputs Row slots) with interactive D-pad focus cards, live remote key-code/focus telemetry, and the complete Android TV manifest (`LEANBACK_LAUNCHER`, `HOME`, `DEFAULT`, `android.hardware.touchscreen` not required, `android.software.leanback` required) plus the `AccessibilityService` and PowerShell ADB helper script.
  - *Why*: Lets you immediately test real 2D D-pad traversal across all B2 zones on your Xiaomi TV in Milestone 1 while keeping the codebase 100% aligned with Milestones 2 and 3.
  - *Alternatives Considered*: A bare screen with two plain buttons, which would not test horizontal-to-vertical focus handoffs between the side rail, hero card, 3×2 grid, and bottom rows.
- **Decision 2 — Non-Destructive Accessibility Service & PowerShell Setup Script**:
  - *Chosen Approach*: Include `BigDogHomeAccessibilityService` listening for home/launcher window state changes and hardware key events, paired with a ready-to-run `setup_xiaomi_tv.ps1` script (and an in-app ADB Setup Guide modal) that reads the current `enabled_accessibility_services` setting and appends BigDog Launcher's service rather than overwriting existing services.
  - *Why*: Prevents accidentally disabling other accessibility utilities on the TV while guaranteeing the Home button returns to BigDog Launcher on Xiaomi firmware.
- **Decision 3 — Compose for TV + Standard Compose Foundation**:
  - *Chosen Approach*: Use Jetpack Compose with custom, lightweight TV focus modifiers (`onFocusChanged`, `focusable`, `graphicsLayer` scale, and `border`) alongside `androidx.tv:tv-material` where appropriate.
  - *Why*: Ensures zero focus-ring clipping, exact `1.06x` scale and `3dp` `#8FD6B0` border rendering matching the B2 mockups, and fast frame rates on modest TV GPUs.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        Android TV System / Xiaomi OS                       │
│  ┌──────────────────────────────┐      ┌────────────────────────────────┐  │
│  │ Intent: MAIN + HOME +        │      │ BigDogHomeAccessibilityService │  │
│  │ LEANBACK_LAUNCHER            │      │ (Intercepts Home / Stock pkg   │  │
│  └──────────────┬───────────────┘      │  & brings MainActivity front)  │  │
└─────────────────┼──────────────────────┴───────────────┬────────────────┘  │
                  ▼                                      ▼                   │
┌────────────────────────────────────────────────────────────────────────────┐
│                         MainActivity (SingleTask)                          │
│  • Hardware Key Event Dispatcher (D-Pad / Back / Menu / Home Telemetry)    │
│  • Full-Screen 16:9 TV Canvas (#101214) & B2 Theme (Space Grotesk + Inter) │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     LauncherViewModel & UI State Flow                      │
│  • Active Nav Destination (Home, Movies, All Apps, Inputs, Search, Config) │
│  • Focused Element Telemetry & Last Key Pressed (Milestone 1 Verification) │
│  • Quick Settings Drawer State & ADB Override Status Check                 │
│  • Hero Package Target ("Movie Box Pro") & Manual "To Watch" List State    │
└──────────┬──────────────────────────┬───────────────────────────┬──────────┘
           ▼                          ▼                           ▼
┌─────────────────────┐  ┌─────────────────────────┐  ┌──────────────────────┐
│   B2 Side Rail      │  │   B2 Main Stage (Home)  │  │ Quick Settings &     │
│   (88dp Width)      │  │                         │  │ Setup Overlay        │
│ • Brand Emblem      │  │ • Live Clock & Header   │  │ • 2x2 Status Tiles   │
│ • Home / Movies /   │  │ • Hero Tile (440x280dp) │  │ • 6 TV Settings Rows │
│   Apps / Inputs     │  │ • Watch Panel (3 Tabs + │  │ • Xiaomi ADB & Home  │
│ • Search / Settings │  │   3x2 16:9 Grid)        │  │   Override Status    │
│ • 4dp Accent Bar    │  │ • Apps Row (7x 144x92)  │  │   Inspector Modal    │
│                     │  │ • Inputs Row (6x 58dp)  │  │                      │
└─────────────────────┘  └─────────────────────────┘  └──────────────────────┘
```

### Data Model & State

- **`LauncherUiState`**:
  - `selectedRailItem`: Current active section in the left rail (`HOME`, `MOVIES`, `ALL_APPS`, `INPUTS`, `SEARCH`, `SETTINGS`).
  - `focusedItemId` & `lastKeyEventLabel`: Live telemetry tracked in Milestone 1 so the user can visually confirm on the TV screen which card holds D-pad focus and that `DPAD_UP`, `DPAD_DOWN`, `DPAD_LEFT`, `DPAD_RIGHT`, `DPAD_CENTER`, and `BACK` register cleanly.
  - `isAccessibilityServiceEnabled` & `isDefaultLauncher`: Live runtime checks inspecting `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` and `PackageManager.resolveActivity(HOME)` so the user can see directly on the TV screen whether Step A and Step B have succeeded.
  - `isQuickSettingsOpen`: Controls the `460dp` right-hand Quick Settings slide-over drawer.
  - `watchTab`: Active tab (`CONTINUE`, `NEXT_EPISODE`, `TO_WATCH`) and the list of manual/sample `WatchItem` entries.

### Interactive Component & State Mapping (Milestone 1 Roadmap to Full B2)

1. **Manifest & TV Launcher Identity**:
   - Declares `android.software.leanback` (`required="true"`) and `android.hardware.touchscreen` (`required="false"`).
   - Configures `MainActivity` with `launchMode="singleTask"`, `CATEGORY_HOME`, `CATEGORY_DEFAULT`, `CATEGORY_LEANBACK_LAUNCHER`, and `CATEGORY_LAUNCHER`, plus a custom 320×180 `tv_banner` drawable and custom adaptive launcher icon featuring the BigDog neon-mint emblem on `#101214`.
2. **D-Pad Focus Engine (`TvFocusableSurface`)**:
   - Reusable Compose wrapper applying `onFocusChanged`, `focusable()`, `scale(1.06f)` when focused, `#8FD6B0` `3dp` focus border, `#1F252B` raised background tint, and click handling for `KEYCODE_DPAD_CENTER` / `KEYCODE_ENTER`.
3. **Milestone 1 Interactive Verification Bar & B2 Spatial Skeleton**:
   - Displays all B2 zones in their exact proportional layout so 4-way directional focus traversal between the `88dp` Side Rail, Header utility pills, `440×280dp` Movie Box Pro Hero card, 3×2 Watch Panel tabs/cards, 7-item Apps Row, and 6-item Inputs Row works out of the box on day one.
   - Includes a toggleable **Milestone 1 & ADB Setup Inspector** overlay showing the exact package name, accessibility service component name, live accessibility enabled state, and the PowerShell commands to copy/run from the laptop.
