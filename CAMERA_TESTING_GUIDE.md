# Camera Feature Testing Guide

## Pre-Testing Setup

### 1. Build the APK

```bash
# Clean build
cd SumviltadConnect
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

### 2. Install on Device

```bash
# Install via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or install manually by copying APK to device
```

### 3. Grant Permissions

- Open Settings → Apps → Sumviltad Connect
- Grant Camera permission
- Grant Storage permission (for gallery access)

## Testing Checklist

### ✅ Basic Camera Functionality

#### Test 1: Camera Opens Correctly

- [ ] Navigate to Disease Detection screen
- [ ] Tap "Take Photo" button
- [ ] Custom camera screen opens (not system camera)
- [ ] Camera preview displays correctly
- [ ] Visual guide overlay appears (square frame with green corners)
- [ ] Instructions card visible at top
- [ ] Controls visible at bottom

**Expected Result**: Full-screen camera with UI elements overlay

#### Test 2: Camera Permission Handling

- [ ] First time: Permission dialog appears
- [ ] Grant permission: Camera opens successfully
- [ ] Deny permission: Stays on Disease Detection screen
- [ ] Open Settings and grant: Next click opens camera

**Expected Result**: Proper permission flow

#### Test 3: Camera Capture

- [ ] Position device to frame a leaf (or any object for testing)
- [ ] Tap large white capture button (center bottom)
- [ ] Button shows loading spinner briefly
- [ ] Returns to Disease Detection screen
- [ ] Captured image displays in preview
- [ ] "Analyzing image..." message shows

**Expected Result**: Image captured and analysis starts

---

### ✅ Camera Controls

#### Test 4: Flash Control

- [ ] Tap flash icon (top-right)
- [ ] Icon cycles: 💡 OFF → 🔆 ON → ⚡ AUTO → 💡 OFF
- [ ] Take photo with flash OFF (dim environment): Dark image
- [ ] Take photo with flash ON: Bright image with flash
- [ ] Take photo with flash AUTO (dim): Flash activates

**Expected Result**: Flash modes work correctly

#### Test 5: Camera Flip

- [ ] Tap flip icon (bottom-left)
- [ ] Camera switches to front camera
- [ ] Preview updates (selfie view)
- [ ] Tap again: Back to rear camera
- [ ] Capture works with both cameras

**Expected Result**: Camera flip works smoothly

#### Test 6: Back/Close Button

- [ ] Open camera screen
- [ ] Tap X button (top-left)
- [ ] Returns to Disease Detection screen
- [ ] No image captured
- [ ] Previous state restored

**Expected Result**: Clean exit without capture

---

### ✅ Image Processing

#### Test 7: Image Size Optimization

- [ ] Take a photo
- [ ] Check file size in cache: `adb shell ls -lh /data/data/com.zynt.sumviltadconnect/cache/`
- [ ] Find processed\_\*.jpg files
- [ ] Verify size is ~50-150 KB (not MB)

**Expected Result**:

- Original: ~2-5 MB
- Processed: ~50-150 KB

#### Test 8: Image Resolution

To verify image is 224x224:

```kotlin
// Add temporary log in DiseaseDetectionViewModel.uploadImage()
val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
Log.d("ImageSize", "Width: ${bitmap?.width}, Height: ${bitmap?.height}")
```

- [ ] Take photo
- [ ] Check logcat: `adb logcat | grep ImageSize`
- [ ] Verify output: `Width: 224, Height: 224`

**Expected Result**: Processed image is exactly 224x224 pixels

#### Test 9: Orientation Correction

- [ ] Hold device in portrait mode, take photo
- [ ] Hold device in landscape mode, take photo
- [ ] Hold device upside down, take photo
- [ ] All images display correctly oriented
- [ ] No sideways or upside-down images

**Expected Result**: All orientations handled correctly

---

### ✅ Disease Detection Integration

#### Test 10: ML Processing with Camera Images

- [ ] Take photo of rice leaf (or test image)
- [ ] Image uploads successfully
- [ ] ML analysis completes
- [ ] Disease result displayed
- [ ] Confidence percentage shown
- [ ] Recommendations displayed

**Expected Result**: Full detection flow works

#### Test 11: "Not a Rice Leaf" Detection

- [ ] Take photo of non-leaf object (hand, wall, etc.)
- [ ] ML analysis completes
- [ ] "Not a Rice Leaf" message shows
- [ ] Error styling applied (red border)
- [ ] Helpful message displayed

**Expected Result**: Non-leaf detection works

#### Test 12: Multiple Scans

- [ ] Complete first scan
- [ ] Tap "New Scan" button
- [ ] Camera opens again
- [ ] Take another photo
- [ ] Second analysis completes
- [ ] Previous result cleared

**Expected Result**: Can perform multiple scans in session

---

### ✅ UI/UX Testing

#### Test 13: Visual Guide Overlay

- [ ] Open camera
- [ ] Verify square guide frame visible
- [ ] Verify green corner brackets visible
- [ ] Verify dashed border (white)
- [ ] Verify dark overlay outside frame
- [ ] Guide helps positioning object

**Expected Result**: Guide overlay aids in framing

#### Test 14: Instructions Clarity

- [ ] Read instruction card
- [ ] Instructions are helpful
- [ ] Instructions are clear
- [ ] Green background stands out
- [ ] Text is readable

**Expected Result**: Instructions guide user effectively

#### Test 15: Loading States

- [ ] Tap capture button
- [ ] Circular progress indicator shows on button
- [ ] Button disabled during capture
- [ ] Loading overlay on image preview
- [ ] "Analyzing image..." text visible
- [ ] "This may take a few seconds" subtext

**Expected Result**: Clear feedback during processing

#### Test 16: Responsive Touch Targets

- [ ] All buttons easy to tap
- [ ] Capture button large enough (80dp)
- [ ] No accidental taps
- [ ] Buttons respond immediately

**Expected Result**: Comfortable touch interaction

---

### ✅ Error Handling

#### Test 17: Camera Failure

- [ ] Use emulator without camera support
- [ ] Or physically cover camera
- [ ] Error message appears
- [ ] Graceful degradation
- [ ] Can return to previous screen

**Expected Result**: Handles camera errors gracefully

#### Test 18: Network Failure During Upload

- [ ] Enable airplane mode
- [ ] Take photo
- [ ] Upload fails with error message
- [ ] Error clearly explains issue
- [ ] Can retry when network restored

**Expected Result**: Network errors handled properly

#### Test 19: Image Processing Failure

To test, temporarily break ImageProcessor:

- [ ] Take photo
- [ ] Processing fails
- [ ] Falls back to original image
- [ ] Or shows error message
- [ ] App doesn't crash

**Expected Result**: Graceful fallback or error message

---

### ✅ Performance Testing

#### Test 20: Camera Launch Speed

- [ ] Measure time from button tap to camera open
- [ ] Should be < 1 second on modern devices
- [ ] Should be < 2 seconds on older devices
- [ ] No lag or freeze

**Expected Result**: Fast camera launch

#### Test 21: Capture Speed

- [ ] Measure time from capture tap to return
- [ ] Processing should be < 2 seconds
- [ ] Image processing efficient
- [ ] UI responsive throughout

**Expected Result**: Fast capture and processing

#### Test 22: Memory Usage

Monitor memory:

```bash
adb shell dumpsys meminfo com.zynt.sumviltadconnect | grep TOTAL
```

- [ ] Open camera multiple times
- [ ] Take multiple photos
- [ ] Memory doesn't grow excessively
- [ ] No memory leaks

**Expected Result**: Stable memory usage

---

### ✅ Device Compatibility

#### Test 23: Different Android Versions

Test on:

- [ ] Android 7 (API 24) - Minimum supported
- [ ] Android 10 (API 29)
- [ ] Android 12 (API 31)
- [ ] Android 14 (API 34+) - Latest

**Expected Result**: Works on all supported versions

#### Test 24: Different Screen Sizes

Test on:

- [ ] Small phone (< 5.5")
- [ ] Medium phone (5.5" - 6.5")
- [ ] Large phone (> 6.5")
- [ ] Tablet (if available)

**Expected Result**: UI adapts to all sizes

#### Test 25: Different Camera Hardware

Test on devices with:

- [ ] Single rear camera
- [ ] Dual/triple camera
- [ ] High megapixel camera (48MP+)
- [ ] Low megapixel camera (8MP)

**Expected Result**: Works with all camera types

---

### ✅ Edge Cases

#### Test 26: Low Storage

- [ ] Fill device storage to near capacity
- [ ] Try to take photo
- [ ] Error message if storage full
- [ ] Or uses available cache space

**Expected Result**: Handles low storage gracefully

#### Test 27: Low Battery

- [ ] Let battery drop to 10-15%
- [ ] Camera still works
- [ ] Performance might be reduced (expected)

**Expected Result**: Camera functional even on low battery

#### Test 28: Rapid Button Tapping

- [ ] Open camera
- [ ] Rapidly tap capture button
- [ ] Only one capture processed
- [ ] No duplicate uploads
- [ ] UI stays responsive

**Expected Result**: Prevents duplicate captures

#### Test 29: Background/Foreground Transitions

- [ ] Open camera
- [ ] Press home button (app goes to background)
- [ ] Return to app
- [ ] Camera resumes correctly
- [ ] Or returns to previous screen gracefully

**Expected Result**: Handles app lifecycle correctly

---

## Regression Testing

### Test 30: Gallery Selection Still Works

- [ ] Tap "Choose from Gallery"
- [ ] Gallery picker opens
- [ ] Select existing image
- [ ] Image uploads and analyzes
- [ ] Results displayed correctly

**Expected Result**: Gallery selection unaffected by camera feature

### Test 31: Other App Features Unaffected

- [ ] Navigate to other screens
- [ ] Check crop health history
- [ ] Check tasks/events
- [ ] All features work normally

**Expected Result**: No regression in other features

---

## Performance Metrics to Record

| Metric                         | Target | Actual | Pass/Fail |
| ------------------------------ | ------ | ------ | --------- |
| Camera launch time             | < 1s   | \_\_\_ | ☐         |
| Capture processing             | < 2s   | \_\_\_ | ☐         |
| Image size reduction           | > 90%  | \_\_\_ | ☐         |
| ML upload time                 | < 3s   | \_\_\_ | ☐         |
| Total flow (capture to result) | < 10s  | \_\_\_ | ☐         |
| Memory usage increase          | < 50MB | \_\_\_ | ☐         |

## Bug Report Template

If you find issues, document them:

```markdown
## Bug Report

