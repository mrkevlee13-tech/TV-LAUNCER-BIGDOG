# BigDog Launcher stage plan

Updated and renumbered: **11 November 2026 (Asia/Shanghai)**, at Kev's request.

**Use the new S1–S5 numbering below from now on.** These are the remaining stages, not a reset of completed work. The old S1–S14 plan, all acceptance records, failures and deployment history are preserved in [the archived plan](archive/STAGE-PLAN-before-renumbering-2026-11-11.md). The unused old S12 reservation is removed from the active list.

Kev is preparing new notes. This update saves the current position before those notes arrive; it does not start another implementation stage.

## Current stage list

- [ ] **S1 — Home escape and response time** — current priority. Physical Home works in the latest reported test, but cold return still takes about 4.1 seconds and previous 4K memory-pressure failures remain relevant.
- [ ] **S2 — VLC video resume and pause on Home** — picture and sound confirmed; end-to-end resume, pause-on-Home and persistence still need acceptance.
- [ ] **S3 — Reliable return from external TV settings** — investigate the remaining black-screen/Back-return issue separately from Home.
- [ ] **S4 — Settings backdrop and presentation** — dim/blur BigDog behind settings where the actual screen supports it; agree limitations for opaque external settings.
- [ ] **S5 — Restart, saved-state and remote regression test** — final pass after the earlier stages are accepted or explicitly deferred.

## Working rules

1. Read this plan and `AGENTS.md`. Work on one stage at a time unless Kev explicitly combines or reorders them. His current notes take precedence over the background PDF and old assumptions.
2. **Home is the user's escape:** physical Home must override the current app, player, overlay or pending launch and return to BigDog's main screen with usable focus. Only activating Hero resumes its selected source. Do not change this rule to accommodate another feature.
3. Kev chose **pause VLC on Home**. Home should return to BigDog and silence/pause VLC, retaining the last video for explicit Hero activation.
4. Keep D-pad focus visible and predictable. OK activates the focused control; Back dismisses the current overlay or returns one level independently of Home.
5. Preserve BigDog's package ID, signing compatibility, app data, saved preferences, GitHub Actions workflow and existing package states. Keep keys/keystores/encoded signing material out of Git. Do not uninstall BigDog to bypass a signing mismatch.
6. After every finished app update, build and install the compatible signed APK in place over ADB, verify the installed build/hash and launch it, then give exact TV test steps. This is standing permission; report the precise TV/signing blocker if installation cannot be completed.
7. On this Android 9 TV, preverify the newly installed package before its first launch (`cmd package compile -m verify -f com.aistudio.bigdoglauncher.b2tv`) and verify BigDog's Home interceptor is enabled/bound afterwards. Preserve any unrelated accessibility services. Do not count installation-time restarts as a normal return test.
8. Stop for Kev's TV test. Tick stages only after his confirmation; retain failed tests and documented limits. Never infer acceptance from compilation, an activity launch, decoded frames or an ADB screenshot alone.
9. Commit/push only when explicitly requested. Kev authorised this local save/commit; **no push** was requested. Documentation-only acceptance/renumbering does not require another APK install.

## Accepted baseline — retained, not reopened by renumbering

The archived S1–S10 were accepted by Kev. This includes main TV settings and SmartShare shortcuts; settings shortcuts overall (with return reliability deferred); sidebar visibility; clean app artwork; full-screen All apps focus; draft Home-app selection with Confirm/Cancel/Back; the HDMI/USB/source Hero layout without redundant watch shortcuts or AV; and the accepted wallpaper artwork, full-screen preset grid, motion/dim/blur controls and preview behaviour.

Further confirmations on 11 November:

- **VLC picture:** Kev confirmed the movie is visible after replacing the audio-only media-session launch with VLC's explicit video entry point.
- **VLC sound:** Kev confirmed **“arc selected, now has sound.”** He clarified that the soundbar source is labelled **TV/ARC**. Use that exact name in future instructions. The bar's HDMI source receives the directly connected PS5; TV/ARC receives VLC and other TV-app audio through the existing cable.
- **Home:** Kev reports it works, but with a slow response. This does not close new S1.
- **Projectivy:** Kev uninstalled it and requested removal of its remnants. Its stale accessibility components and sole boot-completed entry were removed. BigDog's interceptor is restored; do not reintroduce Projectivy.

Picture/sound acceptance does not imply that all of new S2, Home responsiveness, settings return or restart tests have passed.

## S1 — Home escape and response time

**Origin:** old S11, reopened after the VLC/4K failure.

Home must dismiss BigDog screens/overlays, close HDMI full screen, cancel pending file/VLC launches and restore Hero focus. It must remain the escape even when the current app is faulty. Pausing VLC must not delay the Home launch or start any replacement media.

Implemented: activity re-entry reset, cancellation of pending Hero launches, focus restoration, an existing Home interceptor moved into its own `:home` process, explicit input-session release and background artwork release. The latest startup guard avoids decoding the initial app artwork twice.

**Still open:** the latest measured cold launch is **4147ms**, essentially unchanged from the earlier 4157ms. Memory pressure from 4K playback previously killed both the UI and the Home service and delayed service restart. Isolating the service survived an artificial UI-only termination test, but that alone was insufficient proof under real playback. More recent playback retained the Home process while the UI was reclaimed.

**Firmware limit:** implicit Home resolves to Android's `FallbackHome` despite BigDog's default preference. Read-only firmware inspection found Xiaomi's third-party Home allowlist. The targeted Home-only property change was rejected by the TV; the system-owned allowlist is protected. No firmware property/root/protected-file change was made. Do not promise an unconditional native Home guarantee from the current background interceptor. Preserve this blocker until solved or Kev explicitly accepts a limitation.

**TV test:** press physical Home once from VLC playing the same 4K video, another app, Search, All apps, Wallpaper Studio and quick settings. Expect BigDog's main screen with Hero focused, VLC silent/paused and no late player takeover. Activate Hero then immediately press Home to exercise cancellation. Measure warm return separately from a return after UI-process reclamation. No repeated presses or persistent black trap are acceptable. Back must still work independently.

**Acceptance:** open; await Kev's new notes and subsequent test.

## S2 — VLC video resume and pause on Home

**Origin:** the playback/Hero follow-up to archived S8/S11.

Hero follows the app the user opens, including VLC opened outside BigDog. For VLC it shows the real last-video title and current/paused status. Hero activation opens the actual video file through VLC's exported video entry point, using the recorded position when available. Home pauses VLC and returns to BigDog; it must never trigger continuation.

Implemented: a brief MediaBrowser connection reads VLC's title, library ID, queue file URI and available position. The small record is persisted in the existing DataStore. VLC's video wrapper receives video MIME plus its `item_location` URI extra, avoiding the earlier audio-only `playFromMediaId` regression. Missing/unavailable information falls back clearly to opening VLC. No invented movie metadata/progress, notification-reading permission or live embedded VLC picture is promised. No VLC private data or settings were reset.

**Confirmed:** movie picture and sound with soundbar source **TV/ARC**. **Still to confirm:** Home actually pauses audible playback, repeated Hero resume and position, a different video's title replacing the old one, persistence and missing-file behaviour.

**TV test:** play a video in VLC, press Home and expect silent BigDog with that video's title in Hero. Activate Hero and expect picture plus sound at the recorded/VLC-saved position or VLC's own resume prompt. Repeat with a different video, then after a later restart. Check a missing/disconnected USB file fails understandably. Ensure Home preempts a pending resume without delayed playback.

**Acceptance:** partial; leave unticked until Kev accepts the complete behaviour.

## S3 — Reliable return from external TV settings

**Origin:** return-reliability portion of old S13; old settings-shortcut acceptance remains valid.

