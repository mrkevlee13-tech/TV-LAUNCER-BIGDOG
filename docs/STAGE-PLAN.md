# BigDog Launcher stage plan

Updated: **11 November 2026 (Asia/Shanghai)**. Kev approved the summary of his new notes and explicitly requested **animation, wallpapers, appearance and logo setup first**. This overrides the previous implementation order.

Use **S1–S11 below** from now on. Each request has one owning stage; the cross-reference table shows where the previous S1–S5 work moved. The [previous S1–S5 plan](archive/STAGE-PLAN-S1-S5-before-notes-2026-11-11.md) and [original plan/history](archive/STAGE-PLAN-before-renumbering-2026-11-11.md) are archived, not parallel task lists.

S1 is implemented locally and is awaiting TV acceptance. Work remains on S1 until Kev confirms the result; later stages have not started.

## Current stage list

- [ ] **S1 — BigDog branding, animation, wallpapers and appearance** — first, as requested: logo, startup animation, clock-side icon, supplied default video and six wallpapers, app-icon and Hero-card size sliders, title-free wallpaper thumbnails.
- [ ] **S2 — Home app ordering and custom folder shortcuts** — long-press rearrangement, USB/VLC shortcut and MovieBoxPro Downloads, configurable in settings.
- [ ] **S3 — Hero and input presentation, preview and signal status** — remove unwanted labels/icons/HDMI overlay, refine input icons, stabilise All apps preview behaviour, separate soundbar connection from active PS5 video.
- [ ] **S4 — Home escape, complete first frame and background-app view** — previous S1 plus empty-load fix, double Home, recent/background apps and protected Clear all.
- [ ] **S5 — VLC video resume and pause on Home** — previous S2; retain confirmed picture/sound, finish continuation and pause acceptance.
- [ ] **S6 — Tomato VPN setup and connection persistence** — private account setup and protection through app switching, Home, background management and playback.
- [ ] **S7 — TV browser access and installation** — assess Chrome Android APK/TV compatibility and provide the agreed browser.
- [ ] **S8 — Reliable return from external TV settings** — previous S3, with its black-screen/Back-return investigation retained.
- [ ] **S9 — Settings backdrop and presentation** — previous S4; external settings presentation is separate from S1's Home/wallpaper appearance.
- [ ] **S10 — TV boot-to-BigDog and Redmi splash feasibility** — investigate firmware boot branding separately from BigDog's own S1 startup animation.
- [ ] **S11 — Restart, saved-state and full remote regression test** — previous S5, extended to all new features.

## Working rules

1. Read this plan and `AGENTS.md`. Work on one stage at a time, stop for Kev's TV test, and tick only after he confirms it works or accepts a documented limitation. His current requests take precedence over the background PDF and old assumptions.
2. **Single Home remains the escape:** it must override the current app, player, overlay or pending launch and return to BigDog's main screen with usable focus. Only activating Hero resumes a source. The newly requested second Home press opens the background-app view; do not add a wait to the first press just to detect a double press.
3. Kev chose **pause VLC on Home**. Retain the video for explicit Hero resume. Wallpaper/startup animations must not take audio focus, play audible sound or delay the Home escape.
4. Keep D-pad focus visible and predictable. OK activates; Back dismisses the current overlay or returns one level independently of Home. Long-press/reordering and sliders must work with the remote.
5. Preserve BigDog's package ID, compatible signing, app data, saved settings, GitHub Actions workflow and unrelated package states. Keep keys, keystores and encoded signing material out of Git; never uninstall BigDog to bypass a certificate mismatch.
6. Build and install each finished app update in place over ADB automatically, verify installed version/build/hash and launch it before the TV handoff. Standing permission applies. Report an exact TV/signing blocker if installation cannot be completed.
7. On this Android 9 TV, preverify the installed package before its first launch (`cmd package compile -m verify -f com.aistudio.bigdoglauncher.b2tv`) and verify the Home interceptor is enabled/bound afterwards. Preserve unrelated accessibility services. Installation restarts are not ordinary return tests.
8. Keep the observed 4K memory-pressure failures in scope. Pause/release wallpaper playback when BigDog is hidden or covered by full-screen input playback. Test decoder/session competition when an HDMI Hero preview is present. Never infer a pass from compilation, an activity launch or decoded frames alone.
9. Use provided VPN credentials only for private login in the VPN app. Do not copy credentials into this plan, code, assets, logs or Git. Do not treat the redacted credential text as permission to invent missing characters.
10. Commit or push only on explicit request. The previous snapshot was committed locally as `4da1d6a`; that completed request is not standing permission to commit every subsequent change. Kev explicitly requested a local commit of the S1 implementation and updated plan on 11 November. This does not mark S1 accepted or authorize pushing.

