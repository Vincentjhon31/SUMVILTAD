# Firebase Cloud Messaging (FCM) Setup Guide - 2025
## Complete Setup for Push Notifications in SumviltadConnect

This guide follows the official Firebase Cloud Messaging documentation for Android apps (October 2025).

---

## ✅ What Has Been Completed

### 1. **Dependencies Added** ✓
- Firebase BOM version 33.7.0
- Firebase Analytics KTX
- Firebase Messaging KTX
- Google Services plugin configured

### 2. **AndroidManifest.xml Updated** ✓
- POST_NOTIFICATIONS permission added (required for Android 13+)
- MyFirebaseMessagingService registered
- Default notification channel ID metadata configured

### 3. **Firebase Messaging Service Created** ✓
- File: `MyFirebaseMessagingService.kt`
- Handles incoming FCM messages
- Creates notification channels
- Processes both notification and data payloads

### 4. **MainActivity Updated** ✓
- Runtime notification permission request for Android 13+
- FCM token retrieval and logging
- Ready to send token to backend server

---

## 🔥 Firebase Console Setup Steps

Follow these steps in the Firebase Console:

### Step 1: Access Firebase Console
1. Go to [https://console.firebase.google.com/](https://console.firebase.google.com/)
2. Sign in with your Google account
3. Select your existing project or create a new one

### Step 2: Add Android App (if not done)
1. Click on the **gear icon** (⚙️) → **Project settings**
2. Scroll to **"Your apps"** section
3. Click **"Add app"** → Select **Android icon**
4. Fill in the required information:
   - **Android package name**: `com.zynt.sumviltadconnect`
   - **App nickname** (optional): "SumviltadConnect"
   - **Debug signing certificate SHA-1** (optional): Get it using:
     ```cmd
     cd android
     gradlew signingReport
     ```
5. Click **"Register app"**

### Step 3: Download google-services.json
1. After registration, download the `google-services.json` file
2. **IMPORTANT**: Place this file in your project at:
   ```
   SumviltadConnect/app/google-services.json
   ```
3. The file should be at the same level as `build.gradle.kts`
4. **Never commit this file to public repositories** (add to .gitignore)

### Step 4: Enable Cloud Messaging API
1. In Firebase Console, go to **Build** → **Cloud Messaging**
2. If you see "Get started", click it
3. The API should be automatically enabled

### Step 5: Enable Firebase Cloud Messaging API (V1) - IMPORTANT
1. Go to **Project Settings** (⚙️) → **Cloud Messaging** tab
2. Look for **"Cloud Messaging API (Legacy)"** section
3. Click on **"Manage API in Google Cloud Console"**
4. This opens Google Cloud Console
5. Click **"ENABLE"** for **Firebase Cloud Messaging API**
6. Wait for it to enable (may take a few seconds)
7. Return to Firebase Console

### Step 6: Get Server Key (for backend)
If you need to send notifications from your backend server:
1. Go to **Project Settings** → **Cloud Messaging** tab
2. Scroll to **"Cloud Messaging API (Legacy)"** section
3. Copy the **Server key** - store it securely
4. You'll use this key in your backend API to send notifications

### Step 7: Configure App Settings
1. In Firebase Console, go to **Project Settings** → **General** tab
2. Verify your app details:
   - Package name: `com.zynt.sumviltadconnect`
   - App ID (starts with `1:...`)
   - `google-services.json` is downloaded
3. If you added SHA-1 certificate, verify it's listed under "SHA certificate fingerprints"

---

## 📱 Testing Push Notifications

### Test 1: Get FCM Token from Device
1. Build and run your app on a **physical device** (emulator may not support push)
2. Grant notification permission when prompted
3. Open **Logcat** in Android Studio
4. Filter by **"MainActivity"** or **"FCM"**
5. Look for log: `FCM Token: [YOUR_TOKEN]`
6. Copy this token - you'll need it for testing

### Test 2: Send Test Notification from Firebase Console
1. In Firebase Console, go to **Engage** → **Messaging** (or **Cloud Messaging**)
2. Click **"Send your first message"** or **"New campaign"**
3. Fill in the notification details:
   - **Notification title**: "Test Notification"
   - **Notification text**: "Hello from Firebase!"
   - **Notification image** (optional): Leave blank
4. Click **"Next"**
5. Under **Target**:
   - Select **"User segment"**
   - Choose your app: **SumviltadConnect**
   - Select **"All users"**
   - OR select **"Single device"** and paste your FCM token
6. Click **"Next"**
7. **Scheduling**: Select **"Now"**
8. Click **"Next"**
9. Click **"Review"** → **"Publish"**

### Test 3: Verify Notification
- **App in Background**: You should see a system notification
- **App in Foreground**: Check Logcat for "Message received" logs
- Tap the notification to open the app

---

## 🔔 Notification Behaviors

### When App is in Background/Killed
- Firebase automatically displays notifications
- Uses the system notification tray
- Tapping opens MainActivity

### When App is in Foreground
- `onMessageReceived()` is called in `MyFirebaseMessagingService`
- You must manually display the notification
- Full control over notification appearance

### Notification Payload Types

#### 1. Notification Message (Auto-displayed when app in background)
```json
{
  "notification": {
    "title": "New Message",
    "body": "You have a new message!",
    "icon": "ic_notification"
  },
  "to": "DEVICE_FCM_TOKEN"
}
```

#### 2. Data Message (Always delivered to onMessageReceived)
```json
{
  "data": {
    "title": "New Update",
    "message": "Check out the new features!",
    "type": "feature_update"
  },
  "to": "DEVICE_FCM_TOKEN"
}
```

---

## 🚀 Sending Notifications from Backend

### Using Server Key (Legacy API)

**PHP Example:**
```php
<?php
function sendFCMNotification($deviceToken, $title, $message) {
    $serverKey = 'YOUR_SERVER_KEY_FROM_FIREBASE'; // From Step 6
    
    $notification = [
        'title' => $title,
        'body' => $message,
        'icon' => 'ic_notification',
        'sound' => 'default'
    ];
    
    $data = [
        'notification' => $notification,
        'to' => $deviceToken,
        'priority' => 'high'
    ];
    
    $headers = [
        'Authorization: key=' . $serverKey,
        'Content-Type: application/json'
    ];
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    
    $result = curl_exec($ch);
    curl_close($ch);
    
    return json_decode($result, true);
}

// Usage
$result = sendFCMNotification(
    'DEVICE_FCM_TOKEN',
    'Hello!',
    'This is a test notification'
);
print_r($result);
?>
```

### Using HTTP v1 API (Recommended)
Requires OAuth 2.0 authentication. See: [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)

---

## 📋 Topic Subscriptions (Optional)

Subscribe users to topics to send notifications to groups:

### In Android Code:
```kotlin
import com.google.firebase.messaging.FirebaseMessaging

// Subscribe to a topic
FirebaseMessaging.getInstance().subscribeToTopic("all_users")
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("FCM", "Subscribed to topic: all_users")
        }
    }

// Unsubscribe from a topic
FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users")
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("FCM", "Unsubscribed from topic: all_users")
        }
    }
```

### Send to Topic from Firebase Console:
1. Create notification as usual
2. In **Target** step, select **"Topic"**
3. Enter topic name: `all_users`
4. Send notification

---

## 🔧 Troubleshooting

### Issue: Not receiving notifications
**Solutions:**
- ✅ Check `google-services.json` is in `app/` folder
- ✅ Verify package name matches in Firebase Console
- ✅ Test on **physical device**, not emulator
- ✅ Grant notification permission in Android settings
- ✅ Check Logcat for FCM errors
- ✅ Ensure Firebase Cloud Messaging API is enabled in Google Cloud Console

### Issue: Token is null
**Solutions:**
- ✅ Check internet connection
- ✅ Verify `google-services.json` is correct
- ✅ Clean and rebuild project
- ✅ Uninstall and reinstall app

### Issue: Notification not showing in foreground
**Solution:**
- This is expected behavior - you must manually display it
- Check `MyFirebaseMessagingService.kt` - `sendNotification()` method

### Issue: App crashes when receiving notification
**Solutions:**
- ✅ Verify `ic_launcher_foreground` exists in drawable
- ✅ Check `default_notification_channel_id` is in strings.xml
- ✅ Check Logcat for stack trace

---

## 📝 Next Steps

### 1. Store FCM Token on Backend
Update `sendRegistrationToServer()` in `MyFirebaseMessagingService.kt`:
```kotlin
private fun sendRegistrationToServer(token: String?) {
    // Example: Send to your API
    val retrofit = Retrofit.Builder()
        .baseUrl("https://your-api.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api = retrofit.create(YourApi::class.java)
    lifecycleScope.launch {
        try {
            api.updateFCMToken(token)
            Log.d(TAG, "Token sent to server successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send token to server", e)
        }
    }
}
```

### 2. Handle Notification Clicks
Add extras to Intent in `sendNotification()`:
```kotlin
val intent = Intent(this, MainActivity::class.java).apply {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    putExtra("notification_type", type)
    putExtra("notification_data", dataJson)
}
```

### 3. Customize Notification Icon
1. Create notification icon: `res/drawable/ic_notification.xml`
2. Update `MyFirebaseMessagingService.kt`:
```kotlin
.setSmallIcon(R.drawable.ic_notification)
```

### 4. Add Notification Sounds
1. Place sound file in `res/raw/notification_sound.mp3`
2. Update notification builder:
```kotlin
.setSound(Uri.parse("android.resource://" + packageName + "/" + R.raw.notification_sound))
```

---

## 📚 Additional Resources

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Notification Guide](https://developer.android.com/develop/ui/views/notifications)
- [Firebase Console](https://console.firebase.google.com/)
- [Test FCM Notifications](https://firebase.google.com/docs/cloud-messaging/android/first-message)

---

## ✨ Summary

Your app is now ready for Firebase Cloud Messaging! Here's what you need to do:

1. ✅ **Firebase Console Setup** - Follow steps 1-7 above
2. ✅ **Download google-services.json** - Place in `app/` folder
3. ✅ **Build & Run** - Install on physical device
4. ✅ **Grant Permission** - Allow notifications
5. ✅ **Test** - Send test notification from Firebase Console
6. ✅ **Integrate Backend** - Update token storage logic
7. ✅ **Deploy** - Your app is ready for production!

**Good luck with your push notifications! 🎉**

