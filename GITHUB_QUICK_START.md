# Quick Start Guide - Pushing to GitHub

This guide will help you push your SUMVILTAD Connect project to GitHub.

## Prerequisites

- Git installed on your computer
- GitHub account created
- Repository created at: https://github.com/Vincentjhon31/SUMVILTAD.git

---

## Step 1: Initialize Git (If Not Already Done)

Open PowerShell in your project directory and run:

```powershell
git init
```

---

## Step 2: Add Remote Repository

```powershell
git remote add origin https://github.com/Vincentjhon31/SUMVILTAD.git
```

Verify the remote:

```powershell
git remote -v
```

---

## Step 3: Configure Git (First Time Only)

```powershell
git config user.name "Vincent Jhon"
git config user.email "your-email@example.com"
```

---

## Step 4: Add Files to Git

```powershell
# Add all files
git add .

# Or add specific files
git add README.md CHANGELOG.md LICENSE
git add app/
```

Check what will be committed:

```powershell
git status
```

---

## Step 5: Create First Commit

```powershell
git commit -m "Initial commit: SUMVILTAD Connect v1.0.0

- Add comprehensive README with project documentation
- Implement disease detection with PyTorch ML
- Add task management system
- Implement crop health monitoring
- Add irrigation scheduling
- Integrate Firebase push notifications
- Add events and community features
- Setup automated versioning and GitHub releases"
```

---

## Step 6: Create Main Branch

```powershell
# Rename current branch to main
git branch -M main
```

---

## Step 7: Push to GitHub

```powershell
# Push to GitHub
git push -u origin main
```

If you get authentication errors, you may need to:

1. Use a Personal Access Token (PAT) instead of password
2. Or use GitHub CLI: `gh auth login`

---

## Step 8: Create First Release Tag

```powershell
# Create version tag
git tag -a v1.0.0 -m "Release version 1.0.0 - Initial public release"

# Push the tag
git push origin v1.0.0
```

This will trigger GitHub Actions to build and release your APK automatically!

---

## Step 9: Configure GitHub Secrets (For Automated Builds)

Go to: https://github.com/Vincentjhon31/SUMVILTAD/settings/secrets/actions

Add these secrets:

### 1. GOOGLE_SERVICES_JSON

- Encode your `google-services.json`:
  ```powershell
  $base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("app\google-services.json"))
  $base64 | Set-Clipboard
  ```
- Paste the clipboard content as the secret value

### 2. SIGNING_KEY

- Encode your keystore file:
  ```powershell
  $base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\your-keystore.jks"))
  $base64 | Set-Clipboard
  ```
- Paste as secret value

### 3. KEY_STORE_PASSWORD

- Your keystore password

### 4. ALIAS

- Your key alias (e.g., "sumviltad")

### 5. KEY_PASSWORD

- Your key password

---

## Automated Version Update Script

Use the PowerShell script for easy version updates:

```powershell
# Update to new version
.\update-version.ps1 -NewVersion 1.0.1

# The script will:
# 1. Update build.gradle.kts
# 2. Update CHANGELOG.md
# 3. Commit changes
# 4. Create git tag
# 5. Push to GitHub
# 6. Trigger automated build and release
```

---

## Daily Workflow

### Making Changes

```powershell
# 1. Create a feature branch
git checkout -b feature/new-weather-integration

# 2. Make your changes
# ... edit files ...

# 3. Commit changes
git add .
git commit -m "feat(weather): add weather forecast integration"

# 4. Push branch
git push origin feature/new-weather-integration

# 5. Create Pull Request on GitHub
# 6. Merge to main after review
```

### Creating a New Release

```powershell
# 1. Checkout main branch
git checkout main
git pull origin main

# 2. Use the update script
.\update-version.ps1 -NewVersion 1.1.0

# 3. Wait for GitHub Actions to build
# 4. Check releases: https://github.com/Vincentjhon31/SUMVILTAD/releases
```

---

## Useful Git Commands

```powershell
# Check status
git status

# View commit history
git log --oneline --graph --all

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard local changes
git checkout -- <file>

# Pull latest changes
git pull origin main

# View branches
git branch -a

# Delete local branch
git branch -d feature/branch-name

# Delete remote branch
git push origin --delete feature/branch-name

# View tags
git tag -l

# Delete tag locally
git tag -d v1.0.0

# Delete tag remotely
git push origin --delete v1.0.0
```

---

## Troubleshooting

### "Remote origin already exists"

```powershell
git remote remove origin
git remote add origin https://github.com/Vincentjhon31/SUMVILTAD.git
```

### "Authentication failed"

Use a Personal Access Token:

1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Generate new token with `repo` permissions
3. Use token as password when pushing

Or use GitHub CLI:

```powershell
gh auth login
```

### "Large files not uploading"

Add to `.gitignore`:

```
*.apk
*.aab
build/
```

### "Push rejected - non-fast-forward"

```powershell
# Pull first
git pull origin main --rebase

# Then push
git push origin main
```

---

## GitHub Repository Structure

After pushing, your repo will look like:

```
SUMVILTAD/
├── .github/
│   └── workflows/
│       ├── release.yml          # Automated APK builds
│       └── pr-checks.yml        # PR validation
├── app/
│   ├── src/
│   └── build.gradle.kts
├── README.md                    # Main documentation
├── CHANGELOG.md                 # Version history
├── CONTRIBUTING.md              # Contribution guide
├── LICENSE                      # MIT License
├── VERSION_UPDATE_GUIDE.md      # Version management
├── GITHUB_QUICK_START.md        # This file
└── update-version.ps1           # Version update script
```

---

## Viewing Your Project on GitHub

After pushing, visit:

- **Repository**: https://github.com/Vincentjhon31/SUMVILTAD
- **Releases**: https://github.com/Vincentjhon31/SUMVILTAD/releases
- **Actions**: https://github.com/Vincentjhon31/SUMVILTAD/actions
- **Issues**: https://github.com/Vincentjhon31/SUMVILTAD/issues

---

## Enabling GitHub Pages (Optional)

To host documentation:

1. Go to Settings → Pages
2. Select Source: "main" branch, "/docs" folder
3. Your docs will be at: https://vincentjhon31.github.io/SUMVILTAD/

---

## Next Steps

1. ✅ Push code to GitHub
2. ✅ Create first release tag
3. ✅ Configure GitHub secrets for automated builds
4. ⏳ Wait for first automated build
5. ⏳ Test the release APK
6. ⏳ Share with users!

---

**Need Help?**

- GitHub Docs: https://docs.github.com
- Git Docs: https://git-scm.com/doc
- Issues: https://github.com/Vincentjhon31/SUMVILTAD/issues

---

_Last Updated: October 30, 2025_
