# Firebase Setup Guide

## Overview

This guide will help you set up Firebase configuration for the SUMVILTAD Connect Android app.

## ⚠️ Security Warning

**NEVER commit `google-services.json` to Git!** This file contains sensitive API keys and should be kept private.

## Setup Steps

### 1. Download Your Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **svtc-acd06** (or create a new one)
3. Navigate to **Project Settings** (gear icon)
4. Scroll down to **Your apps** section
5. Find your Android app or add a new one:
   - **Package name**: `com.zynt.sumviltadconnect`
   - **App nickname**: SUMVILTAD Connect (optional)
   - **Debug signing certificate SHA-1**: (optional, for development)
6. Click **Download google-services.json**

### 2. Install the Configuration File

1. Copy the downloaded `google-services.json` file
2. Paste it into the `app/` directory of this project:
   ```
   SumviltadConnect/
   └── app/
       └── google-services.json  ← Place file here
   ```
3. Verify the file is in the correct location (same level as `build.gradle.kts`)

### 3. Verify Configuration

The `google-services.json` file should contain:

```json
{
  "project_info": {
    "project_number": "YOUR_PROJECT_NUMBER",
    "project_id": "YOUR_PROJECT_ID",
    "storage_bucket": "YOUR_PROJECT_ID.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:YOUR_PROJECT_NUMBER:android:YOUR_APP_ID",
        "android_client_info": {
          "package_name": "com.zynt.sumviltadconnect"
        }
      },
      "api_key": [
        {
          "current_key": "AIzaSy..."
        }
      ]
    }
  ]
}
```

**Important**: The `package_name` must match: `com.zynt.sumviltadconnect`

### 4. Enable Required Firebase Services

In Firebase Console, enable these services:

#### Firebase Cloud Messaging (FCM)

1. Go to **Build** → **Cloud Messaging**
2. Click **Get Started** if not already enabled
3. Note your **Server Key** (used for Laravel backend)

#### Firebase App Distribution (Optional)

1. Go to **Release & Monitor** → **App Distribution**
2. Add testers or groups for beta testing

#### Firebase Authentication (If needed)

1. Go to **Build** → **Authentication**
2. Enable sign-in methods as needed

### 5. Update GitHub Secrets (For CI/CD)

If using GitHub Actions for Firebase distribution:

1. Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions**
2. Add the following secrets:
   - `FIREBASE_APP_ID`: Your Firebase App ID (from `google-services.json`)
   - `FIREBASE_SERVICE_ACCOUNT`: Your Firebase service account JSON (for Laravel backend)

### 6. Security Checklist

- [ ] `google-services.json` is in `app/` directory
- [ ] File is **NOT** committed to Git (should show as ignored)
- [ ] `.gitignore` includes `google-services.json`
- [ ] Backup the file securely (password manager, secure cloud storage)
- [ ] Team members have their own copy (don't share via public channels)

## Troubleshooting

### Build Error: "google-services.json is missing"

**Solution**: Download `google-services.json` from Firebase Console and place in `app/` directory.

### API Key Leaked Warning on GitHub

**Solution**:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to **APIs & Services** → **Credentials**
3. Find the leaked API key
4. Click **Regenerate Key** or **Delete** and create a new one
5. Download new `google-services.json` from Firebase Console
6. Update the file locally (don't commit!)

### Push Notifications Not Working

**Solution**:

1. Verify FCM is enabled in Firebase Console
2. Check `google-services.json` has correct `api_key`
3. Ensure Laravel backend has Firebase service account configured
4. Verify FCM tokens are being stored in database

### Package Name Mismatch

**Error**: `Package name 'com.zynt.sumviltadconnect' not found in google-services.json`

**Solution**:

1. In Firebase Console, add Android app with correct package name
2. Download new `google-services.json`
3. Replace the file in `app/` directory

## Alternative Setup (Using Template)

If you don't have access to Firebase Console:

1. Copy `google-services.json.example` to `google-services.json`:
   ```powershell
   Copy-Item app\google-services.json.example app\google-services.json
   ```
2. Edit `app\google-services.json` with your actual values
3. **Never commit** the real file to Git

## Team Setup

For team members:

1. **DO NOT** download `google-services.json` from Git (it should be ignored)
2. Ask project admin for the file via secure channel (encrypted email, password manager share)
3. Place file in `app/` directory locally
4. Verify Git shows it as ignored: `git status` (should not appear)

## Production vs Development

### Development

- Use the Firebase project: **svtc-acd06**
- Test with development build variants

### Production

- Consider creating separate Firebase project for production
- Use different `google-services.json` for release builds
- Keep production keys even more secure

## Need Help?

- [Firebase Documentation](https://firebase.google.com/docs/android/setup)
- [Firebase Cloud Messaging Setup](https://firebase.google.com/docs/cloud-messaging/android/client)
- Check project's SECURITY.md for vulnerability reporting

---

**Remember**: Treat `google-services.json` like a password. Keep it private! 🔒