**Title**: [Brief description]

**Severity**: Critical / High / Medium / Low

**Environment**:

- Device: [Model]
- Android Version: [e.g., Android 12]
- App Version: [e.g., 1.0.0]

**Steps to Reproduce**:

1. Step one
2. Step two
3. Step three

**Expected Behavior**:
[What should happen]

**Actual Behavior**:
[What actually happened]

**Screenshots/Logs**:
[Attach if available]

**Workaround**:
[If any]
```

## Test Results Summary

```
Date: _____________
Tester: _____________
Device: _____________
Android Version: _____________

✅ Passed: ___ / 31
❌ Failed: ___ / 31
⚠️  Partial: ___ / 31

Critical Issues: ___
High Issues: ___
Medium Issues: ___
Low Issues: ___

Overall Status: ☐ Pass  ☐ Fail  ☐ Needs Fixes

Notes:
___________________________________
___________________________________
___________________________________
```

---

## Next Steps After Testing

### If All Tests Pass ✅

1. Create release build
2. Test release build on multiple devices
3. Prepare for production deployment
4. Update version number in `build.gradle.kts`
5. Generate release APK/AAB
6. Upload to Play Store or distribute via GitHub

### If Tests Fail ❌

1. Document all bugs
2. Prioritize by severity
3. Fix critical/high priority bugs first
4. Re-test after fixes
5. Repeat until all tests pass

### Useful ADB Commands

```bash
# View logs
adb logcat | grep -i camera
adb logcat | grep -i "CameraScreen\|ImageProcessor\|DiseaseDetection"

# Clear app data (fresh start)
adb shell pm clear com.zynt.sumviltadconnect

# Take screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Record video (testing demo)
adb shell screenrecord /sdcard/demo.mp4
# Stop recording: Ctrl+C
adb pull /sdcard/demo.mp4

# Check app storage
adb shell du -sh /data/data/com.zynt.sumviltadconnect/cache/
```

---

**Good luck with testing! 🧪📱**
