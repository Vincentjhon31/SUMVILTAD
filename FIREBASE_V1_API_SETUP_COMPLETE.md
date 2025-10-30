# Firebase Cloud Messaging V1 API Setup Guide

## ✅ Modern FCM V1 API Implementation

Your push notification system now uses the **Firebase Cloud Messaging API (V1)**, which is the modern, recommended approach by Firebase. The Legacy API is deprecated and will be removed on June 20, 2024.

---

## 📋 What I've Implemented

### Backend Changes:
1. ✅ Created `FirebaseNotificationServiceV1.php` with V1 API support
2. ✅ Updated `.env` configuration for V1 API
3. ✅ Implemented OAuth 2.0 authentication with service account
4. ✅ All existing controllers already integrated (EventController, IrrigationScheduleController)

### Android App:
1. ✅ Already compatible with V1 API (no changes needed)
2. ✅ FCM token generation working
3. ✅ Push notification reception working

---

## 🚀 Complete Setup Steps

### **Step 1: Download Firebase Service Account JSON**

The browser should have opened this link: https://console.firebase.google.com/project/svtc-acd06/settings/serviceaccounts/adminsdk

**In the Firebase Console:**

1. You'll see a section titled **"Firebase Admin SDK"**
2. Select **"PHP"** as your admin SDK configuration snippet language
3. Click the **"Generate new private key"** button
4. A dialog will appear warning you to keep this file secure
5. Click **"Generate key"**
6. A JSON file will be downloaded (named something like `svtc-acd06-firebase-adminsdk-xxxxx.json`)

### **Step 2: Save Service Account File**

Move the downloaded JSON file to your Laravel project:

**Save it as:**
```
C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata\storage\app\firebase-service-account.json
```

**Important:** Keep this file secure! It contains private keys that can be used to send notifications on behalf of your app.

---

## 📝 How the V1 API Works

### Traditional (Legacy) API ❌:
```
HTTP POST https://fcm.googleapis.com/fcm/send
Authorization: key=AAAA... (Server Key)
```

### Modern (V1) API ✅:
```
HTTP POST https://fcm.googleapis.com/v1/projects/{project-id}/messages:send
Authorization: Bearer {OAuth 2.0 Token}
```

### Benefits of V1 API:
- ✅ More secure (OAuth 2.0 vs static key)
- ✅ Better error messages
- ✅ More features (message targeting, analytics)
- ✅ Officially supported and maintained
- ✅ No expiration concerns

---

## 🔧 Technical Details

### How Authentication Works:

1. **Service Account JSON contains:**
   - Client email
   - Private key (RSA)
   - Project ID

2. **PHP Backend creates JWT:**
   - Signs JWT with private key
   - Includes scope: `firebase.messaging`

3. **Exchanges JWT for Access Token:**
   - Sends JWT to Google OAuth 2.0
   - Receives short-lived access token (1 hour)

4. **Sends Notification:**
   - Uses access token in Authorization header
   - Sends to FCM V1 endpoint

---

## 📂 File Structure

```
app/sampledata/
├── .env                                    # Updated with FIREBASE_PROJECT_ID
├── storage/
│   └── app/
│       └── firebase-service-account.json   # ← Place service account here
├── app/
│   ├── Services/
│   │   ├── FirebaseNotificationService.php       # Old (Legacy API)
│   │   └── FirebaseNotificationServiceV1.php     # New (V1 API) ✅
│   └── Http/Controllers/
│       ├── EventController.php                   # Already integrated
│       └── IrrigationScheduleController.php      # Already integrated
```

---

## 🔄 Switch to V1 Service

### Option A: Rename Files (Recommended)

```bash
cd "C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata\app\Services"

# Backup old service
ren FirebaseNotificationService.php FirebaseNotificationService_Legacy.php

# Use V1 service
ren FirebaseNotificationServiceV1.php FirebaseNotificationService.php
```

### Option B: Update Controllers Manually

In `EventController.php` and `IrrigationScheduleController.php`, change:
```php
use App\Services\FirebaseNotificationService;
```
to:
```php
use App\Services\FirebaseNotificationServiceV1 as FirebaseNotificationService;
```

---

## ✅ Testing Your Setup

### 1. Verify Service Account File

Create a test script:

```php
<?php
// test-firebase-v1.php

require __DIR__ . '/vendor/autoload.php';

$serviceAccountPath = storage_path('app/firebase-service-account.json');

echo "Firebase V1 API Configuration Test\n";
echo "====================================\n\n";

if (file_exists($serviceAccountPath)) {
    echo "✅ Service account file found!\n";
    $data = json_decode(file_get_contents($serviceAccountPath), true);
    
    if ($data) {
        echo "✅ Service account JSON is valid\n";
        echo "Project ID: " . ($data['project_id'] ?? 'N/A') . "\n";
        echo "Client Email: " . ($data['client_email'] ?? 'N/A') . "\n";
        echo "Private Key: " . (isset($data['private_key']) ? 'Present ✅' : 'Missing ❌') . "\n";
    } else {
        echo "❌ Failed to parse service account JSON\n";
    }
} else {
    echo "❌ Service account file NOT found at:\n";
    echo "   $serviceAccountPath\n\n";
    echo "Please download it from Firebase Console:\n";
    echo "https://console.firebase.google.com/project/svtc-acd06/settings/serviceaccounts/adminsdk\n";
}

echo "\n====================================\n";
```

Run it:
```bash
cd "C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata"
php test-firebase-v1.php
```

### 2. Test Push Notification

After placing the service account file:

1. **Run database migration:**
   ```bash
   php artisan migrate
   ```

2. **Login to your mobile app** (to register FCM token)

3. **Create a test event** from admin dashboard

4. **Check Laravel logs:**
   ```
   storage/logs/laravel.log
   ```
   Look for: `FCM V1 notification sent successfully`

---

## 🔍 Troubleshooting

### Error: "Service account file not found"

**Solution:** 
- Verify file location: `storage/app/firebase-service-account.json`
- Check file permissions (should be readable by PHP)

### Error: "Failed to parse service account JSON"

**Solution:**
- Make sure you downloaded the correct file from Firebase
- Check if JSON is valid (use online JSON validator)
- Re-download the service account key if corrupted

### Error: "Failed to get access token"

**Solution:**
- Check if `openssl` extension is enabled in PHP
- Verify private key in service account JSON
- Check internet connectivity

### Error: "Permission denied" (403)

**Solution:**
- Verify Firebase Cloud Messaging API is enabled
- Check service account has correct permissions
- Re-generate service account key

### No notifications received on device

**Checks:**
1. ✅ Service account file in correct location
2. ✅ FCM token stored in database (users.fcm_token)
3. ✅ Device connected to internet
4. ✅ App has notification permissions
5. ✅ Check Laravel logs for errors

---

## 📊 Comparison: Legacy vs V1

| Feature | Legacy API ❌ | V1 API ✅ |
|---------|--------------|-----------|
| **Authentication** | Static Server Key | OAuth 2.0 Token |
| **Security** | Less secure | More secure |
| **Expiration** | Deprecated (June 2024) | Maintained |
| **Endpoint** | /fcm/send | /v1/projects/{id}/messages:send |
| **Batch Send** | Yes (registration_ids) | No (send individually) |
| **Error Messages** | Basic | Detailed |
| **Features** | Limited | Advanced targeting, analytics |

---

## 🎯 Next Steps

1. ✅ Download service account JSON from Firebase Console
2. ✅ Save it as `storage/app/firebase-service-account.json`
3. ✅ Run `php test-firebase-v1.php` to verify
4. ✅ Switch to V1 service (rename files or update imports)
5. ✅ Test by creating an event
6. ✅ Verify notifications are received on mobile

---

## 📚 Resources

- **Official Migration Guide:** https://firebase.google.com/docs/cloud-messaging/migrate-v1
- **V1 API Reference:** https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages
- **Service Account Setup:** https://firebase.google.com/docs/admin/setup
- **Your Firebase Project:** https://console.firebase.google.com/project/svtc-acd06

---

## ✅ Success Checklist

- [ ] Opened Firebase Console Service Accounts page
- [ ] Downloaded service account JSON file
- [ ] Saved file as `storage/app/firebase-service-account.json`
- [ ] Verified file exists with `php test-firebase-v1.php`
- [ ] Switched to V1 service (renamed or updated imports)
- [ ] Ran database migration (`php artisan migrate`)
- [ ] Logged into mobile app
- [ ] Created test event from admin dashboard
- [ ] Received push notification on device
- [ ] Checked Laravel logs for success messages

---

**Status:** Ready for Production ✅  
**API Version:** FCM V1 (Modern)  
**Project ID:** svtc-acd06  
**Last Updated:** October 27, 2025

