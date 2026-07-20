# Bubble Bottom

<p align="center">
  <b>A modern and customizable Android Bubble Bottom Navigation library with smooth animations.</b>
</p>

<p align="center">
  Create beautiful bottom navigation experiences with an elegant bubble animation effect, flexible customization options, and simple integration.
</p>

<p align="center">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-24-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-blue?logo=kotlin&logoColor=white"/></a>
</p>

---

## ✨ Features

* 🌊 Smooth bubble animation effects
* 🎨 Fully customizable appearance
* ⚡ Lightweight and optimized
* 📱 Built for modern Android applications
* 🔥 Easy XML integration
* 🧩 Supports multiple navigation items
* 🎯 Custom colors and icons
* 🔄 Animated item selection
* 🛠 Kotlin friendly
* 📦 Simple dependency setup

---

## 📦 Installation

### Gradle

Add the dependency:

```gradle
dependencies {
    implementation("io.github.selimdawa:bubble-bottom:latest-version")
}
```

Or using Version Catalog:

```toml
[versions]
bubbleBottom = "latest-version"

[libraries]
bubble-bottom = { module = "io.github.selimdawa:bubble-bottom", version.ref = "bubbleBottom" }
```

---

## 🚀 Usage

### XML

Add `BubbleBottom` to your layout:

```xml
<com.selimdawa.bubblebottom.BubbleBottom
    android:id="@+id/bubbleBottom"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

---

### Kotlin

Initialize the view:

```kotlin
val bubbleBottom = findViewById<BubbleBottom>(R.id.bubbleBottom)
```

Add navigation items:

```kotlin
bubbleBottom.addItem(
    icon = R.drawable.ic_home,
    title = "Home"
)

bubbleBottom.addItem(
    icon = R.drawable.ic_search,
    title = "Search"
)

bubbleBottom.addItem(
    icon = R.drawable.ic_profile,
    title = "Profile"
)
```

Listen for item selection:

```kotlin
bubbleBottom.setOnItemSelectedListener { position ->
    when(position) {
        0 -> {
            // Home
        }
        1 -> {
            // Search
        }
        2 -> {
            // Profile
        }
    }
}
```

---

## 🎨 Customization

You can customize the appearance to match your application design.

### Change selected color

```kotlin
bubbleBottom.selectedColor = Color.BLUE
```

### Change background color

```kotlin
bubbleBottom.backgroundColor = Color.WHITE
```

### Change animation mode

```kotlin
// 20 Options available (e.g., SLIDE, MORPH, BOUNCE, DROP, JUMP, FLIP, ZOOM, ROTATE, SHAKE, ...)
bubbleBottom.animationMode = AnimationMode.BOUNCE
```

### Change animation duration

```kotlin
bubbleBottom.animationDuration = 300L // Set to -1 for auto-calculate
```

### Back to Home Behavior

By default, pressing the system back button will navigate to the first tab (Home) before closing the app.

```kotlin
bubbleBottom.isBackToHomeEnabled = true // Default is true
bubbleBottom.homeId = 1 // Specify which ID is your Home tab
```

---

## 🛠 XML Attributes

Example:

```xml
<com.selimdawa.bubblebottom.BubbleBottomNavigation
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:mbn_animationMode="bounce"
    app:mbn_animationDuration="400"
    app:mbn_backToHomeEnabled="true"
    app:mbn_homeId="1"
    app:selectedColor="@color/purple"
    app:backgroundColor="@color/white"/>
```

---

## 🎭 Animation Modes (20 Total)

| Mode         | Description                                    |
|:-------------|:-----------------------------------------------|
| `slide`      | Classic smooth movement.                       |
| `morph`      | Smart jump with disappear/reappear effect.     |
| `bounce`     | Fun movement with overshoot.                   |
| `drop`       | Bubble falls from the top into place.          |
| `jump`       | High-arcing jump animation.                    |
| `flip`       | Icon performs a 360 flip.                      |
| `zoom`       | Icon pops out and settles.                     |
| `rotate`     | Icon spins once.                               |
| `shake`      | Attention-grabbing horizontal shake.           |
| `anticipate` | Winds up backwards before snapping forward.    |
| `elastic`    | Rubbery, over-oscillating movement.            |
| `fade`       | Cross-fades during transition.                 |
| `squash`     | Flattens while moving, expands at destination. |
| `wave`       | Curve wobbles like a wave.                     |
| `pulse`      | Continuous soft pulse on select.               |
| `fling`      | High velocity start with smooth stop.          |
| `spin_move`  | Icon spins while moving.                       |
| `glow`       | Soft pulse glow on the circle.                 |
| `jelly`      | Wobbly width transition.                       |
| `tilt`       | 3D-like tilt effect on the icon.               |

---

## 📱 Requirements

| Requirement | Version   |
|-------------|-----------|
| Android     | API 24+   |
| Kotlin      | Supported |
| Gradle      | 8+        |

---

## 🎯 Why BubbleBottom?

Traditional bottom navigation bars are functional but often lack personality.

BubbleBottom provides a modern animated navigation experience while keeping implementation simple and lightweight.

---

## 🤝 Contributing

Contributions are welcome!

If you find a bug or have an idea for improvement:

1. Fork this repository
2. Create your feature branch

```bash
git checkout -b feature/new-feature
```

3. Commit your changes

```bash
git commit -m "Add new feature"
```

4. Push to your branch

```bash
git push origin feature/new-feature
```

5. Open a Pull Request

---

## 🐛 Issues

If you find any issues, please open an issue on GitHub with:

* Android version
* Device information
* Error logs
* Steps to reproduce

---

## 📄 License

```
Copyright (c) 2026 Selim Dawa

Licensed under the Apache License, Version 2.0
```

See the [LICENSE](LICENSE) file for more information.

---

## ⭐ Support

If you like this library, consider giving it a ⭐ on GitHub.

Your support helps improve and maintain this project.
