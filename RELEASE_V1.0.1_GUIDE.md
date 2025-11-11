# 🚀 Release v1.0.1 - Step-by-Step Guide

This guide will walk you through pushing your new update to GitHub and creating an automated release.

## 📋 Pre-Release Checklist

Before pushing, make sure:

- [x] Version updated in `app/build.gradle.kts` (v1.0.1, code 2)
- [x] CHANGELOG.md updated with new features
- [x] All code compiles without errors
- [x] Update checker feature tested
- [ ] App tested on physical device
- [ ] All features working correctly

---

## 🔧 Step 1: Commit All Changes

Open PowerShell in the project root and run:

```powershell
# Navigate to project root
cd "C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect"

# Check current status
git status

# Add all new and modified files
git add .

# Commit with descriptive message
git commit -m "Release v1.0.1: Update checker feature and enhanced profile screen

✨ New Features:
- Added automatic GitHub update checker
- Enhanced About and Help & Support dialogs
- Real-time update notifications with visual indicators
- Direct APK download from GitHub releases

🔧 Improvements:
- Better profile screen organization
- Material Design 3 dialogs
- Improved error handling

🐛 Bug Fixes:
- Fixed About section display
- Fixed Help & Support functionality

📦 Technical:
- Added UpdateRepository, UpdateViewModel
- Created GitHubApiService for release checking
- Implemented semantic version comparison
- Added comprehensive documentation"
```

---

## 🏷️ Step 2: Create and Push Git Tag

The tag triggers the GitHub workflow automatically.

```powershell
# Create version tag (must match versionName with 'v' prefix)
git tag v1.0.1

# Verify tag was created
git tag -l

# Push commits to GitHub
git push origin main

# Push the tag (this triggers the workflow!)
git push origin v1.0.1
```

**⚠️ Important**: The tag format MUST be `v1.0.1` (with lowercase 'v' prefix) to trigger the workflow!

---

## 🤖 Step 3: GitHub Workflow Automatically Runs

Once you push the tag, GitHub Actions will automatically:

1. ✅ Checkout your code
2. ✅ Set up Java 17
3. ✅ Decode Firebase credentials
4. ✅ Build release APK
5. ✅ Upload to Firebase App Distribution (testers group)
6. ✅ Create GitHub Release with APK
7. ✅ Upload APK as artifact (90-day backup)

### Monitor the Workflow

1. Go to: https://github.com/Vincentjhon31/SUMVILTAD/actions
2. You'll see "Build & Deploy to Firebase App Distribution" running
3. Click on it to see real-time progress
4. Wait 5-10 minutes for completion

### Workflow Steps Breakdown

```
┌─────────────────────────────────────────┐
│ 1. Checkout Code                        │ (30 seconds)
├─────────────────────────────────────────┤
│ 2. Setup Java 17                        │ (1 minute)
├─────────────────────────────────────────┤
│ 3. Decode google-services.json          │ (5 seconds)
├─────────────────────────────────────────┤
│ 4. Build Release APK                    │ (5-8 minutes)
│    - Download dependencies              │
│    - Compile Kotlin code                │
│    - Process resources                  │
│    - Build APK                          │
├─────────────────────────────────────────┤
│ 5. Upload to Firebase                   │ (1 minute)
│    - Send to testers group              │
│    - Generate distribution link         │
├─────────────────────────────────────────┤
│ 6. Create GitHub Release                │ (30 seconds)
│    - Attach APK file                    │
│    - Generate release notes             │
│    - Publish release                    │
├─────────────────────────────────────────┤
│ 7. Upload Artifact                      │ (30 seconds)
└─────────────────────────────────────────┘
Total Time: ~5-10 minutes
```

---

## ✅ Step 4: Verify Release

### Check GitHub Release

1. Go to: https://github.com/Vincentjhon31/SUMVILTAD/releases
2. You should see "SUMVILTAD Connect v1.0.1"
3. Verify:
   - ✅ Release title is correct
   - ✅ APK file is attached (app-release.apk)
   - ✅ Release notes are generated
   - ✅ Not marked as draft or pre-release

### Check Firebase App Distribution

1. Go to: https://console.firebase.google.com/
2. Select your project
3. Go to: Release & Monitor → App Distribution
4. Verify:
   - ✅ New release v1.0.1 appears
   - ✅ Testers group has access
   - ✅ Distribution link works

### Download and Test

```powershell
# Option 1: Download from GitHub
# Visit: https://github.com/Vincentjhon31/SUMVILTAD/releases/tag/v1.0.1
# Click on app-release.apk to download

# Option 2: Use Firebase App Tester app on Android device
# Testers will receive email notification automatically
```

---

## 🧪 Step 5: Test the Update Checker

This is important - verify your update checker works!

