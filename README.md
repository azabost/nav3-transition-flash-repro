# Nav3 Transition Flash Reproduction

Minimal Android launcher app demonstrating a bug in **Navigation3** (`1.2.0-alpha05`): `NavDisplay` renders one stale frame of the previous screen when all transitions are set to `EnterTransition.None` / `ExitTransition.None`.

## The bug

https://github.com/user-attachments/assets/46547393-9d7c-4815-bd66-1b10088ea7bb

<img width="10%" alt="frame_0001" src="https://github.com/user-attachments/assets/27a1d466-8879-4478-9bfb-88e1f5136c12" />
<img width="10%" alt="frame_0006" src="https://github.com/user-attachments/assets/fc920093-d903-47df-bb9a-61c37d786fd4" />
<img width="10%" alt="frame_0007" src="https://github.com/user-attachments/assets/10b9cde7-3e42-4b0c-ab59-072ad3292bfb" />
<img width="10%" alt="frame_0016" src="https://github.com/user-attachments/assets/415b42fb-0205-4d79-8ada-c7e7b4f0a0a9" />
<img width="10%" alt="frame_0024" src="https://github.com/user-attachments/assets/85c21a36-1d8d-42e9-ae71-d195c803a362" />
<img width="10%" alt="frame_0028" src="https://github.com/user-attachments/assets/a12a40cb-a07f-45f6-b9de-bdeb708b70b1" />

_You can see individual frames of the video above in `docs/frames` directory._

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

See [ISSUE.md](docs/ISSUE.md) for the complete bug report prepared for Google Issue Tracker.
