# 🚀 Quick Start: Release v1.0.1

## Option 1: Automated Release (Recommended) ⚡

### Using the Release Script

1. **Open PowerShell** in the project root directory
2. **Run the release script**:

   ```powershell
   .\release.bat
   ```

   Or directly:

   ```powershell
   .\release.ps1
   ```

3. **Follow the prompts**:

   - Script will check for uncommitted changes
   - Commit changes if needed
   - Create git tag v1.0.1
   - Push to GitHub
   - Trigger GitHub Actions workflow

4. **Done!** The workflow will automatically:
   - Build APK
   - Upload to Firebase
   - Create GitHub Release
   - Notify testers

---

## Option 2: Manual Release 🔧

### Quick Commands

```powershell
# 1. Commit changes
git add .
git commit -m "Release v1.0.1: Update checker and enhanced profile"

# 2. Create and push tag
git tag v1.0.1
git push origin main
git push origin v1.0.1

# 3. Monitor workflow
# Visit: https://github.com/Vincentjhon31/SUMVILTAD/actions
```

---

## What Happens After Push?

### GitHub Actions Workflow (5-10 minutes)

```
✅ Checkout code
✅ Setup Java 17
✅ Build release APK
✅ Upload to Firebase (testers group)
✅ Create GitHub Release
✅ Attach APK to release
```

### Results

- **GitHub Release**: https://github.com/Vincentjhon31/SUMVILTAD/releases/tag/v1.0.1
- **Firebase Distribution**: Testers receive notification
- **APK Available**: Download from GitHub releases

---

## Verify Release ✓

### 1. Check GitHub Actions

- Go to: https://github.com/Vincentjhon31/SUMVILTAD/actions
- Verify workflow completed successfully
- Check for any errors

### 2. Check GitHub Release

- Go to: https://github.com/Vincentjhon31/SUMVILTAD/releases
- Verify v1.0.1 appears
- Download APK and test

### 3. Test Update Checker

- Install old version (v1.0.0)
- Open app → Profile
- Tap "Check for Updates"
- Should show update to v1.0.1

---

## Troubleshooting 🔍

### Workflow Doesn't Start

```powershell
# Check tag format
git tag -l

# Recreate tag if needed
git tag -d v1.0.1
git push --delete origin v1.0.1
git tag v1.0.1
git push origin v1.0.1
```

### Build Fails

```powershell
# Test locally first
.\gradlew clean assembleRelease
```

### Update Checker Shows "Up to Date"

- Wait 2-3 minutes for GitHub API
- Verify tag format: `v1.0.1` (with 'v')
- Check release is not draft

---

## Files Changed in This Release

### Modified Files

- ✏️ `app/build.gradle.kts` - Version updated to 1.0.1
- ✏️ `ProfileScreen.kt` - Added update checker UI
- ✏️ `CHANGELOG.md` - Added v1.0.1 entry

### New Files

- ✨ `GitHubRelease.kt` - GitHub API models
- ✨ `GitHubApiService.kt` - API interface
- ✨ `UpdateRepository.kt` - Update logic
- ✨ `UpdateViewModel.kt` - State management
- ✨ `UPDATE_CHECKER_FEATURE.md` - Documentation
- ✨ `RELEASE_GUIDE.md` - Release instructions
- ✨ `RELEASE_V1.0.1_GUIDE.md` - Detailed guide
- ✨ `release.ps1` - Automated release script
- ✨ `release.bat` - Script wrapper

---

## Need Help? 📞

- **Documentation**: See `RELEASE_V1.0.1_GUIDE.md` for detailed instructions
- **Update Feature**: See `UPDATE_CHECKER_FEATURE.md` for technical details
- **Issues**: https://github.com/Vincentjhon31/SUMVILTAD/issues

---

## Ready? 🎯

**Choose one:**

1. **Automated** (Easy):

   ```powershell
   .\release.bat
   ```

2. **Manual** (Control):
   ```powershell
   git add .
   git commit -m "Release v1.0.1"
   git tag v1.0.1
   git push origin main
   git push origin v1.0.1
   ```

---

**That's it! The GitHub workflow handles the rest!** 🎉

_Created: November 11, 2025_
