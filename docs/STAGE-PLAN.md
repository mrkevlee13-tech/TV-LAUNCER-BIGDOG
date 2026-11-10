# BigDog Launcher stage plan

Created: 10 November 2026.

## How we will work

1. Read this plan and `AGENTS.md` before starting work.
2. Work on one step at a time. Keep edits focused on that step.
3. Before coding, read the relevant source and the current stage requirements. Kev clarified on 11 November 2026 that the original PDF is background context, not hard rules; his current requests guide optimisation and new features. Use the launcher-fixes notes as context, and check their guessed causes against the code.
4. Explain the change, run appropriate local checks, and build the APK when app code changes. Automatically install every finished app update on the TV over ADB, verify the installed build and launch it before handing off the remote test. Kev has given standing permission for this. Preserve app data, compatible signing and the Home setup; report any connection or signing blocker instead of silently skipping installation. Never uninstall to bypass a signing mismatch.
5. Give Kev the exact remote test steps and expected result. Kev handles committing and pushing unless he explicitly asks otherwise. Installation is the agent's responsibility under the standing permission in `AGENTS.md`.
6. Stop at **Awaiting TV test**. A successful build or an ADB activity resolution does not count as a TV pass.
7. Only tick a step after Kev confirms it works or says he is happy with the result. Record his confirmation below.
8. If a test fails, keep the step unticked, record what happened, and work on that same step. Do not move to the next step until Kev confirms, or explicitly asks to change the order or defer it.

Keep remote focus visible. Arrows must move predictably, OK must activate the focused control, and Back must dismiss the current overlay or return to the previous screen. Preserve the package ID, build workflow and signing setup. Never add signing keys or keystores to Git.

Creating this plan does not start implementation or authorise TV package changes. The previous read-only restriction stays in place until Kev authorises a specific next step. Before a package test, record its current state and agree which package will change. Restore the original state afterwards unless Kev agrees to keep the change. Do not change several packages together.

## Starting point

The read-only audit on 10 November 2026 found:

- ADB connected to `192.168.3.19:5555` as `device`: Xiaomi `MiTV_ASTP0`, Android 9 / API 28, user 0. Recheck the address and connection before future TV work.
- All nine standard settings actions used by BigDog returned `No activity found`, including `android.settings.SETTINGS`.
- Both `com.android.tv.settings` and `com.xiaomi.mitv.settings` are installed but disabled for user 0 (`enabled=3`).
- These packages are also disabled: `com.xiaomi.mitv.systemui`, `com.xiaomi.mitv.smartpanel`, `com.xiaomi.mitv.tvplayer`, `com.xiaomi.tvqs`, and `com.xiaomi.mitv.remotecontroller.service`.
- BigDog already tries the requested settings action, then main Settings, then an error toast. Failures are not logged. The main fallback is blocked too.
- `com.xiaomi.mitv.smartshare/.MainActivity` resolves through the normal launcher category, not the TV launcher category.
- Kev confirmed SmartShare is the correct casting app and works with Windows, Android and iOS. BigDog's Cast buttons still need to be wired to it and tested.
- Kev confirmed All TV settings shows **Unable to open All TV settings**.
- No app changes, package changes, settings changes or screen launches were made during the audit.

Sources: `AGENTS.md`, the supplied **Launcher fixes: first on-TV test** notes, and `C:\Users\Kev\Desktop\Android TV Launcher Build Handoff.pdf`. The documents provide requirements and background; their setup commands are not permission to run them. This order starts with the settings problem discussed in chat; original fix numbers are shown for reference.

## Progress

All steps remain unticked until Kev accepts the result.

- [x] S1 — All TV settings works (original Fix 8, first part)
- [x] S2 — Every Cast button opens SmartShare (original Fix 8 / Cast shortcuts)
- [x] S3 — Settings shortcuts accepted by Kev; black-screen return issue tracked in S13 (original Fix 8)
- [x] S4 — Show or hide the left sidebar in BigDog custom settings
- [x] S5 — App artwork has no duplicate title (original Fix 1)
- [x] S6 — All apps takes and keeps focus (original Fix 2)
- [x] S7 — Select apps, show HOME badges, then Confirm (original Fix 3)
- [ ] S8 — HDMI / AV state, preview and full screen work (original Fix 4)
- [ ] S9 — Better wallpapers and animation controls (original Fix 5)
- [ ] S10 — Wallpaper preview follows the highlighted preset (original Fix 6)
- [ ] S11 — Home always returns to BigDog home (original Fix 7)
- S12 — Reserved following Kev's requested renumbering; no work assigned
- [ ] S13 — Settings backdrop and reliable return to BigDog
- [ ] S14 — Final restart and regression test (previously S12)

