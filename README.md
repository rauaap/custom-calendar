# Custom Calendar

An Android home screen widget that lists your upcoming calendar events, where
you decide what each line looks like via a `date(1)`-style format string.

Colors, font size, and the event format are configured per widget instance, so
two widgets on the same home screen can look completely different. There is no
launcher activity worth opening — the app's only screen just explains how to
place the widget.

Requires Android 8.0 (API 26) or newer and calendar read access.

## Adding the widget

1. Long-press an empty spot on the home screen, choose **Widgets**, find
   **Custom Calendar** and place it.
2. The settings screen opens automatically and asks for calendar access.
   **Save** stays disabled until access is granted; if you deny it, the widget
   shows "Tap to grant calendar access" and tapping it reopens the settings.
3. To change settings later, long-press the widget and tap the edit (pencil)
   icon.

The widget can be resized freely in both directions.

## Customization options

Everything is live-previewed at the top of the settings screen using a sample
event, so you can see the result before saving.

| Option | Effect |
|--------|--------|
| **Event name color** | Color of the `%E` (event name) part of each line. |
| Event name → **Use calendar color** | Ignores the chosen color and uses each event's own calendar color instead, per row. Disables the swatch. |
| **Date & text color** | Color of everything that is *not* `%E`: date output, literal text, punctuation. |
| Date & text → **Use calendar color** | Same as above, for the non-name part of the line. |
| **Widget background** | Fill behind the event list. Alpha is supported, so a translucent or fully transparent background works. |
| **Show "No upcoming events" when empty** | Off leaves the widget blank when there is nothing to show. The "Tap to grant calendar access" message is unaffected — it appears either way. |
| **Font size** | 10–28 sp. Also scales the vertical padding between rows, so larger text stays readable rather than cramped. |
| **Event format** | The per-event format string — see below. |

Tapping any color swatch opens an HSV picker with a saturation/value square, a
hue slider, an alpha slider with an opacity percentage, and a hex field that
stays in sync with the sliders (`RRGGBB`, or `RRGGBBAA` to include opacity).

Settings are stored per widget id and are deleted when that widget is removed
from the home screen.

## Event format

The format string is rendered once per event. Specifiers follow Linux
`date(1)`/`strftime`, with `%E` added for the event name:

| Specifier | Expands to |
|------|-------------------------------|
| `%E` | event name |
| `%Y` | year, 4 digits (2026) |
| `%y` | year, 2 digits (26) |
| `%m` | month, 2 digits (07) |
| `%B` | month name (July) |
| `%b` | month name, short (Jul) |
| `%d` | day of month, 2 digits (26) |
| `%e` | day of month, space-padded ( 5) |
| `%A` | weekday name (Sunday) |
| `%a` | weekday name, short (Sun) |
| `%j` | day of year, 3 digits (207) |
| `%H` | hour, 24h, 2 digits (14) |
| `%I` | hour, 12h, 2 digits (02) |
| `%M` | minute, 2 digits (05) |
| `%p` | AM or PM |
| `%%` | a literal `%` character |
| `\n` | line break |
| `\\` | a literal backslash |

Month and weekday names follow the device locale. All other characters are kept
as-is, so you can mix in your own words and punctuation. An unrecognized escape
or specifier is also left as typed (`%q` stays `%q`, `\q` stays `\q`).

The default format is:

```
%E — %A %d %B, %H:%M
```

Two-line rows work well when event names are long:

```
%a %d.%m - %H:%M\n%E
```

Each row is capped at the number of lines the format produces, and anything
longer is ellipsized — so a very long event name cannot make one row taller
than the rest.

## What the widget shows

- Events starting within the next 14 days, across all calendars on the device,
  earliest first, capped at 20 rows.
- All-day events are rendered in UTC so their date is not shifted by the local
  time zone.
- Tapping a row opens that event in the calendar app; tapping the widget
  background opens the calendar at today.
- With no upcoming events, the widget shows "No upcoming events", unless that
  message is switched off in the settings.
- The list refreshes whenever the calendar provider changes (via a
  content-trigger job), rather than waiting out the ~30 minute floor Android
  imposes on widget update intervals.

## Build

Fully containerized, CLI-driven Gradle build. No JDK, Android SDK, or Gradle
needed on the host — everything runs inside a podman container defined by the
`Containerfile`. The only host dependency is `podman` (and `adb`, if you want to
install on a device).

```
Containerfile          Fedora + JDK 21 + Android SDK + Gradle toolchain
Makefile               build / shell / install targets (podman wrapper)
build.gradle           root project — pins the Android Gradle Plugin version
settings.gradle        project name + module list
app/src/main/java/com/aapr/customcalendar/
  widget/              widget provider, list factory, settings screen, color picker
  format/              format string parser and Spannable renderer
```

Build the toolchain image once:

```sh
make image
```

Build a debug APK (rebuilds the image if needed):

```sh
make debug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Other targets:

```sh
make release            # assembleRelease
make clean              # gradle clean
make gradle ARGS="tasks"   # run any gradle task in the container
make shell              # interactive shell inside the build container
```

The Gradle cache is persisted in a named volume
(`custom-calendar-gradle-cache`) so incremental builds and the debug keystore
survive between runs.

## Install on a device

The build stays containerized; only `adb` runs on the host:

```sh
sudo dnf install android-tools     # Fedora
make install                       # adb install -r the debug APK
```

## Notes

- **Container-only by design.** There is no Gradle wrapper (`gradlew`); the
  pinned Gradle version lives solely in the `Containerfile`. Build through
  `make`, not on the host.
- SDK level, build-tools, and Gradle versions are all `ARG`s at the top of the
  `Containerfile` — change them in one place.