## Accepted baseline and current observations

The original archived S1–S10 were accepted by Kev: TV settings/SmartShare shortcuts, sidebar visibility, clean app artwork, All apps focus, Home-app Confirm/Cancel/Back selection, source Hero/inputs without redundant watch shortcuts or AV, and the then-current wallpaper controls. New requests below extend or replace specific parts of that baseline; do not reimplement accepted behaviour elsewhere.

- VLC's movie picture is confirmed. Sound is confirmed after Kev selected the soundbar source **TV/ARC**; he supplied that exact label. Its HDMI source receives the directly connected PS5. The cable to the TV remains unchanged.
- Physical Home currently works, but cold return is still about **4.1 seconds**. Earlier real 4K playback killed the UI and Home-service processes; an artificial UI-only termination test was not sufficient proof of reliability.
- Home pause, repeated VLC resume/position/persistence and a complete restart regression remain awaiting acceptance. Keep these open in S4/S5/S11 respectively.
- Projectivy was uninstalled by Kev; its stale accessibility and boot entries were removed. Do not reintroduce it.
- Kev's fragment **“issue, when I was playing a video from VLC…”** is incomplete. Keep it flagged under S5 and ask for the missing symptom before inventing another VLC defect. It does not block S1.

## S1 — BigDog branding, animation, wallpapers and appearance

**Priority:** first. This is the sole owner of Home branding, supplied wallpaper assets, Wallpaper Studio appearance and app-icon sizing. It does not own external settings backdrops (S9), the empty/incomplete Home load (S4), or the firmware Redmi boot logo (S10).

- Correct the missing BigDog app icon/branding inside the launcher. Use Kev's newly attached mint-green BigDog PNG as the chosen Home-screen logo, including beside the clock; preserve its proportions and any source transparency. This supplied logo takes precedence over a recreated logo or a crop from the wallpapers. Provide the requested branding animation when the app starts/loads, including after TV startup reaches BigDog.
- Use the supplied `BigDog-20s-1080p.mp4` as the default animated Home wallpaper. Inspect the file/codec and optimise playback/file size for this TV. A 20-second wallpaper must not become a mandatory 20-second startup wait.
- Add all six supplied static artworks as wallpaper options: Mint Final Frame, Cobalt Ribbons, Violet Ribbons, Ember Ribbons, Lunar Orbit and Copper Facets. Improve the current poor choices using these supplied assets. Optimise sizes without visibly damaging the branding; fill the screen and avoid unwanted bars, clipping or distortion.
- Retain custom wallpaper support, Dim, optional Blur, Motion control and saved selection. Keep the gallery full screen with all thumbnails visible and usable focus, without the old truncated carousel.
- Add a remote-friendly Home app-icon size slider supporting at least **30% smaller than the current size**, plus increase/decrease. Save the selected size; preserve readable labels, focus rings and usable navigation. App order is owned by S2.
- Add a separate remote-friendly Hero-card size slider (70?110%) and save it with appearance settings. Keep wallpaper thumbnail artwork visible without preset titles.
- Keep wallpaper playback silent and suspend it when hidden/full-screen HDMI takes over. Measure smoothness and memory/decoder impact rather than reintroducing the VLC/Home failures.

**Asset inventory:** original files remain on Kev's Desktop; they have not been copied, converted or modified in this planning update. Existence was checked on 11 November. Source video is about 17 MB; source PNGs are about 2.1–2.3 MB each.

