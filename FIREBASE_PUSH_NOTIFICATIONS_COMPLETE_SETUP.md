# Complete Firebase Cloud Messaging (FCM) Push Notifications Setup Guide

## ✅ Current Status

Your test notifications are working! Now let's make sure push notifications work for real events and irrigation schedules.

---

## 📋 What I've Implemented

### Backend (Laravel/PHP)
1. ✅ Database migration for `fcm_token` field in users table
2. ✅ Updated User model to include `fcm_token` in fillable fields
3. ✅ Complete `FirebaseNotificationService` implementation
4. ✅ Integrated FCM notifications in `EventController` (when events are created/updated)
5. ✅ Integrated FCM notifications in `IrrigationScheduleController` (when schedules are created/updated)
6. ✅ API endpoint `/api/notifications/fcm-token` already exists

### Android App
1. ✅ Firebase Messaging Service (`MyFirebaseMessagingService`) already working
2. ✅ Updated MainActivity to send FCM token to backend after login
3. ✅ Updated MyFirebaseMessagingService to send token when refreshed
4. ✅ Updated ApiService to support FCM token storage

---

## 🚀 Steps to Complete Setup

### Step 1: Run Database Migration

Open your terminal and navigate to your Laravel project:

```bash
cd "C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata"
php artisan migrate
```

This will add the `fcm_token` column to your users table.

---

### Step 2: Get Firebase Server Key

1. Go to **Firebase Console**: https://console.firebase.google.com/
2. Select your project: **SumviltadConnect**
3. Click the **⚙️ Settings** icon (gear icon) → **Project settings**
4. Go to the **Cloud Messaging** tab
5. Scroll down to **Cloud Messaging API (Legacy)**
6. Find **Server key** and copy it

**IMPORTANT:** If you see a message saying "Cloud Messaging API (Legacy) is disabled", you need to enable it:
   - Click the **⋮** (three dots) menu
   - Click **Manage API in Google Cloud Console**
   - This will open Google Cloud Console
   - Click **ENABLE** for "Firebase Cloud Messaging API"
   - Wait a few seconds, then go back to Firebase Console and refresh
   - You should now see the Server Key

---

### Step 3: Add Firebase Server Key to Laravel .env

1. Open your `.env` file located at:
   ```
   C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata\.env
   ```

2. Add this line at the end (replace with your actual server key):
   ```env
   FIREBASE_SERVER_KEY=YOUR_FIREBASE_SERVER_KEY_HERE
   ```

3. Save the file

4. Clear Laravel config cache:
   ```bash
   cd "C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata"
   php artisan config:clear
   php artisan cache:clear
   ```

---

### Step 4: Rebuild Android App

Since we modified the code, you need to rebuild the app:

1. In Android Studio, click **Build** → **Clean Project**
2. Then click **Build** → **Rebuild Project**
3. Run the app on your device

---

### Step 5: Test the Integration

#### Test 1: Login and Check Token Storage

1. **Open the app and login** with a farmer account
2. Check Android Logcat for these messages:
   ```
   FCM Token: [your-token-here]
   FCM token sent to server successfully
   ```

3. **Verify in database** that the token is stored:
   - Open your database (phpMyAdmin or MySQL)
   - Check the `users` table
   - Find your user and verify `fcm_token` column has a value

#### Test 2: Create an Event (Admin Dashboard)

1. **Login to your admin web dashboard**
2. **Navigate to Events** page
3. **Create a new event** with these details:
   - Title: "Test Push Notification Event"
   - Description: "Testing FCM notifications"
   - Event Date: Tomorrow
   - Location: Any location
   - Status: Upcoming

4. **Check your mobile app** - you should receive a push notification!

5. **Check Laravel logs** for confirmation:
   ```
   C:\Users\jhon vincent\AndroidStudioProjects\SumviltadConnect\app\sampledata\storage\logs\laravel.log
   ```
   Look for: `FCM notification sent successfully`

#### Test 3: Create Irrigation Schedule (Admin Dashboard)

