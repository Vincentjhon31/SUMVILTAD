# Profile API Fix - Files to Upload to Hostinger

## Problem
The Android app is getting a 500 error when trying to load the user profile:
```
"message": "Target class [UserController] does not exist."
```

This happens because the routes file on Hostinger is still pointing to the old `UserController` instead of the new `ProfileApiController`.

## Solution - Upload These 2 Files to Hostinger

### File 1: ProfileApiController.php
**Location on Hostinger:** `/domains/fieldconnect.site/public_html/app/Http/Controllers/Api/ProfileApiController.php`
**Local file:** `app/sampledata/app/Http/Controllers/Api/ProfileApiController.php`

This file has been fixed to follow the same clean pattern as CropHealth, Tasks, and Irrigation controllers.

### File 2: api.php (Routes file)
**Location on Hostinger:** `/domains/fieldconnect.site/public_html/routes/api.php`
**Local file:** `app/sampledata/routes/api.php`

The routes file needs to be updated to use `ProfileApiController` instead of `UserController`.

## Critical Route Changes in api.php

The `/api/user` endpoint should point to:
```php
Route::get('/user', [\App\Http\Controllers\Api\ProfileApiController::class, 'index']);
```

NOT to:
```php
Route::get('/user', [UserController::class, 'index']); // ❌ This is wrong
```

## Steps to Fix on Hostinger

1. **Login to your Hostinger file manager or use FTP/SSH**

2. **Upload ProfileApiController.php:**
   - Navigate to: `/domains/fieldconnect.site/public_html/app/Http/Controllers/Api/`
   - Replace the existing `ProfileApiController.php` with the new one

3. **Upload api.php:**
   - Navigate to: `/domains/fieldconnect.site/public_html/routes/`
   - Replace the existing `api.php` with the new one

4. **Clear Laravel cache (if you have SSH access):**
   ```bash
   cd /domains/fieldconnect.site/public_html
   php artisan config:clear
   php artisan cache:clear
   php artisan route:clear
   ```

   If you don't have SSH access, you can create a simple PHP file to clear cache:
   ```php
   <?php
   require __DIR__ . '/vendor/autoload.php';
   $app = require_once __DIR__ . '/bootstrap/app.php';
   $kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
   $kernel->bootstrap();
   
   Artisan::call('config:clear');
   Artisan::call('cache:clear');
   Artisan::call('route:clear');
   
   echo "Cache cleared successfully!";
   ?>
   ```

## How to Verify It's Fixed

After uploading the files, test the API:

1. **Test via browser (with authentication token):**
   ```
   https://fieldconnect.site/api/user
   ```

2. **Expected response:**
   ```json
   {
     "user": {
       "id": 1,
       "name": "John Doe",
       "email": "john@example.com",
       "role": "farmer",
       "farmer_profile": { ... },
       "farm_areas": [ ... ]
     },
     "message": "Profile retrieved successfully",
     "success": true
   }
   ```

3. **Test in Android app:**
   - Open the app
   - Navigate to Profile → Personal Information
   - It should now display all your profile details without errors

## Files Summary

✅ **ProfileApiController.php** - Fixed and ready
✅ **api.php** - Already correctly configured locally
❌ **Hostinger needs both files uploaded**

The Profile API will work exactly like CropHealth, Irrigation, and Tasks once these files are on Hostinger!

