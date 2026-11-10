# BigDog Launcher project rules

- Keep changes focused on the requested feature. Avoid unrelated edits and refactors.
- Make every interactive Android TV control usable with the remote. Keep focus visible, make the directional arrows move focus predictably, use OK to activate the focused control, and make Back return to the previous screen or dismiss the current overlay.
- Preserve the existing app package ID, GitHub Actions build workflow, and APK signing setup.
- Keep signing keys, keystores, and encoded key contents out of Git. Use the existing GitHub Actions secret for CI signing.
- After each change, tell me what to test on the TV, including the steps and expected result.
- Leave committing, pushing, and installing on the TV to me unless I explicitly ask you to do it.
- Follow `docs/STAGE-PLAN.md` for the launcher fixes. Work on one step at a time, stop for my TV test, and only tick it off or move on after I confirm it works or I am happy with the result.
