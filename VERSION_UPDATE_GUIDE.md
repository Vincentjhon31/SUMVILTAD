# Version Update Guide

This document explains how to update the app version for a new release.

## Quick Version Update

### 1. Update Version in build.gradle.kts

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2           // Increment by 1
    versionName = "1.0.1"     // Update to new version
}
```

### 2. Update CHANGELOG.md

Add your changes under a new version section:

```markdown
## [1.0.1] - 2025-11-15

### Added

- New weather integration feature

### Fixed

- Camera crash on Android 13
- Task notification timing issue

### Changed

- Improved ML model accuracy to 95%
```

### 3. Commit Changes

```bash
git add app/build.gradle.kts CHANGELOG.md
git commit -m "chore(release): bump version to 1.0.1"
git push origin main
```

### 4. Create Git Tag

```bash
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1
```

### 5. GitHub Actions will automatically:

- Build the release APK
- Sign the APK
- Create a GitHub Release
- Upload the APK as a release asset

---

## Semantic Versioning Guide

We follow **Semantic Versioning**: `MAJOR.MINOR.PATCH`

### MAJOR Version (X.0.0)

Increment when you make **incompatible API changes** or **major redesigns**

Examples:

- Complete UI redesign
- Database schema breaking changes
- Removing or significantly changing features
- 1.x.x → 2.0.0

### MINOR Version (x.X.0)

Increment when you **add functionality** in a backward-compatible manner

Examples:

- New features (weather integration, market prices)
- New screens or major UI components
- New API integrations
- 1.0.x → 1.1.0

### PATCH Version (x.x.X)

Increment when you make **backward-compatible bug fixes**

Examples:

- Bug fixes
- Performance improvements
- Minor UI tweaks
- Security patches
- 1.0.0 → 1.0.1

---

## Version Code vs Version Name

### Version Code (versionCode)

- **Type**: Integer
- **Purpose**: Internal tracking for Google Play Store
- **Rule**: Must increment by 1 with each release
- **Examples**: 1, 2, 3, 4...
- **Usage**: Play Store uses this to determine which version is newer

### Version Name (versionName)

- **Type**: String
- **Purpose**: User-facing version identifier
- **Rule**: Follow Semantic Versioning (MAJOR.MINOR.PATCH)
- **Examples**: "1.0.0", "1.0.1", "1.1.0", "2.0.0"
- **Usage**: Shown to users in app and Play Store

### Example Timeline

| Release Date | versionCode | versionName | Notes           |
| ------------ | ----------- | ----------- | --------------- |
| 2025-10-30   | 1           | 1.0.0       | Initial release |
| 2025-11-15   | 2           | 1.0.1       | Bug fixes       |
| 2025-12-01   | 3           | 1.1.0       | Weather feature |
| 2025-12-15   | 4           | 1.1.1       | Minor fixes     |
| 2026-01-30   | 5           | 2.0.0       | Major redesign  |

---

## Pre-release Versions

For testing versions before official release:

### Alpha Releases

```
versionName = "1.1.0-alpha.1"
```

- Very early, unstable
- For internal testing only

### Beta Releases

```
versionName = "1.1.0-beta.1"
```

- More stable than alpha
- For beta testers

### Release Candidates

```
versionName = "1.1.0-rc.1"
```

- Nearly final version
- Final testing before release

### Example Pre-release Flow

```
1.0.0 → 1.1.0-alpha.1 → 1.1.0-alpha.2 → 1.1.0-beta.1 → 1.1.0-rc.1 → 1.1.0
```

---

## Automated Version Updates

### Using PowerShell Script (Windows)

Create `update-version.ps1`:

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$NewVersion
)

$buildGradle = "app\build.gradle.kts"
$content = Get-Content $buildGradle -Raw

# Extract current versionCode
if ($content -match 'versionCode = (\d+)') {
    $currentVersionCode = [int]$matches[1]
    $newVersionCode = $currentVersionCode + 1

    # Update versionCode
    $content = $content -replace 'versionCode = \d+', "versionCode = $newVersionCode"

    # Update versionName
    $content = $content -replace 'versionName = "[\d\.]+"', "versionName = `"$NewVersion`""

    # Save file
    Set-Content -Path $buildGradle -Value $content

    Write-Host "✓ Updated version to $NewVersion (code: $newVersionCode)" -ForegroundColor Green
} else {
    Write-Host "✗ Could not find versionCode in build.gradle.kts" -ForegroundColor Red
}
```

Usage:

```powershell
.\update-version.ps1 -NewVersion "1.0.1"
```

### Using Bash Script (Linux/macOS)

Create `update-version.sh`:

```bash
#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./update-version.sh <new-version>"
    echo "Example: ./update-version.sh 1.0.1"
    exit 1
