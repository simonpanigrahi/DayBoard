# DayBoard

Offline, always-on routine board for an Android tablet. Full design in docs/ARCHITECTURE.md — read it before any structural change.

## Hard invariants (never violate, never "improve")
1. The `:engine` module has ZERO `android.*` and `androidx.*` imports. Pure Kotlin/JVM only.
2. `AndroidManifest.xml` must NEVER contain INTERNET or ACCESS_NETWORK_STATE.
3. The event log is append-only. `EventRepository` exposes no update or delete.
4. ALL durations come from `SystemClock.elapsedRealtime()`. ALL scheduling comes from wall clock. Never mix.
5. Do not edit gradle/libs.versions.toml, build.gradle.kts version pins, or the Gradle wrapper without asking me first. The version matrix is known-good.

## Conventions
- Kotlin, Compose Material 3, Room, coroutines/Flow. Manual DI via AppContainer — do NOT add Hilt.
- No new third-party dependencies without asking. No Retrofit, no chart library, no image loader.
- Engine logic is test-first: write the failing test, then the implementation.
- Every `resolve()`/`layout()`/`foldEvents()` change requires `./gradlew :engine:test` to pass before you say you're done.

## Commands
- `./gradlew :engine:test` — fast, no emulator
- `./gradlew :app:assembleDebug`
- `./gradlew :app:installDebug` — installs to connected tablet
- `adb logcat -s DayBoard:V`

## Style
- Explain WHY in comments only where the reasoning is non-obvious (clock handling, fold edge cases). No comments restating what the code does.
- Small files. If a composable exceeds ~120 lines, split it.