| Asset | Source |
| --- | --- |
| Home/clock-side logo | `C:/Users/Kev/Desktop/bigdoglaucnericon.png` (the exact PNG attached in chat) |
| Default animation | `C:/Users/Kev/Desktop/BigDog-20s-1080p.mp4` |
| Mint Final Frame | `C:/Users/Kev/Desktop/BigDog-Static-Wallpapers/01-Mint-Final-Frame.png` |
| Cobalt Ribbons | `C:/Users/Kev/Desktop/BigDog-Static-Wallpapers/02-Cobalt-Ribbons.png` |
| Violet Ribbons | `C:/Users/Kev/Desktop/BigDog-Static-Wallpapers/03-Violet-Ribbons.png` |
| Ember Ribbons | `C:/Users/Kev/Desktop/BigDog-Static-Wallpapers/04-Ember-Ribbons.png` |
| Lunar Orbit | `C:/Users/Kev/Desktop/BigDog-Static-Wallpapers/05-Lunar-Orbit.png` |
| Copper Facets | `C:/Users/Kev/Desktop/BigDog-Static-Wallpapers/06-Copper-Facets.png` |

**TV test:** open BigDog and inspect branding/startup animation and the icon beside the clock. Confirm the supplied video is the default background, remains silent and navigates smoothly. Inspect all six new wallpaper choices full screen and in the complete gallery; apply/reopen and check saved selection and controls. Use D-pad Left/Right on both size sliders, including 30% smaller and larger, apply and reopen to verify saved size/focus. All wallpaper thumbnails must fit without preset titles. Open VLC or full-screen HDMI and return; wallpaper must release playback resources while away. Single Home must remain responsive throughout. Firmware boot-logo replacement is tested only in S10.

**Status:** built, installed in place and launched on the TV; awaiting Kev's remote acceptance. Keep S1 unticked. Version remains 1.0 (code 1); installed update time 11 November 2026 13:11:33. Pulled installed APK and signed build match SHA-256 `c5328bb939d075c615a29babd2256878f69da322e0d591dbede7729fc4a49ead`. Matching signing certificate and saved app data/Home interceptor are preserved; Xiaomi voice accessibility remains enabled.

Implementation: supplied motion + six stills + Custom photo replace the generated gallery choices in a fixed two-row grid. Old built-in selections map to the new motion default; custom photo selections and other saved controls remain. Home app size (70?120%) and Hero-card size (70?110%) have separate slider/D-pad controls; choose **Apply appearance** to save. Wallpaper preset titles are removed from the thumbnail grid; accessible names and selection indicators remain. The placeholder rail ring is replaced with the supplied logo, also shown beside the clock with an 850ms reveal. Motion is silent, asynchronously prepared, released when hidden/full-screen HDMI or another app opens, and uses the still Mint artwork when disabled/unavailable. It also yields its decoder to HDMI Hero preview. Existing Android blur support is retained; this stage does not add a separate Android 9 blur renderer.

**Verification:** debug build passed; existing hardware/Hero/VLC/Home tests passed (10 tests). TV screenshot confirms all seven preset thumbnails plus Custom photo fit together, preset titles are absent and both size sliders are visible. A vendor MediaPlayer teardown hang found during S1 testing was corrected by serial worker-thread cleanup, stopping before release and avoiding `setSurface(null)`; TV logs confirm decoder release. No acceptance of physical-remote behaviour or playback regressions is inferred from these checks.

## S2 — Home app ordering and custom folder shortcuts

This owns Home content arrangement and folder destinations; icon size remains owned by S1.

- Long-press an app on Home to enter move mode, move it left/right with the remote, confirm the order and persist it. Define usable Cancel/Back behaviour and visible move/focus feedback. Preserve the existing unlimited Home selection and Confirm/Cancel/Back selection flow.
- When the Samsung 980/another USB drive is connected, provide a Home shortcut to its video folder through VLC or the supported folder-browsing route.
- Provide a MovieBoxPro Downloads folder shortcut. Verify the actual app/path/access route; do not substitute an unrelated folder or promise direct access to private app storage.
- Let Kev configure folder shortcuts in settings, including destination, display name, visibility and order where supported. Handle absent/unmounted drives clearly without a dead tile or misleading connection state.

