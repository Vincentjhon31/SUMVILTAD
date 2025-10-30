# Firebase App Distribution Setup Guide

This guide will help you set up Firebase App Distribution to automatically distribute your APK to testers.

## Benefits

✅ **No file size limits** - Your 205MB APK works perfectly!
✅ **Automatic notifications** - Testers get email when new version is available
✅ **Easy installation** - One-click install from email
✅ **Version tracking** - Keep track of all releases
✅ **Crash reporting** - See crashes from distributed builds
✅ **Free forever** - Part of Firebase Spark plan

---

## Step 1: Create Firebase Service Account

### 1.1 Open Google Cloud Console

Go to: https://console.cloud.google.com/

### 1.2 Select Your Project

- If you already have Firebase project for SUMVILTAD, select it
- If not, create a new project first at https://console.firebase.google.com/

### 1.3 Create Service Account

1. In Google Cloud Console, go to **IAM & Admin** → **Service Accounts**
2. Click **"+ CREATE SERVICE ACCOUNT"**
3. Enter details:
   - **Service account name**: `firebase-app-distribution`
   - **Description**: `GitHub Actions service account for Firebase App Distribution`
4. Click **"CREATE AND CONTINUE"**

### 1.4 Grant Role

1. In **"Grant this service account access to project"** section
2. Select role: **Firebase App Distribution Admin**
3. Click **"CONTINUE"**
4. Click **"DONE"**

### 1.5 Create JSON Key

1. Find your newly created service account in the list
2. Click the **three dots** menu (⋮) → **"Manage keys"**
3. Click **"ADD KEY"** → **"Create new key"**
4. Select **JSON** format
5. Click **"CREATE"**
6. The JSON file will download automatically

**⚠️ IMPORTANT**: Keep this file secure! It contains sensitive credentials.

---

## Step 2: Get Firebase App ID

### 2.1 Open Firebase Console

Go to: https://console.firebase.google.com/

### 2.2 Select Your Project

Click on your SUMVILTAD project

### 2.3 Register Android App (if not already done)

1. Click the **Android icon** to add Android app
2. Enter package name: `com.zynt.sumviltadconnect`
3. Give it a nickname: "SUMVILTAD Connect"
4. Click **"Register app"**
5. Download `google-services.json` (you already have this)

### 2.4 Get App ID

1. Go to **Project Settings** (gear icon) → **Your apps**
2. Under your Android app, you'll see **App ID**
3. It looks like: `1:123456789012:android:abcdef123456789`
4. **Copy this App ID** - you'll need it for GitHub Secrets

---

## Step 3: Configure GitHub Secrets

### 3.1 Open the JSON Key File

Open the downloaded JSON file from Step 1.5 and **copy ALL the content**

### 3.2 Add Secrets to GitHub

1. Go to your GitHub repository: https://github.com/Vincentjhon31/SUMVILTAD
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**

Add these secrets:

#### Secret 1: FIREBASE_APP_ID

- **Name**: `FIREBASE_APP_ID`
- **Value**: Your App ID from Step 2.4 (e.g., `1:123456789012:android:abcdef123456789`)

#### Secret 2: FIREBASE_SERVICE_ACCOUNT

- **Name**: `FIREBASE_SERVICE_ACCOUNT`
- **Value**: Paste the ENTIRE content of the JSON file from Step 3.1

#### Secret 3: GOOGLE_SERVICES_JSON (if not already added)

- **Name**: `GOOGLE_SERVICES_JSON`
- **Value**: Content of your `app/google-services.json` file

---

## Step 4: Create Test Group (Optional)

### 4.1 Add Testers

1. In Firebase Console, go to **App Distribution** → **Testers & Groups**
2. Click **"Add testers"**
3. Enter email addresses of people who will test your app:
   ```
   tester1@example.com
   tester2@example.com
   your.email@example.com
   ```
4. Click **"Add testers"**

### 4.2 Create Group

1. Click **"Groups"** tab
2. Click **"Create group"**
3. Name: `testers` or `beta-testers`
4. Add the testers you just created
5. Click **"Create group"**

---

## Step 5: Update GitHub Actions Workflow

The workflow file has been created at:
`.github/workflows/firebase-distribution.yml`

This workflow will:

- ✅ Build APK when you push tags (e.g., `v1.0.0`)
- ✅ Upload APK to Firebase App Distribution
- ✅ Notify testers via email
- ✅ Add release notes from git commit

---

## Step 6: Test the Setup

### 6.1 Commit and Push

```powershell
git add .github/workflows/firebase-distribution.yml
git commit -m "ci: add Firebase App Distribution workflow"
git push origin main
```

### 6.2 Create a Release Tag

```powershell
git tag -a v1.0.0 -m "Release version 1.0.0 - Initial release with Firebase Distribution"
git push origin v1.0.0
```

### 6.3 Monitor the Build

1. Go to GitHub → **Actions** tab
2. You should see the workflow running
3. Wait for it to complete (takes 5-10 minutes)

### 6.4 Check Firebase Console

1. Go to Firebase Console → **App Distribution** → **Releases**
2. You should see your new release!
3. Testers will receive an email invitation

---

## How Testers Install the App

### First Time Setup

1. Tester receives email: **"You're invited to test SUMVILTAD Connect"**
2. Click **"Get started"** in the email
3. Install **Firebase App Tester** app from Play Store
4. Sign in with their Google account (same email used for invitation)
5. See SUMVILTAD Connect in the list
6. Click **"Download"** and **"Install"**

### Future Updates

1. Tester receives email: **"New build available"**
2. Open Firebase App Tester app
3. Click **"Update"** next to SUMVILTAD Connect
4. That's it! No need to uninstall old version

---

## Distribution Links

After uploading to Firebase, you'll get shareable links:

### Public Download Link

```
https://appdistribution.firebase.dev/i/abcdef123456
```

### For Specific Testers

Invite them via Firebase Console with their email addresses.

---

## Adding Release Notes

Release notes are automatically generated from git commit messages, but you can customize:

```powershell
git tag -a v1.0.1 -m "Version 1.0.1

New Features:
- Weather integration
- Offline disease detection

Bug Fixes:
- Fixed camera crash on Android 13
- Improved notification timing

Known Issues:
- None"

git push origin v1.0.1
```

---

## Troubleshooting

### "Permission denied" Error

- Make sure service account has **Firebase App Distribution Admin** role
- Re-create the service account JSON key

### "App not found" Error

- Verify `FIREBASE_APP_ID` secret is correct
- Check that Android app is registered in Firebase Console

### Testers Not Receiving Emails

- Check spam folder
- Verify email addresses are correct in Firebase Console
- Make sure testers are added to the distribution group

### APK Build Fails

- Check GitHub Actions logs for specific error
- Verify `GOOGLE_SERVICES_JSON` secret is set correctly
- Make sure `google-services.json` is valid

---

## Cost

**Firebase App Distribution is 100% FREE** ✅

- No limit on file size
- No limit on number of distributions
- No limit on number of testers
- Part of Firebase Spark (free) plan

---

## Comparison: Firebase vs Google Drive

| Feature             | Firebase App Distribution | Google Drive       |
| ------------------- | ------------------------- | ------------------ |
| File Size Limit     | ✅ None                   | ✅ 15GB            |
| Auto Updates        | ✅ Yes                    | ❌ No              |
| Email Notifications | ✅ Yes                    | ❌ Manual          |
| One-Click Install   | ✅ Yes                    | ❌ Manual download |
| Version History     | ✅ Yes                    | ❌ No              |
| Crash Reports       | ✅ Yes                    | ❌ No              |
| Tester Management   | ✅ Yes                    | ❌ No              |
| Professional        | ✅ Yes                    | ❌ Personal use    |
| Free                | ✅ Yes                    | ✅ Yes             |

**Winner**: Firebase App Distribution 🏆

---

## Next Steps

After setup:

1. ✅ Test with a small group first (2-3 testers)
2. ✅ Collect feedback
3. ✅ Fix bugs
4. ✅ Release to larger group
5. ✅ Eventually publish to Play Store

---

## Resources

- [Firebase App Distribution Docs](https://firebase.google.com/docs/app-distribution)
- [GitHub Action](https://github.com/wzieba/Firebase-Distribution-Github-Action)
- [Firebase Console](https://console.firebase.google.com/)
- [Google Cloud Console](https://console.cloud.google.com/)

---

**Setup Date**: October 30, 2025