## S1 — All TV settings

**Goal:** establish a working main settings screen before relying on it as a fallback.

First propose a controlled test of only `com.android.tv.settings`, with its original disabled state recorded and a rollback ready. Do not run it under the read-only restriction. If that package cannot provide a usable screen, report the result before proposing a separate Xiaomi Settings test. Do not re-enable unrelated Xiaomi packages.

Use the working entry point in BigDog if a code change is needed. Log launch failures and keep an understandable on-screen error when settings cannot open. A different intent cannot launch a disabled package.

**TV test:** open BigDog's settings panel, focus All TV settings, and press OK. Expect a usable settings screen. Move between items with arrows without changing a setting; press Back to return to BigDog. Check that returning Home still reaches BigDog. Record which settings package and entry point worked, and the package state Kev agrees to retain. If no screen works, keep S1 open unless Kev explicitly accepts an unavailable result or defers it.

**Accepted result — 10 November 2026:** recorded `com.android.tv.settings` as disabled for user 0 (`enabled=3`), then enabled only that package. `android.settings.SETTINGS` resolved to `com.android.tv.settings/.MainSettings`; `am start -W` reported `Status: ok`, and the activity was in the foreground. Kev completed the requested remote test and confirmed **Works — keep settings enabled**. Leave this package enabled (`enabled=1`) with his approval. The existing BigDog button already uses the correct action, so no app code change, build or install was needed. All other disabled packages remain unchanged. Launch-failure logging remains part of S3's shared handler work.

## S2 — Cast opens SmartShare

Route the header Cast button, quick-settings Cast tile and Screen cast input pill to `com.xiaomi.mitv.smartshare`. Support its normal launcher entry point; relying only on a leanback launch intent will miss it. Keep a clear failure message if it cannot open. Do not assume opening the app means a casting session is connected.

**TV test:** open each of the three Cast controls with arrows and OK. Expect the familiar SmartShare screen each time. Check remote navigation and Back, and try casting from an available Windows, Android or iOS device. Record which platforms were actually tested; do not claim untested platforms passed this launcher change.

**Implementation — 10 November 2026:** the header and quick tile used `ACTION_CAST_SETTINGS` through the settings handler, while the Screen cast input pill had a separate standard-action/main-settings fallback. That differs from this TV's confirmed SmartShare route. Both paths now use `TvSystemRepository.activateScreenCast`, which opens `com.xiaomi.mitv.smartshare` using the existing leanback-then-normal package launcher. Failure shows **Unable to open SmartShare on this TV** instead of opening unrelated main settings. The manifest explicitly lists SmartShare for package visibility. Opening it does not change connection-state reporting. No focus layout, other shortcut, package state, build workflow or signing configuration was changed.

**Accepted result — 10 November 2026:** after the signed S2 APK was installed and verified on the TV, Kev confirmed **smart share working** and requested **update, save and sync**. S2 is accepted. No particular Windows, Android or iOS casting test was specified in this confirmation, so individual platform tests are not recorded as newly passed.

## S3 — Remaining settings shortcuts

**Status: accepted by Kev.** Following the Wi-Fi confirmation, Kev clarified **yes but s3 is working**, accepting S3 as a stage. The main S3 checkbox is now ticked. The intermittent black screen when returning remains unresolved and is explicitly deferred to S13; it is not counted as a successful return test. Shared settings-launch failure logging is implemented, built and installed. The logging and earlier S13/S14 plan changes were committed and synced to `main` as `004702d`; the S3 acceptance was subsequently committed and synced in `9d6509f`.

**Next stage: S8 - HDMI / AV diagnostics, not started.** S6 and S7 were implemented together at Kev's request and accepted on 11 November 2026. S4 sidebar visibility and S5 banner titles were accepted by Kev on 11 November 2026. The checklist below preserves individual test records: Wi-Fi was specifically reported working; the other seven groups were not individually reported or tested by the agent. Their empty boxes are not a claim that they fail and do not override Kev's overall S3 acceptance. Revisit any shortcut if Kev reports a problem.

