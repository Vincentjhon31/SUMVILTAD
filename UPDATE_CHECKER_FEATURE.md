# App Update Checker Feature

## Overview

The SumviltadConnect app now includes an automatic update checker that connects to GitHub releases to notify users when a new version is available.

## How It Works

### 1. Architecture

The update checker follows MVVM architecture with three main components:

- **Model Layer**: `GitHubRelease` data classes
- **Repository Layer**: `UpdateRepository` handles GitHub API communication
- **ViewModel Layer**: `UpdateViewModel` manages update state
- **UI Layer**: Profile screen displays update notifications

### 2. Update Check Flow

```
User clicks "Check for Updates"
         ↓
UpdateViewModel.checkForUpdates()
         ↓
UpdateRepository.checkForUpdates()
         ↓
GitHub API: GET /repos/Vincentjhon31/SUMVILTAD/releases/latest
         ↓
Compare versions (semantic versioning)
         ↓
Update UI state (UpdateAvailable / UpToDate / Error)
         ↓
Show dialog or banner if update available
```

### 3. Version Comparison

The app uses **semantic versioning** (MAJOR.MINOR.PATCH):

- Example: `1.0.0` → `1.0.1` (update available)
- Example: `1.0.0` → `1.0.0` (up to date)
- Example: `1.2.5` → `2.0.0` (major update available)

## Features

### ✨ Visual Update Indicators

1. **Update Banner** (Top of Profile)

   - Appears when update is available
   - Shows new version number
   - Clickable to view details

2. **Menu Item Status**

   - Shows current version by default
   - Displays "Checking..." when checking
   - Shows ✓ icon when up to date
   - Shows ! icon when update available
   - Shows loading spinner while checking

3. **Update Dialog**
   - Current vs Latest version comparison
   - Release notes ("What's New")
   - Download size
   - Direct download button
   - Dismiss option ("Later")

### 🔄 Update States

```kotlin
sealed class UpdateState {
    object Idle                              // No check performed
    object Checking                          // Checking for updates
    object UpToDate                          // App is current
    data class UpdateAvailable(release)     // Update found
    data class Error(message)                // Network/API error
}
```

## GitHub Integration

### Release Requirements

For the update checker to work, GitHub releases must follow this format:

1. **Tag Name**: Must use semantic versioning with 'v' prefix

   - ✅ Correct: `v1.0.1`, `v2.0.0`, `v1.5.3`
   - ❌ Incorrect: `1.0.1`, `release-1.0`, `version1`

2. **APK Asset**: Must attach APK file to release

   - File must end with `.apk` extension
   - Example: `SumviltadConnect-v1.0.1.apk`

3. **Release Type**:
   - Must NOT be marked as "draft"
   - Can be pre-release (will still be detected)

### Example GitHub Release Creation

```bash
# Step 1: Create a new tag
git tag v1.0.1
git push origin v1.0.1

# Step 2: Create release on GitHub
- Go to: https://github.com/Vincentjhon31/SUMVILTAD/releases/new
- Tag: v1.0.1
- Title: SumviltadConnect v1.0.1
- Description:
  ## What's New
  - Fixed About dialog display issue
  - Added update checker feature
  - Improved notification handling

  ## Bug Fixes
  - Fixed camera permission crash
  - Resolved detection history loading issue

# Step 3: Upload APK
- Drag and drop: app-release.apk
- Or: SumviltadConnect-v1.0.1.apk

# Step 4: Publish release
```

## Configuration

### BuildConfig Settings

