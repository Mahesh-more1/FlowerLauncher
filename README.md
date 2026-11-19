# FlowerLauncher 🌸

An independent, clean-room, privacy-first Android home launcher inspired by Smart Launcher 6.

Built with **100% Kotlin + Jetpack Compose (Material 3)**. Zero trackers, zero ads, zero proprietary analytics, and 100% offline-first.

---

## Features

* **Signature Flower Layout:** Circular arc arrangement for primary applications with fluid sub-grid positioning.
* **Fluid Sub-Grid Mode:** Freeform customizable grid with arbitrary rows and columns.
* **Categorized App Drawer:**
  * 6 smart automatic categories (*Communication, Internet, Games, Media, Utilities, Settings*).
  * **Pro Feature Parity:** Create unlimited custom categories, reorder tabs, and move apps.
* **Smart Widgets Suite:**
  * Live typography clock, calendar, and battery readout.
  * Swipeable widget stacks (`HorizontalPager`) with interactive page indicators.
  * Quick sticky note memo.
* **Universal Search Panel:**
  * Fast on-device search across apps, shortcuts, and web queries (DuckDuckGo, Google).
  * Built-in offline mathematical expression calculator.
* **Privacy & Security Vault:**
  * Conceal sensitive applications from drawer and search with one tap.
  * Pure `#000000` AMOLED Black mode for OLED power efficiency.
* **Portability:**
  * Full JSON layout export and import to clipboard or storage.
* **Real-Time Notification Badges:**
  * Integrated notification listener service rendering live unread counters.

---

## Technical Stack

* **Language:** Kotlin 2.1.0
* **UI Toolkit:** Jetpack Compose + Material 3
* **Min SDK:** Android 10 (API 29)
* **Target SDK:** Android 16 (API 36)
* **Architecture:** Clean Architecture + MVVM + UDF (Unidirectional Data Flow)
* **CI/CD:** Automated GitHub Actions build workflow (`.github/workflows/build.yml`)

---

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```
