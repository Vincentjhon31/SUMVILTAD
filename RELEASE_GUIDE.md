# Quick Guide: Creating a New Release

Follow these steps to release a new version of SumviltadConnect:

## 1. Update Version Numbers

Edit `app/build.gradle.kts`:

```kotlin
versionCode = 2           // Increment by 1 (was 1)
versionName = "1.0.1"     // Update version (was "1.0.0")
```

**Version Naming Rules**:

- **MAJOR** (1.x.x): Breaking changes, major new features
- **MINOR** (x.1.x): New features, backward compatible
- **PATCH** (x.x.1): Bug fixes, minor improvements

## 2. Build Release APK

### Option A: Android Studio

1. Build → Generate Signed Bundle / APK
2. Select APK
3. Choose release variant
4. Wait for build to complete
5. Find APK at: `app/build/outputs/apk/release/app-release.apk`

### Option B: Command Line (PowerShell)

```powershell
# Navigate to project root
cd C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect

# Build release APK
.\gradlew assembleRelease

# APK location
# app\build\outputs\apk\release\app-release.apk
```

## 3. Test the APK (Important!)

Before releasing, test the APK:

```powershell
# Install on connected device
adb install app\build\outputs\apk\release\app-release.apk

# Or uninstall old version first
adb uninstall com.zynt.sumviltadconnect
adb install app\build\outputs\apk\release\app-release.apk
```

**Test Checklist**:

- [ ] App launches successfully
- [ ] Login/registration works
- [ ] Disease detection works
- [ ] Camera functionality works
- [ ] Notifications work
- [ ] No crashes or errors

## 4. Create Git Tag

```powershell
# Commit all changes first
git add .
git commit -m "Release v1.0.1: Update checker feature"

# Create tag (must match versionName with 'v' prefix)
git tag v1.0.1

# Push to GitHub
git push origin main
git push origin v1.0.1
```

## 5. Create GitHub Release

### Via Web Interface (Recommended)

1. Go to: https://github.com/Vincentjhon31/SUMVILTAD/releases/new

2. Fill in release details:

   - **Tag**: Select `v1.0.1` (the tag you just pushed)
   - **Release title**: `SumviltadConnect v1.0.1`
   - **Description**: Write release notes (see template below)

3. Upload APK:

   - Click "Attach binaries by dropping them here or selecting them"
   - Upload: `app-release.apk`
   - Rename to: `SumviltadConnect-v1.0.1.apk` (optional but recommended)

4. Click "Publish release"

### Release Notes Template

```markdown
## 🎉 What's New in v1.0.1

### ✨ New Features

- Added automatic update checker
- Users can now check for new versions from Profile screen
- Visual notification banner when updates are available

### 🐛 Bug Fixes

- Fixed About dialog not showing content
- Resolved Help & Support section display issues
- Improved notification handling

### 🔧 Improvements

- Enhanced profile screen UI
- Better error messages
- Performance optimizations

### 📱 Download

Download the APK file below and install on your device.

**Requirements**:

- Android 7.0 (API 24) or higher
- ~50 MB storage space

**Installation**:

1. Download the APK file
2. Enable "Install from unknown sources" in your device settings
3. Open the downloaded file and follow installation prompts

### 🔗 Links

- [Full Changelog](https://github.com/Vincentjhon31/SUMVILTAD/compare/v1.0.0...v1.0.1)
- [Report Issues](https://github.com/Vincentjhon31/SUMVILTAD/issues)
- [Documentation](https://github.com/Vincentjhon31/SUMVILTAD/blob/main/README.md)
```

## 6. Verify Update Checker

After publishing the release:

1. Install **old version** (v1.0.0) on a test device
2. Open app and go to Profile
3. Tap "Check for Updates"
4. Should show "Update Available" dialog with v1.0.1
5. Verify download button opens GitHub release page
6. Test APK download and installation

## 7. Announce Release

Optional but recommended:

1. **Email to testers**: Notify beta testers
2. **Social media**: Post update announcement
3. **Documentation**: Update README.md with new version
4. **Commit changes**: Commit version bump and changelog

```powershell
git add .
git commit -m "Bump version to 1.0.1"
git push origin main
```

## Common Issues

### Issue: Tag already exists

```powershell
# Delete local tag
git tag -d v1.0.1

# Delete remote tag
git push --delete origin v1.0.1

# Create new tag
git tag v1.0.1
git push origin v1.0.1
```

### Issue: APK too large

- Check if unnecessary resources are included
- Enable ProGuard/R8 minification
- Use APK Analyzer to identify large files
- Consider using App Bundles (AAB) for Play Store

### Issue: Update checker shows "Up to date" for new release

- Verify tag format: must be `v1.0.1` (with 'v' prefix)
- Check release is not marked as "draft"
- Ensure version in tag matches `versionName` in build.gradle
- Wait a few minutes for GitHub API to update

## Quick Reference

### Version History Template

Keep track of releases in `CHANGELOG.md`:

```markdown
# Changelog

## [1.0.1] - 2025-11-11

### Added

- Update checker feature
- About and Help dialogs

### Fixed

- Profile screen menu items

## [1.0.0] - 2025-11-10

### Added

- Initial release
- Disease detection
- User authentication
- Notification system
```

---

**Need Help?**

- Check `UPDATE_CHECKER_FEATURE.md` for detailed documentation
- Open an issue on GitHub: https://github.com/Vincentjhon31/SUMVILTAD/issues
