### Component used

Navigation3

### Version used

`1.2.0-alpha05`

### Devices/Android versions reproduced on

This bug is most impactful on E-Ink displays (such as Mudita Kompakt) where every extra frame causes a visible full-screen refresh artifact, but it seems to be device-independent (I was able to reproduce it on an emulator as well).

### Description

When `NavDisplay` is configured with `EnterTransition.None` and `ExitTransition.None` for all transition specs (`transitionSpec`, `popTransitionSpec`, `predictivePopTransitionSpec`), it still renders one frame of the previous screen before showing the new one after a back-stack change.

On a standard screen, a single stale frame is barely perceptible. On an E-Ink display, however, it causes a clearly visible flash: the display refreshes once for the stale frame (showing the old screen), then refreshes again for the correct screen.

### Sample project

https://github.com/azabost/nav3-transition-flash-repro

### Steps to reproduce

The attached sample project is a minimal launcher app with two screens:

- **Home** — a centered label and an "Apps" button
- **AppList** — a scrollable list of all installed apps (via `PackageManager`)

The app registers as a launcher (`HOME`/`DEFAULT` intent categories). When the hardware Home button is pressed, `onNewIntent` sends an event through a `Channel` to clear the back-stack until Home is visible.

Each screen logs its draw calls via `Modifier.drawBehind` and displays the current back-stack state as on-screen text.

**Reproduction flow:**

1. Set the sample app as the default launcher
2. Tap "Apps" to navigate to AppList
3. Tap any app (e.g. Calculator) to open it
4. Press the hardware Home button to return to the launcher
5. Observe: **AppList briefly flashes before Home appears**

### Expected behavior

When all transitions are set to `EnterTransition.None` / `ExitTransition.None`, the new screen should appear **immediately** after the back-stack change — with zero frames of the old screen rendered.

### Actual behavior

The **old screen** (AppList) is rendered before the new screen (Home) appears.

### Evidence

#### Logcat output

Filter: `adb logcat -s NavFlashBug:W`

```
07-02 22:20:21.696 W NavFlashBug: Home button pressed
07-02 22:20:21.704 W NavFlashBug: Clearing backStack to [Home]
07-02 22:20:21.721 W NavFlashBug: Compose: backStack=[Home]
07-02 22:20:21.788 W NavFlashBug: AppList draw (backStack=[Home])   -- BUG: AppList draws AFTER being removed
07-02 22:20:22.001 W NavFlashBug: Home draw (backStack=[Home])
```

The back-stack is cleared immediately. The composition already sees `[Home]`. Yet `AppList draw (backStack=[Home])` proves that the AppList screen renders one more frame despite no longer being in the back-stack.

#### Frame-by-frame analysis (screen recording)

The on-screen `backStack=` text visually confirms the inconsistency: the user sees "AppList" while the text says `[Home]`.

### Same issue on forward navigation

The stale frame also occurs when navigating forward (Home → AppList):

```
W NavFlashBug: Compose: backStack=[Home, AppList]
W NavFlashBug: Home draw (backStack=[Home, AppList])        -- Home draws even though AppList is on top
W NavFlashBug: AppList draw (backStack=[Home, AppList])     -- correct frame
```