**TV test:** hold an app, move it both directions, confirm and reopen/restart to check order. Test cancelling a move and ordinary OK launch. Connect/disconnect USB, open the configured folder and verify its destination. Open MovieBoxPro Downloads and check the agreed destination. Edit/hide shortcuts and confirm persistence. Playback continuation itself is tested in S5.

## S3 — Hero and input presentation, preview and signal status

This owns Hero/input visuals and hardware status. VLC playback commands belong to S5; Home escape belongs to S4.

- Remove the **Selected source** label and Hero's bottom-right play icon. Retain useful actual app/video/input information.
- Replace the heavy bordered input-card appearance with cleaner, recognisable input icons and readable labels. Keep a clear focus indication for remote navigation.
- Investigate the reported HDMI preview interruption when All apps opens and restoration when Back closes it. Avoid unnecessary teardown/renegotiation while retaining full-screen All apps focus and correct audio/session lifecycle. Report any native-TV limitation rather than hiding it or creating competing input sessions.
- Remove the persistent **Back to Home – HDMI** overlay once the HDMI source is full screen. Physical Back/Home must still work; a full-screen picture must be unobstructed.
- Distinguish the soundbar's physical HDMI connection from an active PS5/video signal. A cabled soundbar must not be presented as proof that PS5 is on. Show separate connected-device and live/no-signal/unknown information where the TV exposes it; do not falsely mark a genuinely connected port unplugged or guess the downstream PS5 state.

**TV test:** inspect the cleaned Hero and input icons with arrows/OK. Open/close All apps with HDMI selected and check preview stability, focus and audio. Activate full-screen PS5 and confirm no permanent overlay. Turn PS5 off while the soundbar remains connected, switch the bar to TV/ARC, refresh and verify truthful device/signal wording. Check an empty HDMI and physical Back/Home.

## S4 — Home escape, complete first frame and background-app view

**Merged from previous S1**, plus the new single/double Home and incomplete-loading notes. This owns Home routing and first-frame readiness, not wallpaper artwork (S1), firmware boot branding (S10) or external-settings Back return (S8).

- Single Home must immediately start the escape to BigDog, dismiss overlays, close HDMI full screen, cancel pending launches and restore Hero focus. Invoke S5's pause-on-Home behaviour for VLC. It must remain usable when an app/player has a problem.
- Double Home opens a remote-friendly view of actual recent/background apps, with a **Clear all** action. Do not delay the first Home press. Back/one Home dismisses the view predictably.
- Investigate this TV's supported task-management route before promising a full task switcher or close-all capability. Keep recent-history records distinguishable from verified running tasks. Clear eligible apps only; **protect BigDog/Home services and VPN providers**. Identify those providers before testing Clear all, even though VPN login/configuration is in S6.
- Fix the empty/incomplete Home frame shown before apps and saved settings appear. Restore a coherent saved Home state promptly and measure warm/cold returns separately.

**Existing work/limit retained:** re-entry reset, pending-launch cancellation, focus restoration, separate `:home` interceptor, session/artwork release and startup-scan guard are implemented. Latest cold start remains **4147ms**. Memory pressure previously killed both UI and Home service. Native Home still resolves to `FallbackHome` despite BigDog's preference; the firmware allowlist/property is protected and the attempted Home-only change was rejected. Do not represent the interceptor as a guaranteed native Home replacement or silently alter firmware/packages. Keep this blocker until solved or explicitly accepted.

**TV test:** single Home from 4K VLC, another app, Search, All apps, Wallpaper Studio and settings must return to usable, fully populated Home with VLC paused and no late player takeover. Measure cold/warm delay. Activate Hero then immediately Home to test cancellation. Double Home from an app and from Home should open the background-app view. Exercise focus/Back/Clear all and confirm both VPN and Home services remain available. A persistent black trap or delayed app takeover is a failure.

## S5 — VLC video resume and pause on Home

**Previous S2 retained.** This owns the VLC playback integration and pause command used by S4. Hero styling is only in S3; shortcut creation is only in S2.

