<!-- ---------- Header ---------- -->
<div align="center">
  <img src="app/src/main/res/drawable/circletosearch.png" width="200" height="200">
  <h1>Lens</h1>
  <h3>Lens: an open alternative to Google Lens</h3>
  <p>🔒 <em>Google • Bing • Yandex • Tineye</em></p>
</div>

> 🍴 **This is a fork of [AKS-Labs/CircleToSearch](https://github.com/AKS-Labs/CircleToSearch)**
> All credit for the original app, design, and core functionality goes to [AKS-Labs](https://github.com/akslabs). This fork adds a few extra features for personal use:
> - 📷 **Camera QR Code Scanning**
> - 💱 **Currency Converter**
>
> The original donate UI was removed from the in-app interface for this fork — if you'd like to support the original developers, see the [Community](#-community) section below.

<!-- ---------- Badges ---------- -->
<div align="center">
  <img alt="Last commit" src="https://img.shields.io/github/last-commit/haiderakt/CircleToSearch?color=c3e7ff&style=flat-square">
  <img alt="Repo size" src="https://img.shields.io/github/repo-size/haiderakt/CircleToSearch?color=c3e7ff&style=flat-square">
</div>

<!-- ----------   Labels ---------- -->
<div align="center">
  <img alt="API" src="https://img.shields.io/badge/Api%2029+-50f270?logo=android&logoColor=black&style=for-the-badge"/>
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-a503fc?logo=kotlin&logoColor=white&style=for-the-badge"/>
  <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label="/>
  <img alt="material" src="https://custom-icon-badges.demolab.com/badge/material%20you-lightblue?style=for-the-badge&logocolor=333&logo=material-you"/>
</div>

---
##  What is Lens?

Ever wanted to search for something you see on your phone screen? **Lens** brings that power to your fingertips on *any* Android device. Simply draw a circle around what you're looking for, and instantly get results from your favorite search engine.

Think of it as having Google Lens, Bing Visual Search, Yandex, and TinEye all in one place—and it works everywhere.

---
## Why Use This Circle To Search App?

**The Problem with Google's CTS Version**
- Forced Cloud Syncing: Uploads your selection to servers even when you just want to copy text.
- Get's accidentally triggerd frequently, exposing sensitive data instantly to google
- Ecosystem Lock-in: Restricted to Google Search—no support for Bing, Yandex, or AI models.
- Locked to Google ecosystem—no choice of search engine
- Missing Features: Useful features like "Share" and "Save" have been stripped out.
- Hardware Exclusivity: Only available on expensive flagship devices (Pixel 8, Galaxy S24+).

**What We Do Differently**
- Only what you circle gets processed—nothing else
- True Offline OCR: Text recognition works 100% locally on your device—no internet needed.
- QR detection offline, Smart Scan offline—no unnecessary servers
- Universal Compatibility: Works with any search engine (Google, Bing, Yandex, TinEye, ChatGPT).
- Restored Utility: We brought back the "Share" and "Save" features Google removed.
- Works on any Android device, not just expensive flagships
- Works on De-Googled Devices no google programs needed.
- And has many other useful features
- **100% Independent**: Works flawlessly on **any Android phone** (Android 10+), without requiring Google Play Services or OEM-specific software.
- **Privacy-First**: No background tracking or logs—just pure on-device selection.

## ✨ Core Features

### 🔍 Smart Search & AI
- **Multi-Engine Support**: Instant reverse image search using Google Lens, Bing, Yandex, TinEye, **ChatGPT**, and **Perplexity**.
- **Smart Native "Copy Text"**: Extract and copy text from any screen using **Offline OCR (Tesseract)** or **Hybrid Assist Mode**.
- **Customizable OCR Models**: Import external high-accuracy Tesseract language models for specialized detection.
- **Smart Entity Extraction**: Intelligent on-device scanning to seamlessly detect and manage smart content (like barcodes or key info).

### 📱 Seamless OS Integration
- **Universal Trigger**: Double-tap the status bar or configure the floating bubble to trigger instantly over any app.
- **Set As Default Assistant**: Configure as your system's default Android Assistant to trigger directly via home button long-press or diagonal swipe.
- **Smart Selection**: Draw a circle or scribble on the screen to seamlessly crop and select what you want to search.

### 🎨 Beautiful & Customizable UI
- **Modern Design**: Built from the ground up with elegant Material 3 components, smooth animations, and a highly polished interface.
- **Playful Interactions**: Shows friendly, playful, and humorous messages whenever you use the app.
- **Customizable Aesthetics**: Toggle UI elements, gradient borders, Dark Mode, and Desktop Mode for rendering search results.

### 🌐 Quick Actions
- **Fast Sharing**: Instantly copy URLs of search results.
- **Open In Browser**: Bounce directly from the internal in-app viewer to your favorite external browser in one tap.

### 🆕 Fork Additions
- **Camera QR Code Scanning**: Scan QR codes directly using your device camera.
- **Currency Converter**: Built-in on-the-fly currency conversion tool.

---

## 📥 **Get Circle To Search**

### 📦 Download APK Releases
You can download the latest pre-compiled APK packages directly from **[GitHub Releases](https://github.com/haiderakt/Lens/releases)**.

### 🛠️ Build from Source
Alternatively, you can build the application locally:

```bash
git clone https://github.com/haiderakt/Lens.git
cd Lens
./gradlew assembleRelease
```

If you're looking for the original, officially published app, get it here instead:

<div align="center">
    <p align="center">
  <a href="https://github.com/AKS-Labs/CircleToSearch/releases">
    <img alt="Get it on GitHub" src="https://user-images.githubusercontent.com/69304392/148696068-0cfea65d-b18f-4685-82b5-329a330b1c0d.png" height="80px">
  </a>
  <a href="https://f-droid.org/packages/com.akslabs.circletosearch/">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
         alt="Get it on F-Droid"
         height="80">
  </a>
  <a href="https://play.google.com/store/apps/details?id=com.akslabs.circletosearch">
    <img alt='Get it on Google Play'
         src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'
         height="80" />
  </a>
  <br>
  <p><strong>• 🔓 Open Source    • 🚀 Ready to Use</strong></p>
</div>

---

## 📱 **Screenshots**

<table align="center">
  <tr>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/></td>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/></td>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/></td>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200"/></td>
  </tr>
  <tr>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="200"/></td>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="200"/></td>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" width="200"/></td>
    <td><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.png" width="200"/></td>
  </tr>
</table>

---

## 🤝 **Community**

**For this fork:**
- 🐛 **Bug reports / feature requests:** [Open an issue on this fork](https://github.com/haiderakt/Lens/issues)

**For the original project (AKS-Labs):**
- 👨‍💻 **Contribute to upstream:** [AKS-Labs/CircleToSearch](https://github.com/AKS-Labs/CircleToSearch)
- ⭐ Star the [original repo](https://github.com/AKS-Labs/CircleToSearch)
- 💬 Join the [AKS-Labs Telegram group](https://t.me/AKSLabs)
- ☕ [**Donate to the original developers**](https://github.com/sponsors/AKS-Labs) — all core app credit belongs to them.

---

<p align="center">
  Original app made with ❤️ by <a href="https://github.com/akslabs">AKSLabs</a><br>
  Fork maintained by <a href="https://github.com/haiderakt">Haider Ali</a>
</p>