Located in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
buildConfigField("int", "VERSION_CODE", "${versionCode}")
buildConfigField("String", "GITHUB_REPO", "\"Vincentjhon31/SUMVILTAD\"")
buildConfigField("String", "RELEASE_URL", "\"https://github.com/Vincentjhon31/SUMVILTAD/releases\"")
```

### Update Repository Configuration

Located in `UpdateRepository.kt`:

```kotlin
companion object {
    private const val GITHUB_API_BASE_URL = "https://api.github.com/"
    private const val TIMEOUT_SECONDS = 30L
}
```

### ViewModel Configuration

Located in `UpdateViewModel.kt`:

```kotlin
companion object {
    private const val GITHUB_OWNER = "Vincentjhon31"
    private const val GITHUB_REPO = "SUMVILTAD"
}
```

## Usage

### For Users

1. **Automatic Check**:

   - Open Profile screen
   - App will NOT auto-check (manual only for now)

2. **Manual Check**:

   - Go to Profile → App section
   - Tap "Check for Updates"
   - Wait for check to complete
   - If update available, tap banner or menu item to see details

3. **Download Update**:
   - Tap "Download" in update dialog
   - Browser will open GitHub release page
   - Download APK
   - Install (allow "Install from unknown sources" if needed)

### For Developers

#### Releasing New Version

1. **Update Version Numbers** in `app/build.gradle.kts`:

```kotlin
versionCode = 2           // Increment by 1
versionName = "1.0.1"     // Follow semantic versioning
```

2. **Build Release APK**:

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

3. **Create GitHub Release**:

   - Tag: `v1.0.1` (must match versionName)
   - Upload APK file
   - Add release notes
   - Publish release

4. **Test Update Checker**:
   - Install old version on device
   - Open app → Profile
   - Tap "Check for Updates"
   - Verify update dialog appears

## API Reference

### GitHub API Endpoint

```
GET https://api.github.com/repos/{owner}/{repo}/releases/latest
```

**Response Structure**:

```json
{
  "tag_name": "v1.0.1",
  "name": "SumviltadConnect v1.0.1",
  "body": "## What's New\n- Feature 1\n- Feature 2",
  "html_url": "https://github.com/Vincentjhon31/SUMVILTAD/releases/tag/v1.0.1",
  "published_at": "2025-11-11T10:00:00Z",
  "prerelease": false,
  "draft": false,
  "assets": [
    {
      "name": "SumviltadConnect-v1.0.1.apk",
      "browser_download_url": "https://github.com/.../app-release.apk",
      "size": 52428800,
      "content_type": "application/vnd.android.package-archive"
    }
  ]
}
```

## Troubleshooting

### Update Check Not Working

**Problem**: "Error checking for updates" message

**Solutions**:

1. Check internet connection
2. Verify GitHub repository is public
3. Ensure at least one release exists
4. Check release is not marked as "draft"
5. Verify tag format: `v1.0.0` (with 'v' prefix)

### Update Not Detected

**Problem**: Shows "Up to date" but new version exists

**Solutions**:

1. Verify version numbers:
   - Check `BuildConfig.VERSION_NAME`
   - Compare with GitHub tag (remove 'v' prefix)
2. Ensure version comparison is correct:
   - `1.0.0` < `1.0.1` ✅
   - `1.0.0` < `0.9.9` ❌
3. Clear app data and retry

### Download Not Working

**Problem**: Clicking "Download" does nothing

**Solutions**:

1. Ensure APK is attached to release
2. Check APK filename ends with `.apk`
3. Verify browser permissions
4. Try opening release page instead

## Future Enhancements

### Planned Features

- [ ] Automatic update check on app launch
- [ ] Background update check (daily)
- [ ] In-app update download (no browser)
- [ ] Update scheduling (download later)
- [ ] Update history view
- [ ] Delta updates (smaller downloads)
- [ ] Forced updates for critical security patches

### Code Improvements

- [ ] Add update check frequency preferences
- [ ] Implement exponential backoff for failed checks
- [ ] Cache last check time
- [ ] Add analytics for update adoption rate
- [ ] Support beta/alpha channel selection

## Security Considerations

1. **HTTPS Only**: All GitHub API calls use HTTPS
2. **No Auto-Install**: User must manually install APK
3. **Official Repository**: Only checks official SUMVILTAD repo
4. **Version Validation**: Compares semantic version numbers
5. **User Consent**: User must approve download

## Testing

### Manual Testing Checklist

- [ ] Check with current version (should show "Up to date")
- [ ] Check with older version (should show update available)
- [ ] Check with no internet (should show error)
- [ ] Verify update dialog displays correctly
- [ ] Test download button opens browser
- [ ] Verify "Later" button dismisses dialog
- [ ] Test banner appears when update available
- [ ] Verify auto-dismiss for success/error states

### Test Scenarios

1. **Happy Path**:

   - Install v1.0.0
   - Release v1.0.1 on GitHub
   - Check for updates → Should detect v1.0.1

2. **Edge Cases**:

   - No releases → Should show error
   - Draft release → Should ignore
   - Pre-release → Should detect (if latest)
   - Multiple releases → Should get latest only

3. **Error Handling**:
   - Network timeout → Show error message
   - Invalid JSON → Show error message
   - GitHub API rate limit → Show error message

## Files Created/Modified

### New Files

1. `GitHubRelease.kt` - Data models for GitHub API
2. `GitHubApiService.kt` - Retrofit API interface
3. `UpdateRepository.kt` - Update check logic
4. `UpdateViewModel.kt` - Update state management

### Modified Files

1. `ProfileScreen.kt` - Added update UI components
2. `build.gradle.kts` - BuildConfig fields

### Dependencies

All dependencies already exist in project:

- Retrofit 2.9.0 (networking)
- Gson converter (JSON parsing)
- OkHttp logging (debugging)
- Coroutines (async operations)

## License & Credits

This feature is part of SumviltadConnect, an open-source agricultural technology platform for Filipino rice farmers.

- **Repository**: https://github.com/Vincentjhon31/SUMVILTAD
- **Developer**: Vincentjhon31
- **License**: Open Source

---

**Version**: 1.0.0  
**Last Updated**: November 11, 2025  
**Status**: ✅ Production Ready