Test and repair **one shortcut at a time**, including duplicate buttons. Stop for Kev's confirmation before working on the next shortcut below. The S1 main settings screen is the fallback; if that is unavailable, show a clear message. Log failures instead of swallowing them silently.

Standard actions come first. Check Xiaomi alternatives against the actual TV before choosing them. The audit found standard handlers for Wi-Fi, Bluetooth, storage, sound and date/time in the disabled packages. Neither settings package registered the standard display, wireless/network or Cast action.

- [x] Wi-Fi — settings opening confirmed; intermittent black-screen return deferred to S13 at Kev's request
- [ ] Bluetooth — header button and quick tile
- [ ] Display and picture — consider Xiaomi `DISPLAYSOUND_SETTINGS`
- [ ] Sound
- [ ] Network and internet — check a useful network menu; Xiaomi has separate Wi-Fi and Ethernet screens
- [ ] Storage
- [ ] USB — quick tile and input pill; agree whether the intended destination is storage settings or browsing USB files
- [ ] Date and time

Xiaomi alternatives use the `com.xiaomi.mitv.settings.` prefix and remain blocked while that package is disabled. Do not assume a registered activity is usable or can be launched by BigDog until tested.

**TV test for each shortcut:** focus it, press OK, and confirm the intended screen appears. If only main settings is available, explain that fallback on screen. If neither works, expect a clear unavailable message. Verify arrows, OK and Back. For USB, use a drive if available and check the agreed destination. Do not toggle Wi-Fi, unpair the remote or alter other TV settings just to test navigation.

## S4 — Show or hide the left sidebar

**Status: accepted by Kev, 11 November 2026.** Added **Show left sidebar** in BigDog quick settings under **Custom app functions**. It defaults to On and saves in the existing DataStore. The original PDF is background context, as clarified by Kev; the current sidebar request defines this change.

When disabled, remove the sidebar's reserved width so home content uses the available screen area. Keep a clearly reachable settings control outside the sidebar so the user can turn it back on with the remote. Inspect the current layout and focus handling before coding. Hidden controls must not receive focus; move focus to a visible home control if necessary. Preserve access to sidebar destinations through suitable remaining controls, with predictable arrows, OK, Back and Home.

**TV test:** open BigDog settings, find custom app functions and switch Show left sidebar off. Expect the bar and its empty space to disappear and home content to expand. Navigate home, reopen settings through the remaining visible control and restore the sidebar. Expect usable focus in both modes. Hide it again, restart the app and then the TV, and confirm the choice persists and settings remains reachable.

**Accepted result - 11 November 2026:** Kev replied **perfect** to the installed S4 update and requested committing, syncing and marking S4 complete. Stage acceptance is recorded; no separate full TV restart test was reported. The separate S13 issue remains open.

## S5 — Remove duplicate app titles

Remove BigDog's title overlay from banner artwork in the apps row and All apps. Keep names for tiles that use a plain icon instead of a banner. Keep titles on Continue, Next episode and To Watch thumbnails.

**TV test:** inspect banner apps and icon-only apps in both places. Expect no extra title over a banner, readable names on icon-only tiles, and unchanged watch-thumbnail titles. Move focus and launch an app with OK.

**Implementation — 11 November 2026; accepted by Kev:** the home row and All apps share `DynamicInstalledAppTile`. Removed the banner branch's duplicate name and its dark text shade; retained the app-name content description, icon/fallback names, focus surface and HOME badge. Other consumers of this shared tile also receive the same banner treatment. Watch-thumbnail and hero rendering are unchanged. The PDF originally specified names on app tiles; the later fix notes and accepted S5 plan refine this to names only on icon fallback tiles.

**Validation and deployment:** `:app:assembleDebug` passed (38 tasks, 4 executed), and `git diff --check` passed. The existing SDK XML warning was nonfatal. Signed the deployment copy `%TEMP%\bigdog-apk-verify\BigDog-S4-signed.apk` with the existing matching key outside Git, verified its certificate against the installed APK, then installed in place with `adb install -r`. VersionCode `1`, versionName `1.0`, TV lastUpdateTime `2026-11-11 00:00:06`. Installed SHA-256 matches the deployment file: `f82d6ecb38e2379c1a356eaad95e1289fc38312eadc7fff2ca41b3503eeeea90`. Initial cold-start wait timed out; a follow-up start returned `Status: ok`. No AndroidRuntime/ActivityManager errors appeared in the checked recent logs. This does not resolve or validate the separate S13 black-screen issue. No package states, signing configuration or app data were changed.