1. **Login to admin dashboard**
2. **Navigate to Irrigation Schedules**
3. **Create a new schedule** or update existing one
4. **Check your mobile app** - you should receive a push notification!

---

## 🔍 Troubleshooting

### Issue 1: "Firebase Server Key not configured"

**Solution:** Make sure you added `FIREBASE_SERVER_KEY` to your `.env` file and ran `php artisan config:clear`

### Issue 2: No push notifications received

**Checks:**
1. ✅ Verify FCM token is stored in database (users.fcm_token column)
2. ✅ Check Android Logcat for any errors
3. ✅ Check Laravel logs: `storage/logs/laravel.log`
4. ✅ Make sure your device is connected to internet
5. ✅ Verify Firebase Server Key is correct

### Issue 3: "Failed to send FCM token: 401"

**Solution:** This means the user is not authenticated. Make sure:
- User is logged in
- Auth token is stored in SharedPreferences
- API endpoint requires authentication

### Issue 4: Notifications work in test but not from admin

**Solution:**
1. Check if FirebaseNotificationService is being called
2. Add logging in EventController/IrrigationScheduleController
3. Verify farmers have `fcm_token` in database

---

## 📱 How the System Works Now

### When User Opens App:
1. App requests notification permission (Android 13+)
2. Firebase generates FCM token
3. App sends token to backend API: `POST /api/notifications/fcm-token`
4. Backend stores token in `users.fcm_token` column

### When Admin Creates Event:
1. Admin creates event in web dashboard
2. Backend creates database notification records
3. Backend sends email notifications
4. Backend sends SMS notifications (if enabled)
5. **Backend sends FCM push notifications** to all farmers with tokens
6. Farmers receive push notification on their devices

### When Admin Creates/Updates Irrigation Schedule:
1. Admin creates/updates irrigation schedule
2. Backend creates database notification records
3. **Backend sends FCM push notifications** to all farmers with tokens
4. Farmers receive push notification on their devices

---

## 🎯 What Happens When Farmer Taps Notification

Currently, tapping the notification opens the MainActivity. You can customize this behavior later by:
- Adding deep links to specific screens
- Handling notification data in `onMessageReceived`
- Opening specific event/irrigation details

---

## 📄 Files Modified/Created

### Backend (Laravel)
- ✅ `database/migrations/2025_10_27_000001_add_fcm_token_to_users_table.php` (NEW)
- ✅ `app/Models/User.php` (UPDATED)
- ✅ `app/Services/FirebaseNotificationService.php` (NEW)
- ✅ `app/Http/Controllers/EventController.php` (UPDATED)
- ✅ `app/Http/Controllers/IrrigationScheduleController.php` (UPDATED)
- ✅ `app/Http/Controllers/Api/NotificationApiController.php` (Already has storeToken)

### Android App
- ✅ `MainActivity.kt` (UPDATED)
- ✅ `MyFirebaseMessagingService.kt` (UPDATED)
- ✅ `ApiService.kt` (UPDATED)

---

## ✅ Next Steps

1. **Run the migration**: `php artisan migrate`
2. **Add Firebase Server Key** to `.env`
3. **Rebuild Android app**
4. **Login to app** and verify token is sent
5. **Create a test event** in admin dashboard
6. **Verify push notification** is received on device

---

## 🎉 Success Checklist

- [ ] Migration ran successfully
- [ ] Firebase Server Key added to .env
- [ ] Android app rebuilt and running
- [ ] User logged in and FCM token stored in database
- [ ] Created test event from admin dashboard
- [ ] Received push notification on mobile device
- [ ] Push notification works for irrigation schedules
- [ ] Notifications show up with correct title and message

---

## 📞 Need Help?

If you encounter any issues:
1. Check Android Logcat for errors
2. Check Laravel logs: `storage/logs/laravel.log`
3. Verify Firebase Console has Cloud Messaging API enabled
4. Make sure device is connected to internet
5. Try sending test notification from Firebase Console first

---

**Last Updated:** October 27, 2025
**Status:** Ready for Testing ✅

