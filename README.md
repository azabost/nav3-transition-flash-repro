# Nav3 Transition Flash Reproduction

Minimal Android launcher app demonstrating a bug in **Navigation3** (`1.2.0-alpha05`): `NavDisplay` renders one stale frame of the previous screen when all transitions are set to `EnterTransition.None` / `ExitTransition.None`.

## The bug

<video src="docs/nav_flash_repro.mp4" width="240" controls></video>

After pressing the Home button, **AppList flashes for one frame** before Home appears. The on-screen `backStack=` text proves the inconsistency — AppList is rendered with `backStack=[Home]`, meaning it should not be visible.

## How it works

The app has two screens:

- **Home** — a button to open the app list
- **AppList** — a scrollable list of installed apps (via `PackageManager`)

Each screen displays the current back-stack state as text and logs every draw via `Modifier.drawBehind`.

When the Home button is pressed, the back-stack is cleared to `[Home]` via a `Channel`-based Flow.

## Reproduction steps

1. Install and set as default launcher
2. Tap **Apps** to navigate to AppList
3. Tap any app (e.g. Calculator) to open it
4. Press the hardware **Home** button
5. Observe: AppList briefly flashes before Home appears

## Logcat evidence

```
adb logcat -s NavFlashBug:W
```

```
07-02 22:20:21.696 W NavFlashBug: Home button pressed
07-02 22:20:21.704 W NavFlashBug: Clearing backStack to [Home]
07-02 22:20:21.721 W NavFlashBug: Compose: backStack=[Home]
07-02 22:20:21.788 W NavFlashBug: AppList draw (backStack=[Home])   <-- BUG: AppList draws after removal
07-02 22:20:22.001 W NavFlashBug: Home draw (backStack=[Home])
```

## Full issue description

See [ISSUE.md](ISSUE.md) for the complete bug report prepared for Google Issue Tracker.