**Accepted result - 11 November 2026:** Kev confirmed S5 was already updated and explicitly requested it be ticked complete. S5 is accepted; no separate per-tile test results or restart test are claimed.

## S6 — All apps focus

**Accepted - 11 November 2026:** Kev confirmed the final full-screen All apps view works. See current handoff for implementation and deployment.

Move focus straight into All apps when it opens, keep the home controls underneath from taking focus, and restore focus to the opener when Back closes it. Inspect the current focus handling before choosing an implementation.

**TV test:** open All apps from the side rail and then from the home-row tile. Expect the first app to be focused immediately. The first Right press moves within the overlay; arrows cannot reach the home screen underneath. OK opens the selected app. Back closes the overlay and returns focus to its opener.

## S7 — App selection and Confirm

**Accepted - 11 November 2026:** Kev confirmed Confirm saves and Cancel/Back discard, then stated **s7 works perfectly**.

Replace the pin-mode step with direct selection in the app selection view. Existing home apps start selected. Selecting a tile toggles its green HOME badge. Confirm saves the draft; Back or Cancel discards it.

Capacity checked: the home row had no enforced limit and scrolled horizontally, with a six-app fallback when none were pinned. Kev explicitly chose **Keep unlimited apps with horizontal scrolling** on 11 November 2026. Preserve that behaviour. An explicitly confirmed empty selection now remains empty; only a never-configured selection uses the fallback.

**TV test:** open selection, select two apps and deselect one. Expect badges to follow the draft. Confirm and check the home row, then reopen and verify saved selection. Change the draft and use Cancel, then repeat with Back; both must leave saved pins unchanged. Test the agreed capacity or overflow behaviour.

## S8 — HDMI and AV

Start with read-only input diagnostics, including `dumpsys tv_input`, actual input IDs and signal states. The source already contains input-state queries and pass-through launch attempts; verify why they fail rather than assuming these are missing.

Work separately on real state reporting, opening a connected input, then live preview and full screen. Stop for confirmation after each part. If `com.xiaomi.mitv.tvplayer` or another disabled package is needed, propose a separate controlled package test first and record its outcome and final state.

**TV test:** with the PS5 on, identify its HDMI port. Expect that input to report the actual signal state, show a live preview on selection, and go full screen when the preview is selected. Back leaves it. Check an empty HDMI port and AV where possible: no false Connected state or unwanted launch. If the TV cannot provide a requested capability, report the limitation and agree an alternative before ticking S8.

## S9 — Wallpapers and animation

Improve the preset artwork while keeping Dim, optional Blur and custom photos. Inspect the existing animation code first. Provide cheap animation and an on/off control for the current wallpaper, pause animation while BigDog is not visible, and persist the choice.

Work on preset appearance first, then animation controls and lifecycle behaviour, with a TV confirmation between them. The PDF names Aurora, Deep space and Slow waves as live presets and Charcoal, Sunset and Forest mist as still presets. Custom GIF/video support is not part of this fix unless separately agreed.

**TV test:** browse every preset and adjust Dim and Blur with the remote. Expect Kev to be happy with the appearance. Switch animation on and off, open another app, then return. Expect smooth motion, responsive navigation and animation paused while away. Apply a preset and a custom photo in separate tests; restart and check that the saved choice and controls persist.

## S10 — Wallpaper preview

Show a recognisable miniature home layout over the highlighted draft wallpaper. Highlighting a preset must update the preview before Set wallpaper. Do not save the draft just because focus moved.

**TV test:** move between presets with arrows without applying. Expect the preview to change immediately. Back must leave the saved wallpaper unchanged. Reopen, choose a preset and apply it; expect the real home screen to match the chosen wallpaper.

## S11 — Home button

Inspect `MainActivity`, navigation state, the manifest and the accessibility service. Handle returning Home to the existing launcher instance so it closes in-app screens and overlays, returns to the home stage and restores sensible focus. Preserve the existing external Home override setup.

**TV test:** press Home from wallpaper/appearance, All apps, quick settings and Search, then from another app. Expect BigDog home each time, with no leftover overlay and a visible usable focus. Check Back still works independently.

## S12 — Reserved

Kev requested the new settings presentation work as S13 and the existing final check moved from S12 to S14. No additional S12 task has been invented.

