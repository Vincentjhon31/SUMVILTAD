# Disease Detection 502 Error Fix

## Problem Summary

When uploading rice leaf images from the mobile app, users were getting "Not a rice leaf" error even though:
1. The images were valid rice leaf photos
2. The same images worked on the website
3. The API logs showed 502 errors (server errors) instead of legitimate rejections

## Root Cause

The issue was in how the app handled HTTP error responses:

1. **502 Bad Gateway Error**: The server was returning HTML instead of JSON due to temporary server issues
2. **Incorrect Error Handling**: The app was treating ALL HTTP errors (except 400) as "not a rice leaf" 
3. **Missing Fallback**: Server errors (502, 503, 504) should trigger offline detection fallback, not show "not rice leaf"

### What was happening:
```
User uploads image → API has server issue → Returns 502 error → App shows "not a rice leaf"
```

### What should happen:
```
User uploads image → API has server issue → App falls back to offline detection → Shows results
```

## Changes Made

### 1. DiseaseDetectionService.kt
**Fixed HTTP error handling to distinguish between different error types:**

- **400 errors** = "Not a rice leaf" (legitimate rejection by API)
- **502/503/504 errors** = Server temporarily unavailable → Trigger offline fallback
- **Other errors** = Network issues → Trigger offline fallback

**Before:**
```kotlin
private fun handleHttpError(e: retrofit2.HttpException): DiseaseDetectionResponse {
    return when (e.code()) {
        400 -> { /* Handle "not rice leaf" */ }
        else -> {
            // ALL other errors returned "not rice leaf" 
            DiseaseDetectionResponse(is_rice_leaf = false, ...)
        }
    }
}
```

**After:**
```kotlin
private fun handleHttpError(e: retrofit2.HttpException): DiseaseDetectionResponse {
    return when (e.code()) {
        400 -> { /* Handle "not rice leaf" */ }
        502, 503, 504 -> {
            // Server errors - throw to trigger offline fallback
            throw Exception("Server is temporarily unavailable...")
        }
        else -> {
            // Other errors - throw to trigger offline fallback
            throw Exception("Server error ${e.code()}...")
        }
    }
}
```

### 2. Enhanced Error Flow in detectDisease()

Added proper try-catch to handle the thrown exceptions and trigger offline fallback:

```kotlin
try {
    val onlineResult = performOnlineDetection(bitmap)
    // ...
} catch (e: retrofit2.HttpException) {
    try {
        handleHttpError(e) // This throws for 502/503/504
    } catch (fallbackException: Exception) {
        // Falls back to offline detection
        performOfflineDetection(bitmap, startTime, "server_error_fallback")
    }
}
```

### 3. DiseaseDetectionViewModel.kt
Added informative messages when offline fallback is used:

```kotlin
if (result.is_offline && result.api_status == "server_error_fallback") {
    _errorMessage.value = "Server temporarily unavailable. Using offline analysis."
}
```

### 4. DiseaseDetectionScreen.kt
Updated UI to show info messages (not errors) when using offline fallback:

- Info messages use blue/tertiary color scheme
- Error messages use red/error color scheme
- Added Info icon for offline fallback messages

## Testing

After these changes, when you upload a rice leaf image:

1. **If API is working**: Shows online detection results
2. **If API returns 502/503/504**: Automatically falls back to offline detection and shows results
3. **If image is not a rice leaf (400 error)**: Shows "not a rice leaf" message
4. **If network is down**: Uses offline detection directly

## Benefits

✅ Users get accurate disease detection even when server has issues
✅ No more false "not a rice leaf" errors due to server problems
✅ Clear messaging about what detection method is being used
✅ Seamless fallback between online and offline detection

## What to Tell Your Backend Team

The 502 errors indicate:
1. Server might be overloaded during image processing
2. PHP timeout issues when running ML model
3. Server configuration issues (nginx/apache timeout settings)

Recommend:
- Increase PHP `max_execution_time` for the ML endpoint
- Increase web server timeout settings
- Add caching for frequently detected diseases
- Consider async processing with webhooks