1. **Install OLD version** (v1.0.0) on test device:

   ```powershell
   # If you have v1.0.0 APK saved
   adb install path\to\old\app-v1.0.0.apk
   ```

2. **Open app and check for updates**:

   - Go to Profile screen
   - Tap "Check for Updates"
   - Should show "Update Available" banner
   - Should display update dialog with v1.0.1

3. **Test download**:
   - Tap "Download" button
   - Should open GitHub release page
   - Download APK
   - Install and verify

---

## 📱 Alternative: Manual Push (If Workflow Fails)

If the automated workflow fails, you can create the release manually:

### Build APK Locally

```powershell
# Clean previous builds
.\gradlew clean

# Build release APK
.\gradlew assembleRelease

# Find APK at:
# app\build\outputs\apk\release\app-release.apk
```

### Create GitHub Release Manually

1. Go to: https://github.com/Vincentjhon31/SUMVILTAD/releases/new

2. Fill in details:

   - **Tag**: Select `v1.0.1` (or create if not exists)
   - **Release title**: `SUMVILTAD Connect v1.0.1`
   - **Description**: Copy from CHANGELOG.md

3. Upload APK:

   - Drag `app-release.apk` to attachments area
   - Or click "Attach binaries" and select file

4. Click "Publish release"

---

## 🔍 Troubleshooting

### Problem: Workflow Doesn't Trigger

**Cause**: Tag format incorrect or tag already exists

**Solution**:

```powershell
# Delete existing tag if needed
git tag -d v1.0.1
git push --delete origin v1.0.1

# Create new tag
git tag v1.0.1
git push origin v1.0.1
```

### Problem: Build Fails in Workflow

**Cause**: Missing secrets or gradle issues

**Solution**:

1. Check GitHub Secrets are set:

   - `GOOGLE_SERVICES_JSON`
   - `FIREBASE_APP_ID`
   - `FIREBASE_SERVICE_ACCOUNT`

2. Verify locally first:

```powershell
.\gradlew assembleRelease
```

### Problem: Firebase Upload Fails

**Cause**: Invalid Firebase credentials

**Solution**:

1. Check Firebase project settings
2. Verify service account has correct permissions
3. Update `FIREBASE_SERVICE_ACCOUNT` secret if needed

### Problem: Update Checker Shows "Up to Date"

**Cause**: Version comparison issue or GitHub API delay

**Solution**:

1. Wait 2-3 minutes for GitHub API to update
2. Verify tag format: `v1.0.1` (with 'v')
3. Check release is not marked as "draft"
4. Verify version in build.gradle matches

---

## 📝 Post-Release Tasks

After successful release:

### 1. Update Documentation

```powershell
# Update README.md with new version badge
# Update any version-specific documentation
```

### 2. Notify Stakeholders

- Email testers about new version
- Post on social media (if applicable)
- Update project wiki/docs

### 3. Monitor Issues

- Watch GitHub Issues for bug reports
- Monitor Firebase Crashlytics
- Check user feedback

### 4. Plan Next Release

- Review CHANGELOG "Planned" section
- Prioritize next features
- Create GitHub milestones

---

## 🎯 Quick Command Reference

### Essential Commands

```powershell
# Status check
git status
git log --oneline -5

# Stage and commit
git add .
git commit -m "Your message"

# Create and push tag
git tag v1.0.1
git push origin main
git push origin v1.0.1

# View tags
git tag -l
git show v1.0.1

# Delete tag (if needed)
git tag -d v1.0.1
git push --delete origin v1.0.1

# Build APK
.\gradlew clean assembleRelease

# Install APK
adb install app\build\outputs\apk\release\app-release.apk
```

### Useful Git Commands

```powershell
# View last commit
git log -1

# Undo last commit (keep changes)
git reset --soft HEAD~1

# View remote info
git remote -v

# Check if tag exists
git ls-remote --tags origin
```

---

## 🎉 Success Checklist

After completing all steps, verify:

- [x] Code committed and pushed to GitHub
- [x] Tag v1.0.1 created and pushed
- [x] GitHub workflow completed successfully
- [x] GitHub Release created with APK
- [x] Firebase App Distribution updated
- [x] Testers notified via email
- [x] Update checker detects new version
- [x] APK downloads and installs correctly
- [x] All features working in release build

---

## 📞 Need Help?

- **Workflow Issues**: Check [GitHub Actions documentation](https://docs.github.com/en/actions)
- **Firebase Issues**: Check [Firebase App Distribution docs](https://firebase.google.com/docs/app-distribution)
- **Git Issues**: Check [Git documentation](https://git-scm.com/doc)
- **App Issues**: Open issue at https://github.com/Vincentjhon31/SUMVILTAD/issues

---

**Ready to release? Start with Step 1!** 🚀

_Last updated: November 11, 2025_