fi

NEW_VERSION=$1
BUILD_GRADLE="app/build.gradle.kts"

# Extract current versionCode
CURRENT_CODE=$(grep -oP 'versionCode = \K\d+' "$BUILD_GRADLE")
NEW_CODE=$((CURRENT_CODE + 1))

# Update versionCode
sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_GRADLE"

# Update versionName
sed -i "s/versionName = \".*\"/versionName = \"$NEW_VERSION\"/" "$BUILD_GRADLE"

echo "✓ Updated version to $NEW_VERSION (code: $NEW_CODE)"
```

Usage:

```bash
chmod +x update-version.sh
./update-version.sh 1.0.1
```

---

## Release Checklist

Before creating a new release:

### Pre-release Checks

- [ ] All tests pass (`./gradlew test`)
- [ ] No lint errors (`./gradlew lint`)
- [ ] Update CHANGELOG.md with all changes
- [ ] Update version in build.gradle.kts
- [ ] Test on multiple devices/Android versions
- [ ] Check for memory leaks
- [ ] Verify all new features work
- [ ] Review code changes since last release

### Release Process

- [ ] Commit version changes
- [ ] Create and push git tag
- [ ] Wait for GitHub Actions to build
- [ ] Download and test the release APK
- [ ] Verify GitHub Release is created
- [ ] Update release notes if needed
- [ ] Announce release to users

### Post-release

- [ ] Monitor crash reports
- [ ] Check user feedback
- [ ] Fix critical bugs immediately
- [ ] Plan next version features

---

## Hotfix Releases

For critical bugs in production:

1. **Create hotfix branch**

   ```bash
   git checkout -b hotfix/critical-crash-fix main
   ```

2. **Fix the bug**

   ```bash
   # Make your fix
   git commit -m "fix(critical): resolve crash on startup"
   ```

3. **Update version (PATCH only)**

   ```
   1.0.0 → 1.0.1
   ```

4. **Release immediately**
   ```bash
   git checkout main
   git merge hotfix/critical-crash-fix
   git tag -a v1.0.1 -m "Hotfix: Critical crash"
   git push origin main --tags
   ```

---

## Version in Code

Access version information at runtime:

```kotlin
import com.zynt.sumviltadconnect.BuildConfig

// Get version name
val versionName = BuildConfig.VERSION_NAME  // "1.0.0"

// Get version code
val versionCode = BuildConfig.VERSION_CODE  // 1

// Display in UI
Text("Version $versionName (Build $versionCode)")

// GitHub release URL
val releaseUrl = BuildConfig.RELEASE_URL  // "https://github.com/..."
```

---

## Troubleshooting

### "Version already exists"

- Make sure you incremented both versionCode and versionName
- Check if the git tag already exists: `git tag -l`

### GitHub Actions fails to build

- Check if google-services.json secret is configured
- Verify signing key secrets are set correctly
- Review Actions logs for specific errors

### APK not uploaded to release

- Ensure the tag follows format: `v1.0.0` (with 'v' prefix)
- Check GitHub Actions permissions
- Verify GITHUB_TOKEN has write access

---

## Resources

- [Semantic Versioning](https://semver.org/)
- [Android App Versioning](https://developer.android.com/studio/publish/versioning)
- [GitHub Releases](https://docs.github.com/en/repositories/releasing-projects-on-github)
- [Keep a Changelog](https://keepachangelog.com/)

---

**Last Updated**: October 30, 2025