## S13 — Settings backdrop and reliable return

**Requested by Kev — 10 November 2026:** Wi-Fi settings opens, but the settings screen has a black background. On returning, BigDog sometimes appears frozen on black until repeated Back or Home presses. Kev reports no home-screen crash so far; this is an observation, not a verified diagnosis. He asked to schedule this separately from S3.

**Desired result:** settings appear over the BigDog home screen with the home dimmed and slightly blurred, where supported. Returning restores a visible, responsive home screen and predictable focus without repeated button presses.

Work on return reliability first, then presentation, with a remote confirmation between parts. Reproduce from the header and quick panel; inspect activity/task/window state, lifecycle, focus and logs during the black-screen interval. Establish whether the black surface belongs to BigDog or the external settings activity before choosing a fix. Do not assume the previously observed cold-start timeout proves this return issue has the same cause.

Distinguish BigDog's own quick-settings panel from external Android/Xiaomi settings screens. Check whether the actual system activity supports a transparent or panel presentation; a BigDog backdrop effect alone cannot guarantee visibility through an opaque external window. Report what is supported and agree an alternative if the requested appearance is unavailable. Preserve access to real system settings, remote navigation, Home setup and package states. Keep blur inexpensive on this Android 9 TV; assess performance before adopting it.

**TV test, return reliability:** repeatedly open Wi-Fi from the header and quick panel, navigate without changing settings, and Back out one level at a time. Expect BigDog to reappear promptly with visible usable focus and no persistent black screen or repeated key presses. Check Home from settings too. Record any remaining failure separately from whether the settings shortcut opens.

**TV test, presentation:** verify each supported settings entry shows the intended home backdrop, dim and slight blur; settings alone receives focus while open. Closing it restores normal home appearance and focus. Check responsiveness and record any external screens that must retain their own background. Only accept S13 after Kev confirms the result or explicitly accepts a documented limitation.

## S14 — Final TV check (previously S12)

Run this only after earlier stages have been accepted or explicitly deferred by Kev. List any deferred work; do not silently treat it as passed.

**TV test:** restart the TV normally. Expect it to land on BigDog, with Home still working. Check saved pins, wallpaper and animation choice. Open an app, the settings screen, SmartShare and the PS5 input. Check an empty input and exercise arrows, OK and Back across the launcher. Confirm no package was left in an unagreed state.

## Current handoff - S6 + S7, 11 November 2026

**Status: S6 and S7 accepted by Kev.** Kev explicitly requested these two stages together, overriding the normal one-stage workflow for this pair. S1-S7 are ticked complete. S8 is next and has not started. Kev subsequently requested saving, committing and syncing the accepted changes.

- All apps owns a separate focusable, opaque full-screen popup. First app receives focus on entry, including when installed apps finish loading. Home cannot take focus underneath. Closing requests the original rail or home-row All apps control. The home row scrolls its opener into view after a selection changes its length.
- Launching apps remains the default. **Select Home apps** enters selection directly, focuses the first app and toggles draft green HOME badges with OK. **Confirm** writes the entire selection in one DataStore edit. **Cancel** and **Back** close without saving. A failed save keeps the draft open with a retry message. The existing hero-app choice remains available outside selection.
- Kev agreed unlimited selected apps with horizontal scrolling. Existing visible fallback apps start selected on a never-configured Home. An explicitly confirmed empty selection stays empty rather than restoring fallback apps. Existing saved pins are retained until Confirm.
- Home horizontal padding is reduced from 36dp to 12dp on each side, keeping room for focus rings and releasing the rail's reserved width when hidden. Kev additionally requested All apps fully cover Home, including the header. The initial Android dialog left a 24px top/bottom window gap observed in a TV screenshot; the final popup removes that gap and uses an opaque background.
- Validation: final `:app:assembleDebug` passed (38 tasks, 4 executed); `git diff --check` passed. Existing SDK XML warning remains nonfatal. Basic UI checks observed first-app focus, Right moving to the second app and Back restoring the home-row All apps tile. Some transition-time UI dumps failed and are not counted as evidence. No errors appeared in the checked recent AndroidRuntime/ActivityManager logs.
- Final deployment: matching signed APK `%TEMP%/bigdog-apk-verify/BigDog-S6-S7-popup-signed.apk`, installed with `adb install -r` on `192.168.3.19:5555`. VersionCode `1`, versionName `1.0`, lastUpdateTime `2026-11-11 00:47:51`. Installed APK SHA-256 matches the signed file: `9cfe013d65bac09ad70364d1d2621b161bb913cae739b3b28bbff2161bbe2eac`. App data and signing setup preserved; no TV settings or package states changed. Cold-start wait timed out, then the read-only foreground check confirmed BigDog resumed and its popup owning focus. This does not resolve S13.
- Acceptance: Kev confirmed **perfect, now full screen, all apps working fine as well.** For S7 he explicitly answered **All three work - mark S7 complete** to the Confirm/Cancel/Back test and added **s7 works perfectly**. Both stages are accepted. No separate TV restart, zero-selection or per-sidebar-route results are claimed.
- Test brief supplied: open All apps from sidebar and Home tile; expect first-app focus, arrows staying inside, OK launching and Back restoring the opener. In Select Home apps, toggle two on and one off, Confirm and reopen to verify; repeat with Cancel and Back and expect saved Home unchanged. Extra selected apps scroll horizontally. Check Home margins with sidebar On/Off, empty selection and persistence after restart when convenient.
- Next: S8 read-only HDMI / AV diagnostics, only when requested. Kev explicitly authorised committing and syncing this accepted S6/S7 update. The accepted app build is already installed; this handoff update changes documentation only.

