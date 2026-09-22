# DayBoard

An offline, always-on routine board for an Android tablet. It sits on a stand beside you
and shows the clock, the block you are in, how much of it is left, that block's checklist,
what is next, and an honest ledger of where the day actually went.

It is not a task manager and not a calendar. It is a dashboard for the current hour.

## It cannot phone home

The app does not declare `INTERNET`. Not "we choose not to call the network" — the OS will
not permit it. A unit test reads the **merged** manifest, so a permission pulled in by a
dependency fails the build too:

```kotlin
@Test
fun `app never requests internet`() {
    manifests.forEach { manifest ->
        val permissions = usesPermissions(manifest)
        assertFalse(permissions.contains("android.permission.INTERNET"))
        assertFalse(permissions.contains("android.permission.ACCESS_NETWORK_STATE"))
    }
}
```

```
./gradlew :app:testDebugUnitTest
```

The merged manifest is a declared input of that task, so editing the manifest re-runs the
test rather than leaving it up to date and silently unexecuted.

## Three ideas the rest follows from

**Plan and actual are separate data.** The plan is mutable state you edit. What happened is
an append-only event log — `EventRepository` has no update and no delete, and events carry no
foreign key to blocks, so re-committing a plan can never cascade into recorded history. Every
"how long did I really work" number is a pure fold over that log, which means a metric fixed
later gives correct history retroactively.

**The engine is pure Kotlin.** `resolve(plan, events, now, zone) -> BoardState` has no Android
imports, no coroutines and no database. `:engine` applies `kotlin("jvm")` rather than the
Android library plugin, so there is no `android.*` on its compile classpath to import even by
accident. The UI is a dumb renderer of `BoardState`.

**Untracked time is a residual, never a detection.** Time that is neither logged focus nor a
logged break is reported as untracked, in its own bucket, with no spin. The app does not know
what you were doing, so it does not claim to. `elapsed = focused + break + untracked` holds by
construction, and a property test asserts it over 500 generated event sequences.

### Two clocks, never mixed

Every duration comes from `SystemClock.elapsedRealtime()`. Every scheduling decision comes from
the wall clock. Both are stamped on every event. `elapsedRealtime` restarts at reboot, so a
per-boot UUID groups readings and the fold refuses to subtract across a boot boundary, falling
back to wall time and marking that block `Confidence.PARTIAL`. Mixing the two is the number one
bug in this category: a DST shift or a manual clock change turns a 25-minute session into a
3-hour-25-minute one, and the history is silently corrupt forever.

## Filling a day in one input

One box takes either format. Type the line grammar:

```
09:00-09:30 Email professors #deep
  - Mail Dr. Puhan
  - Reply to lab thread
09:30 45m Read the paper #research
45m Revise                  flows after the block above
= 15m buffer    ~ 10m break    ! 17:30 60m Gym
```

`HH:MM-HH:MM` or `HH:MM 45m` pins to the clock · a bare `45m` flows after the block above ·
`=` buffer · `~` break · `!` appointment · `#tag` colours it consistently · two spaces and `-`
adds a checklist item.

Or press **Copy AI prompt**, paste it into any assistant with your day in plain words, and paste
the JSON back into the same box. The parser strips markdown fences and surrounding chatter,
tolerates trailing commas, and accepts `"30m"` / `"1h 15 min"` / `30` for durations and
`"9am"` / `"09:00"` / `"9:00 AM"` for times. Unknown fields degrade with a warning rather than
failing the import.

Both paths produce the same `PlanDraft`, pass through one Review screen, and commit through one
function. Nothing else writes to the plan tables.

## Layout

Real routines mix two kinds of block, and most planners model only one.

| Kind | Behaviour |
| --- | --- |
| **FIXED** | Pinned to the wall clock. A class, a gym slot, a call |
| **FLOW** | Starts when the previous block ends |

A single sweep sorts by the order you edited, walks once with a cursor, pins anchors and chains
flow blocks behind them. When a run of flow blocks does not fit before the next anchor, four
policies decide: `SPILL` eats an adjacent buffer then compresses, `COMPRESS` shrinks
proportionally but never below `minMinutes`, `TRUNCATE` cuts at the anchor, and `PUSH` keeps
durations and reports the overlap as a conflict. Running late is a normal state, not an error:
the board stays startable when the clock has moved past the plan.

## Modules

```
engine/                     pure Kotlin/JVM, no Android
  model/                    Block, SessionEvent, BoardState, BlockActuals
  layout/                   the sweep and the four overflow policies
  fold/                     foldEvents -> BlockActuals, reboot and clock-change handling
  parse/                    DSL grammar, lenient JSON, shared time tokens
  Resolve.kt                resolve(plan, events, now, zone) -> BoardState
app/                        Android
  data/db, data/repo        Room mirrors, append-only event repository
  clock/                    boundary-aligned tick, per-boot id
  ui/board, ui/editor       the board, the editor, review, one-box import
```

## Build

```
./gradlew :engine:test          # 82 tests, no emulator, runs in seconds
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb logcat -s DayBoard:V
```

Kotlin 2.2.10, AGP 9.4.1, `minSdk 26` so `java.time` needs no desugaring. No Hilt, no network
stack, no chart library, no image loader.

## Status

Built: the engine, Room persistence, the board, the manual editor, the review step, and one-box
import for the typed grammar and pasted JSON.

Not built yet: alarms and notifications, the stats screen, ambient mode, calendar import, OCR
and voice. The engine computes a rest/overrun nudge that the board does not render yet, and a
break counts up rather than down because planned break length is not yet in the data model.

## Licence

Apache 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE). Bundles the
[Inter](https://github.com/rsms/inter) typeface under the SIL Open Font License 1.1
(`licenses/Inter-OFL.txt`).