Hero follows the app opened, including VLC launched outside BigDog, and remembers its real last-video title/file/position where available. Only explicit Hero activation resumes video. Home pauses VLC and returns to BigDog without hidden audio or delayed playback.

Implemented: brief MediaBrowser metadata/queue read, persisted title/library ID/file URI/position, actual playing/paused status, VLC's exported video wrapper with video MIME plus `item_location`, Home pause and pending-launch cancellation. Picture and sound on soundbar **TV/ARC** are confirmed. Do not restore the audio-only `playFromMediaId` launch. No invented progress, live embedded VLC picture or private-player data access is promised.

**TV test:** play a video, press Home and verify silence plus its title in Hero. Resume through Hero and verify picture/sound and recorded/VLC-saved position or VLC's resume prompt. Repeat with another video, after restart and with a disconnected/missing USB file. Home must preempt a pending resume. Capture the missing details of Kev's unfinished VLC issue before adding a further defect to this scope.

**Status:** partial acceptance; continuation/pause/persistence and any newly clarified issue remain open.

## S6 — Tomato VPN setup and connection persistence

Configure the existing Tomato VPN app using Kev's supplied account privately. No credentials are recorded here or in source control. Do not hardcode a login into BigDog. If a secret is incomplete/redacted, use the VPN's proper private login flow rather than guessing.

Investigate why the connection stopped under the previous launcher. Configure supported background/always-on behaviour where available and verify it through app switching, Home and memory-heavy playback. Preserve VPN when S4's background-app management/Clear all runs. Do not promise immunity to firmware process-killing without evidence or silently change unrelated services.

**TV test:** connect in Tomato VPN and verify the connection in the VPN app. Open an app requiring VPN, switch apps, press Home, play VLC/HDMI and use the background-app view/Clear all. Verify VPN remains connected and the requiring app still works. Test restart/sleep persistence only for the behaviour actually configured and agreed. Never log or commit login fields.

## S7 — TV browser access and installation

Provide ready-to-use browser access from Home. Assess Kev's Chrome Android APK suggestion for Android 9/TV/remote compatibility and supported installation/distribution before selecting a browser. Verify the app actually installs and is usable with the remote. Explain any limitation and agree the browser choice rather than silently substituting a different product or web container. Preserve BigDog's identity/signing and existing data.

**TV test:** launch the agreed browser from Home, enter an address, navigate a page, use Back and return with Home. Verify readable scaling, remote text entry/focus and VPN compatibility. Record any pointer/keyboard dependency before acceptance.

## S8 — Reliable return from external TV settings

**Previous S3 retained.** Reproduce the intermittent black-screen/frozen-looking Back return. Establish which activity/window owns the black surface and inspect lifecycle, tasks and focus. Do not assume every settings return has the same cause as VLC memory pressure or a post-install delay. S4 owns the separate Home/initial-render problem.

**TV test:** open Wi-Fi from the header and quick panel, navigate without changing Wi-Fi and Back out one level at a time. Expect BigDog promptly with visible focus and no persistent black screen or repeated presses. Check Home through S4. Do not disconnect Wi-Fi, unpair the remote or alter unrelated package states for testing.

## S9 — Settings backdrop and presentation

**Previous S4 retained.** This owns BigDog's settings-panel backdrop and supported external-settings presentation. S1 owns wallpaper selection, Home branding and icon sizing; do not duplicate those controls here.

Aim for settings over a dimmed, slightly blurred BigDog backdrop, using inexpensive rendering on Android 9. Distinguish BigDog's own panel from Android/Xiaomi activities. An opaque external settings window cannot show BigDog behind it; inspect actual support and agree an alternative/limitation before claiming success. Preserve access to real settings.

**TV test:** inspect each supported settings entry, readability/backdrop and focus ownership. Back restores normal Home appearance and focus. Check performance and record opaque screens and any explicitly accepted limitations.

## S10 — TV boot-to-BigDog and Redmi splash feasibility

Investigate whether the TV can start directly into BigDog and whether the manufacturer Redmi splash can be disabled/replaced on this exact firmware. Distinguish the firmware/bootloader/Android boot animation from BigDog's own app startup branding, which is entirely S1.