## Previous S3 handoff — 10 November 2026

**Status: S3 accepted by Kev; return issue deferred to S13.** Kev first confirmed **wifi settings working**, then clarified **yes but s3 is working**. S3 is ticked on that overall confirmation. Reliable return is not recorded as passed, and no additional individual shortcut tests are claimed.

- Both the header Wi-Fi pill and quick tile already use `android.settings.WIFI_SETTINGS` through the shared view-model handler. This matches the PDF's standard-action-first approach. The notes' blocked-settings diagnosis no longer applies to Wi-Fi after S1: the action now resolves and launches `com.android.tv.settings/.connectivity.NetworkActivity` with `Status: ok`. Main Settings also resolves. No Xiaomi alternative is needed.
- Added warning/error logs under `BigDogSettings` for failed primary and fallback launches, including the action and exception. Existing routing, fallback and on-screen messages are preserved. No other shortcut was repaired or tested; no TV settings or package states changed.
- `:app:assembleDebug` passed (38 tasks, 4 executed). The existing SDK XML warning was nonfatal. Source review confirmed both Wi-Fi controls use the same handler. Failure branches were reviewed without disabling a working settings package to force errors.
- Signed a deployment copy using the existing matching key outside Git, checked its certificate against the installed APK, and installed with `adb install -r`. No uninstall; app data and Home setup preserved. Deployment file: `%TEMP%\bigdog-apk-verify\BigDog-S3-Wifi-signed.apk`.
- Installed versionCode `1`, versionName `1.0`, lastUpdateTime `2026-11-10 23:51:17`. Installed APK hash matches the signed deployment copy: `6e9e85b0cca1a576d3a5f05cac2d120ae960b594c1ac4e41e54f8ec66bc1e981`.
- Initial cold-start wait timed out; follow-up returned `Status: ok` in 7 ms. BigDog is resumed and owns the focused window. No errors appeared in the checked recent AndroidRuntime/ActivityManager/BigDogSettings logs.
- **Remote test:** open the header Wi-Fi pill with arrows and OK. Inspect the network screen without changing settings, then Back to BigDog. Repeat through quick settings → Wi-Fi. Both should open the same network screen with usable focus and navigation. Back may return to the still-open quick panel; close it with another Back. Check Home returns to BigDog. Do not toggle Wi-Fi or disconnect the TV.
- Next stage: S5, not started. S13 is planned separately at Kev's request; the former S12 final check is now S14, with S12 reserved. This acceptance update is documentation only, so no new APK build/install is required. Commit `004702d` contains the logging and earlier plan changes; this follow-up acceptance has not been committed or synced.

## Previous S2 handoff (superseded by S3 Wi-Fi above)

