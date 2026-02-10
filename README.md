# HoverDict

> Offline hover translation plugin for JetBrains IDEs. No API keys. No network. Just hover and read.

## What it does

HoverDict translates words in your editor when you hover over them. It ships with a built-in English↔Chinese dictionary (~970 entries covering common programming and general vocabulary). Everything runs locally — no third-party API calls, no telemetry, no network requests.

## Key features

- **Instant lookup** — HashMap-backed dictionary with O(1) access. Translation text renders in the same frame as the popup.
- **Identifier splitting** — Automatically breaks `getUserName` → `get` + `user` + `name` and `get_user_name` → `get` + `user` + `name`, translating each segment.
- **Multi-monitor aware** — Popup position is calculated against the physical screen bounds where your cursor actually is, not the primary monitor.
- **Configurable** — Hover delay, font size, popup opacity, preferred language, shortcuts — all adjustable from the settings panel (`Ctrl+Shift+D`).
- **Sponsor & ad framework** — Built-in sponsor dialog with QR code slot, contact info section, and ad placement area. Shown on first launch (dismissable).

## Compatibility

| IDE | Supported |
|-----|-----------|
| IntelliJ IDEA | ✅ 2023.2+ |
| Rider | ✅ 2023.2+ |
| WebStorm | ✅ 2023.2+ |
| PyCharm | ✅ 2023.2+ |
| GoLand | ✅ 2023.2+ |
| CLion | ✅ 2023.2+ |
| All JetBrains IDEs | ✅ 2023.2+ |

## Build

```bash
./gradlew buildPlugin
```

Output: `build/distributions/HoverDict-1.0.0.zip`

Install via `Settings → Plugins → ⚙️ → Install Plugin from Disk...`

## Shortcuts

| Action | Default |
|--------|---------|
| Toggle translation | `Ctrl+Shift+T` |
| Open settings | `Ctrl+Shift+D` |
| Sponsor & About | `Tools → HoverDict → Sponsor & About` |

## Extending the dictionary

Edit `src/main/resources/dictionary/en_zh.dict`. Format: one entry per line, tab-separated:

```
word\ttranslation
```

Rebuild and reinstall.

## Replacing the sponsor QR code

Drop your image at `src/main/resources/images/sponsor_qr.png` (recommended: 400×400px PNG).

## Architecture

```
com.hoverdict
├── PluginStartupActivity       // Entry point: loads dict, registers listeners
├── actions/
│   ├── ToggleTranslationAction // Toggle on/off
│   ├── OpenSettingsAction      // Open settings dialog
│   └── OpenSponsorAction       // Open sponsor dialog
├── service/
│   ├── DictionaryService       // Singleton dict engine (ConcurrentHashMap)
│   └── HoverMouseListener      // Editor mouse event handler
├── settings/
│   └── HoverDictSettings       // Persistent state (XML-serialized)
└── ui/
    ├── TranslationPopup        // Floating popup (JWindow, multi-monitor safe)
    ├── SettingsDialog           // Config panel (DialogWrapper)
    └── SponsorDialog           // Sponsor/about/ad dialog
```

## License

MIT