Reproduce the intermittent black-screen/frozen-looking return from external settings. Establish which activity/window owns the black surface and inspect lifecycle, tasks, focus and logs. Do not assume the VLC memory-pressure issue or post-install verifier delay explains every settings return.

**TV test:** open Wi-Fi from the header and quick settings, navigate without changing Wi-Fi, then Back out one level at a time. Expect BigDog promptly with visible focus and no persistent black screen or repeated key presses. Test physical Home separately. Do not disconnect Wi-Fi, unpair the remote or change unrelated package states for testing.

**Acceptance:** not started under this new numbering.

## S4 — Settings backdrop and presentation

**Origin:** presentation portion of old S13.

Aim for settings over BigDog's home backdrop, dimmed and slightly blurred, with inexpensive rendering on Android 9. Distinguish BigDog's own quick panel from external Android/Xiaomi settings. A launcher backdrop cannot show through an opaque external window; inspect actual support and agree an alternative/limitation with Kev before claiming this complete. Preserve access to real settings.

**TV test:** inspect each supported entry, confirm readable settings and the intended backdrop where supported. Only the active settings screen receives focus. Back restores normal Home appearance and usable focus. Check responsiveness. Record opaque external screens and any explicitly accepted limitations.

**Acceptance:** not started.

## S5 — Restart, saved-state and remote regression test

**Origin:** old S14. Run after earlier stages pass or Kev explicitly defers them.

**TV test:** restart the TV normally and verify BigDog startup and physical Home. Check saved pins/empty selection, sidebar choice, wallpaper/photo/motion settings and last entertainment source. Exercise All apps and Home-app Confirm/Cancel/Back, SmartShare, external settings return, HDMI selection-to-Hero-to-full-screen, USB/VLC picture and sound on TV/ARC, VLC pause/resume and missing-media handling. An empty HDMI must not show a false green Connected state. AV and redundant watch shortcuts must remain absent. Check arrows/OK/Back/Home and sensible restored focus throughout. Verify no unrelated package or Home setup change was left behind; record any agreed limits.

**Acceptance:** not started.

## Numbering map

| New stage | Previous reference |
| --- | --- |
| S1 — Home escape and response time | Old S11, reopened |
| S2 — VLC video resume and pause on Home | Playback/Hero follow-up to old S8/S11 |
| S3 — Reliable return from external TV settings | Old S13, return reliability |
| S4 — Settings backdrop and presentation | Old S13, presentation |
| S5 — Restart, saved-state and remote regression | Old S14 |

Old S1–S10 remain accepted baseline; old S12 was only reserved. References inside the archive retain their original numbering.

## Saved build and validation

- Package: `com.aistudio.bigdoglauncher.b2tv`; versionCode **1**, versionName **1.0**. TV: `192.168.3.19:5555`, Xiaomi MiTV_ASTP0, Android 9/API 28. Recheck availability before future work.
- Current installed signed build: `%TEMP%/bigdog-apk-verify/BigDog-VLC-cold-start-signed.apk`; lastUpdateTime **2026-11-11 04:24:01**; firstInstallTime preserved at 2026-11-10 22:26:47.
- Installed/deployment SHA-256 match: `8d3163c98f82ca3f86f179c3ad3746b51061f96f8b807e6582f4311f4a4403af`. Existing compatible certificate verified; installed with `adb install -r`, preverified and launched. Home interceptor bound after deployment. Soundbar source was changed by Kev, not remotely.
- Latest APK build passed: 38 tasks, 4 executed, 34 up-to-date. The preceding video/pause build passed all **10 regression tests** (input states, Hero history, Home reset and VLC position/URI/status). The final lifecycle guard changes only duplicate startup refresh behaviour. `git diff --check` passed.
- This save includes the existing app changes and this renumbered plan. No further app code change/build/TV installation is required for this documentation-only reorganisation. Commit locally at Kev's request; do not push. Await his new notes before starting the next implementation change.