Start read-only; record actual support, native Home restrictions and any required privileges. Do not promise that an ordinary launcher APK replaces a firmware boot logo. Any proposed firmware/system-file/package change must be concrete, backed up and separately reviewed/authorised before execution; do not perform rooting or boot-image changes as an incidental wallpaper task.

**TV test:** only after an agreed supported approach, restart normally and record the actual boot sequence, time until usable BigDog, Home behaviour and any manufacturer screen that remains. If modification is unsupported, record the exact limit for Kev's explicit acceptance rather than claiming it is removed.

## S11 — Restart, saved-state and full remote regression test

**Previous S5 retained and extended.** Run after earlier stages are accepted or explicitly deferred; record every agreed limit.

**TV test:** normal TV restart into BigDog; new branding/default animation/gallery and saved wallpaper/motion/dim/blur; icon size, Home order/selection and folder shortcuts; single/double Home, Clear all and VPN survival; Hero information and full-screen HDMI without overlay; truthful connected-versus-signal states; USB/VLC picture/sound on TV/ARC, pause/resume/title/position and missing-media handling; browser, SmartShare and settings Back/Home. Check focus/arrows/OK/long-press/Back across all controls. AV and redundant watch shortcuts remain absent. Confirm package/signing/data and unrelated TV settings remain intact. Do not count installation restarts or decoded frames alone as a TV pass.

## Cross-reference and ownership — no duplicate implementation stages

| Previous stage / new note | Sole owning stage |
| --- | --- |
| Previous S1: Home escape, cold response | S4 |
| Empty Home before apps/saved setup appears | S4 |
| Double Home, recent/background apps, Clear all | S4; consumes VPN protection rule from S6 |
| Previous S2: VLC resume and Home pause | S5; S4 invokes the pause contract |
| Unfinished extra VLC issue | S5, awaiting the missing symptom |
| Previous S3: external-settings return | S8 |
| Previous S4: settings backdrop | S9 |
| Previous S5: final regression | S11 |
| Missing BigDog icon, clock-side logo, own startup branding | S1 |
| Supplied default animation and six wallpapers, asset optimisation | S1 |
| Home app-icon size slider / 30% smaller | S1 |
| Long-press Home app rearrangement | S2 |
| USB/VLC and MovieBoxPro Downloads shortcuts/customisation | S2 |
| Hero label/play icon, input icon design, All apps preview interruption | S3 |
| Full-screen HDMI overlay, soundbar versus active PS5 status | S3 |
| Tomato VPN private login and persistent connection | S6 |
| Preloaded/accessible browser, Chrome APK assessment | S7 |
| Redmi firmware splash and boot-to-BigDog feasibility | S10 |

Cross-stage integration tests are intentional; they do not create a second owner for the feature. The previous S1–S5 are all represented above, and their outstanding tests/limits remain open.

## Saved build and validation

- Last local snapshot commit: `4da1d6a` (before this notes merge). All new stages above are unticked; planning approval is not implementation acceptance.
- Package `com.aistudio.bigdoglauncher.b2tv`, versionCode **1**, versionName **1.0**. TV last verified at `192.168.3.19:5555`, Xiaomi MiTV_ASTP0, Android 9/API 28. Recheck before future work.
- Last installed signed build: `%TEMP%/bigdog-apk-verify/BigDog-VLC-cold-start-signed.apk`, lastUpdateTime **2026-11-11 04:24:01**, firstInstallTime preserved at 2026-11-10 22:26:47. Installed/deployment SHA-256 match: `8d3163c98f82ca3f86f179c3ad3746b51061f96f8b807e6582f4311f4a4403af`.
- Existing compatible certificate verified; installed with `adb install -r`, preverified and launched. Home interceptor bound. Soundbar source was selected by Kev, not remotely changed.
- Last APK build passed (38 tasks, 4 executed); preceding video/pause build passed all **10 regression tests** for inputs, Hero history, Home reset and VLC state. Latest measured cold launch 4147ms is not a proven responsiveness improvement.
- This update changes documentation only. The supplied source media are unchanged; no new app build, APK installation, TV setting change, commit or push was performed. Next work is S1, followed by its on-TV acceptance checkpoint.