- Current step: **S2 accepted; S3 has not started.** S1 remains accepted.
- Version control: `b45be5e` records the plan, project instructions and accepted S1 result. Kev authorised saving and syncing the accepted S2 changes and mandatory automatic-install rule to GitHub.
- App implementation: all three Cast controls route to SmartShare through one helper; the manifest includes package visibility for SmartShare.
- TV state: `com.android.tv.settings` remains enabled with Kev's approval from S1. The S2 APK was installed in place over ADB on 10 November 2026 at 23:46:16. No packages were enabled/disabled during S2 and no uninstall was performed.
- Local checks: reviewed all three control paths, confirmed SmartShare's normal launcher activity still resolves on the TV, and passed `git diff --check`. Local `:app:assembleDebug` passed with Gradle 9.3.1 and the installed Android Studio JBR (38 tasks executed). No extra unit tests were added for this small launch-routing change. The build's missing optional Google Services configuration and SDK XML warnings did not prevent compilation or APK packaging.
- Installation: the default local debug certificate differed from the installed APK. Found the existing matching key at `C:\Users\Kev\Documents\BigDog-debug.keystore`, outside the repository. Used it to sign a deployment copy at `%TEMP%\bigdog-apk-verify\BigDog-S2-signed.apk`; verified the matching certificate before `adb install -r`. The installed APK's SHA-256 matches that signed S2 file. No key contents were added to Git and signing/build configuration is unchanged. Use the existing matching key for future local deployments, or the existing CI secret for CI builds; do not use the default local debug certificate for a TV update.
- Launch check: the initial cold-start wait timed out, but the follow-up start returned `Status: ok` to the running top activity. BigDog owns the foreground activity and focused window; no crash/ANR appeared in the checked recent logs. Kev subsequently confirmed SmartShare works.
- Next action: S3, starting with Wi-Fi only, when work resumes. No S3 changes have been made. Automatically build, install and verify each future finished app update before requesting remote testing. Committing and pushing remain with Kev unless explicitly delegated.
- S2 test brief supplied: header Cast, quick-settings Cast and the Screen cast input pill should each open SmartShare; check arrows, OK and Back, then cast from an available device. Kev accepted the result with **smart share working**; no individual platform was specified.
- Earlier report before installation: Kev saw **Cast shortcut not found on Xiaomi ROM; opened main Settings** in the old APK, last updated at **22:26:47 on 10 November 2026**, before the S2 build at **23:40:06**. That report was superseded by installing S2 at **23:46:16** and Kev's subsequent acceptance.

For every future handoff, update this section with the active step, what changed, local checks, exact TV test, result or blocker, and next action. Keep it at **Awaiting TV test** until Kev responds.

## Acceptance record

Only record acceptance after Kev confirms it. Include individual S3 shortcuts and the separate parts of S8/S9 when tested.

| Step | Change / build tested | TV result | Kev's confirmation and date |
| --- | --- | --- | --- |
| S1 | Enabled only `com.android.tv.settings`; existing APK, no code change | Main settings resolved and launched; Kev confirmed the remote test worked | "Works — keep settings enabled", 10 November 2026 |
| S2 | Three Cast controls routed to SmartShare; build passed; matching signed APK installed and hash-verified at 23:46:16 | Kev confirmed SmartShare works; specific casting platform not reported | "smart share working. update, save and sync", 10 November 2026 |
| S3 Wi-Fi opening | Existing standard route plus shared failure logging; signed APK installed at 23:51:17 | Settings opens. Intermittent black screen on return remains unresolved and is deferred to S13; no full return-navigation pass claimed | "wifi settings working", with black-screen report and request for S13, 10 November 2026 |
| S3 overall | Same installed build; no further app changes | Kev accepted the stage overall. Other shortcut results were not individually supplied; black-screen return remains open under S13 | "yes but s3 is working", 10 November 2026 |
| S4 | Sidebar toggle, persisted preference, full-width layout and retained settings/Search access; signed APK installed at 00:13:00 | Kev accepted the stage overall; full TV restart was not separately reported | "perfect", with request to commit, sync and mark complete, 11 November 2026 |
| S5 | Duplicate banner labels removed; icon fallback names retained; included in installed S4 build | Kev explicitly accepted S5 as already updated and requested it be ticked complete | Explicit S5 completion confirmation, 11 November 2026 |
| S6 | Full-screen All apps popup, first-app focus, underlying Home blocked and opener restoration; final APK installed at 00:47:51 | Kev confirmed full screen and All apps working | "perfect, now full screen, all apps working fine as well", 11 November 2026 |
| S7 | Draft HOME selection, atomic Confirm, Cancel/Back discard, unlimited horizontal Home row | Kev explicitly confirmed all three actions work | "All three work - mark S7 complete" and "s7 works perfectly", 11 November 2026 |
