# GitHub Releases - APK Distribution Guide

This guide explains how to distribute your APK to Sumviltad farmers using GitHub Releases (100% FREE!)

---

## ✅ What Happens Automatically

When you push a version tag (like `v1.0.0`), GitHub Actions will:

1. ✅ Build the APK automatically
2. ✅ Upload to Firebase App Distribution (for your team testing)
3. ✅ **Create GitHub Release with downloadable APK** (for farmers!)
4. ✅ Generate professional release notes

---

## 📥 Download Links

### Latest Release (Always)

```
https://github.com/Vincentjhon31/SUMVILTAD/releases/latest
```

### Direct APK Download (Always Latest)

```
https://github.com/Vincentjhon31/SUMVILTAD/releases/latest/download/app-release.apk
```

### Specific Version (Example: v1.0.0)

```
https://github.com/Vincentjhon31/SUMVILTAD/releases/download/v1.0.0/app-release.apk
```

---

## 🌐 Add to Your Website

### Option 1: Button Link (Recommended)

```html
<!-- Download button that always points to latest version -->
<a
  href="https://github.com/Vincentjhon31/SUMVILTAD/releases/latest/download/app-release.apk"
  class="download-btn"
>
  📥 Download SUMVILTAD Connect
</a>
```

### Option 2: Redirect Page

```html
<!DOCTYPE html>
<html>
  <head>
    <title>Downloading SUMVILTAD Connect...</title>
    <meta
      http-equiv="refresh"
      content="0; url=https://github.com/Vincentjhon31/SUMVILTAD/releases/latest/download/app-release.apk"
    />
  </head>
  <body>
    <p>Downloading SUMVILTAD Connect APK...</p>
    <p>
      If download doesn't start,
      <a
        href="https://github.com/Vincentjhon31/SUMVILTAD/releases/latest/download/app-release.apk"
        >click here</a
      >.
    </p>
  </body>
</html>
```

### Option 3: QR Code

1. Go to: https://www.qr-code-generator.com/
2. Paste: `https://github.com/Vincentjhon31/SUMVILTAD/releases/latest/download/app-release.apk`
3. Download QR code image
4. Print on posters/flyers for farmers to scan

---

## 📱 Installation Instructions for Farmers

### Step 1: Download

Tell farmers to visit:

```
https://github.com/Vincentjhon31/SUMVILTAD/releases/latest
```

Or scan the QR code you created.

### Step 2: Enable Unknown Sources

1. Go to **Settings** → **Security**
2. Enable **"Install from unknown sources"** or **"Install unknown apps"**
3. Allow installation from Chrome/Browser

### Step 3: Install

1. Open the downloaded `app-release.apk` file
2. Tap **"Install"**
3. Wait for installation to complete
4. Tap **"Open"** to launch SUMVILTAD Connect

---

## 🔄 Releasing New Versions

### For Bug Fixes (v1.0.0 → v1.0.1)

```powershell
# Update version in app/build.gradle.kts first:
# versionCode = 2
# versionName = "1.0.1"

git add app/build.gradle.kts
git commit -m "chore: bump version to 1.0.1"
git push origin main

# Create release tag
git tag -a v1.0.1 -m "Version 1.0.1

Bug Fixes:
- Fixed camera crash on Android 13
- Fixed notification timing issue
- Improved offline mode stability"

git push origin v1.0.1
```

### For New Features (v1.0.0 → v1.1.0)

```powershell
# Update version in app/build.gradle.kts:
# versionCode = 3
# versionName = "1.1.0"

git tag -a v1.1.0 -m "Version 1.1.0

New Features:
- Added fertilizer recommendation system
- Real-time weather alerts
- Community chat feature

Improvements:
- Faster disease detection
- Better offline support"

git push origin v1.1.0
```

---

## 🎯 Best Practices

### 1. Announce Updates

When releasing new version, announce on:

- Your website
- Facebook page
- SMS to registered farmers
- Community meetings

### 2. Version Naming

Follow Semantic Versioning:

- **v1.0.0** → **v1.0.1**: Bug fixes only
- **v1.0.0** → **v1.1.0**: New features (backwards compatible)
- **v1.0.0** → **v2.0.0**: Major changes (breaking changes)

### 3. Release Notes

Always include:

- ✅ What's new
- ✅ Bug fixes
- ✅ Known issues (if any)
- ✅ Installation instructions

### 4. Test Before Release

1. Create tag locally first: `git tag -a v1.0.1-beta -m "Beta test"`
2. Push: `git push origin v1.0.1-beta`
3. Test with your team
4. If OK, create final: `git tag -a v1.0.1 -m "Release"`

---

## 📊 Tracking Downloads

GitHub doesn't provide download statistics by default, but you can:

### Option 1: Use GitHub API

Check release download count:

```
https://api.github.com/repos/Vincentjhon31/SUMVILTAD/releases/latest
```

Look for `download_count` in the response.

### Option 2: Add Analytics to Website

Use Google Analytics on your website download page to track clicks.

### Option 3: Use URL Shortener

Create a short link with analytics:

- bit.ly
- tinyurl.com
- Firebase Dynamic Links (free)

---

## 🆚 Comparison: GitHub vs Firebase

| Feature           | GitHub Releases        | Firebase Distribution        |
| ----------------- | ---------------------- | ---------------------------- |
| **Cost**          | ✅ FREE                | ✅ FREE                      |
| **File Size**     | ✅ No limit            | ✅ No limit                  |
| **Public Access** | ✅ Anyone can download | ❌ Email invite only         |
| **Best For**      | ✅ **Farmers**         | ✅ **Your team testing**     |
| **Installation**  | Simple download        | Requires Firebase App Tester |
| **Updates**       | Manual (farmers check) | Automatic notifications      |

**Solution**: Use BOTH! ✨

- **Firebase**: For your team and beta testers (5-20 people)
- **GitHub Releases**: For all farmers (hundreds/thousands)

---

## 🌾 Marketing Materials

### QR Code Poster

```
╔═══════════════════════════════════════╗
║   SUMVILTAD Connect Mobile App       ║
║   Rice Disease Detection System      ║
║                                       ║
║         [QR CODE HERE]                ║
║                                       ║
║   📥 Scan to Download                 ║
║   FREE for Sumviltad Farmers         ║
║                                       ║
║   Requirements:                       ║
║   • Android 7.0+                      ║
║   • 500MB storage                     ║
║   • Internet connection               ║
╚═══════════════════════════════════════╝
```

### SMS Template

```
Good day! Download SUMVILTAD Connect app for FREE rice disease detection.
Visit: https://bit.ly/sumviltad-app
Android 7.0+ required. Need help? Call [number]
```

### Facebook Post Template

```
🌾 SUMVILTAD CONNECT - NOW AVAILABLE! 🌾

FREE Mobile App para sa mga Sumviltad Farmers!

✅ AI-powered Rice Disease Detection
✅ Weather Monitoring
✅ Farm Management Tools
✅ Push Notifications for Alerts

📥 DOWNLOAD NOW:
[Link to GitHub Release]

📱 Requirements:
• Android 7.0 or higher
• 500MB free storage
• Internet connection

📞 Need help installing? Contact: [Your number]

#SumviltadConnect #RiceDisease #SmartFarming
```

---

## 🔧 Troubleshooting

### "Parse Error" During Installation

**Cause**: Corrupted download
**Solution**: Re-download the APK

### "App Not Installed"

**Cause**: Conflicting app signature
**Solution**: Uninstall old version first, then install new one

### "Installation Blocked"

**Cause**: Unknown sources not enabled
**Solution**: Go to Settings → Security → Enable "Install unknown apps"

### Download is Slow

**Cause**: Large file (205MB)
**Solution**: Use WiFi instead of mobile data

---

## 📞 Support

For farmers who need help:

1. Create a support hotline/number
2. Add support contact to release notes
3. Create video tutorial (Tagalog/Local language)
4. Train community leaders to help with installation

---

## 🎉 You're All Set!

Your APK is now automatically distributed to:

- ✅ **GitHub Releases** → For all farmers (public)
- ✅ **Firebase App Distribution** → For your team (private testing)

Every time you push a tag like `v1.0.1`, both systems update automatically!

---

**Setup Date**: October 30, 2025
**Repository**: https://github.com/Vincentjhon31/SUMVILTAD
**Latest Release**: https://github.com/Vincentjhon31/SUMVILTAD/releases/latest
