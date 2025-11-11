# In-App Camera User Guide

## Visual Flow Diagram

```
┌─────────────────────────────────────┐
│   Disease Detection Screen          │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  How to get accurate results  │  │
│  │  • Use clear, well-lit photos │  │
│  │  • Focus on diseased areas    │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │     Select Image Source       │  │
│  │                               │  │
│  │  ┌─────────────────────────┐  │  │
│  │  │  📷 Take Photo          │  │  │ ◄── Tap this
│  │  └─────────────────────────┘  │  │
│  │                               │  │
│  │  ┌─────────────────────────┐  │  │
│  │  │  🖼️  Choose from Gallery│  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
                  │
                  │ Opens custom camera
                  ▼
┌─────────────────────────────────────┐
│         Camera Screen               │
│ ┌───────────────────────────────┐   │
│ │ ✕  Position leaf in frame  💡│   │ ◄── Top Controls
│ └───────────────────────────────┘   │
│ ┌───────────────────────────────┐   │
│ │ • Keep leaf centered          │   │
│ │ • Ensure good lighting        │   │ ◄── Instructions
│ │ • Focus on diseased areas     │   │
│ │ • Hold steady for clear shot  │   │
│ └───────────────────────────────┘   │
│                                     │
│   ┏━━━━━━━━━━━━━━━━━━━━━━━━┓       │
│   ┃                        ┃       │
│   ┃                        ┃       │
│   ┃    📷 Camera Preview   ┃       │ ◄── Live Preview
│   ┃                        ┃       │
│   ┃  [Square Guide Frame]  ┃       │ ◄── Guide Overlay
│   ┃                        ┃       │
│   ┗━━━━━━━━━━━━━━━━━━━━━━━━┛       │
│                                     │
│ ┌───────────────────────────────┐   │
│ │   🔄        ⚪        📐      │   │ ◄── Bottom Controls
│ │  Flip     Capture    Grid     │   │
│ └───────────────────────────────┘   │
└─────────────────────────────────────┘
                  │
                  │ Captures & processes image
                  ▼
┌─────────────────────────────────────┐
│   Disease Detection Screen          │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  [Captured Image Preview]     │  │
│  │                               │  │
│  │   🔄 Analyzing image...       │  │ ◄── Processing
│  │   This may take a few seconds │  │
│  └───────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
                  │
                  │ ML analysis complete
                  ▼
┌─────────────────────────────────────┐
│   Disease Detection Screen          │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  ✓ Analysis Complete          │  │
│  │    Rice leaf detected         │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  Detected Disease             │  │
│  │  Bacterial Blight             │  │ ◄── Results
│  │                               │  │
│  │  Confidence Level: 87.5% 🟢   │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  💡 Recommendations           │  │
│  │  [Treatment instructions...]  │  │
│  └───────────────────────────────┘  │
│                                     │
│  [🔄 New Scan] [📋 View History]   │
└─────────────────────────────────────┘
```

## Camera Screen Details

### Visual Guide Overlay

```
┌───────────────────────────────┐
│ Semi-transparent dark overlay │
│                               │
│    ╔═══════════════╗          │
│    ║               ║          │
│    ║               ║          │
│    ║  Place leaf   ║ ◄── Dashed square guide
│    ║    here       ║          │
│    ║               ║          │
│    ╚═══════════════╝          │
│                               │
│ Semi-transparent dark overlay │
└───────────────────────────────┘

Corner Brackets (green):
    ┏━━━        ━━━┓
    ┃              ┃

    ┃              ┃
    ┗━━━        ━━━┛
```

## Image Processing Flow

```
┌─────────────────┐
│ Original Image  │  2448 x 3264 pixels
│ From Camera     │  ~2.5 MB
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ EXIF Correction │  Rotate based on device orientation
│                 │  Handle portrait/landscape
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Center Crop     │  Scale to fit shortest side
│                 │  Crop excess from center
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Resize to       │  Exactly 224 x 224 pixels
│ 224x224         │  (ML model input size)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ JPEG Compress   │  90% quality
│                 │  ~50-150 KB
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Processed Image │  Ready for ML API
│ (Optimized)     │  Fast upload & processing
└─────────────────┘
```

## Camera Controls

### Top Bar

```
┌─────────────────────────────────┐
│ [✕]  Position leaf in frame [💡]│
│                                 │
│ ┌─────────────────────────────┐ │
│ │ • Keep leaf centered        │ │
│ │ • Ensure good lighting      │ │
│ │ • Focus on diseased areas   │ │
│ │ • Hold steady              │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘

✕  = Close (back to Disease Detection)
💡 = Flash control (Off/On/Auto)
```

### Bottom Bar

```
┌─────────────────────────────────┐
│                                 │
│   [🔄]       [⚪]       [📐]    │
│   Flip     Capture     Grid     │
│                                 │
└─────────────────────────────────┘

🔄 = Switch front/back camera
⚪ = Capture photo (main button)
📐 = Grid overlay (future feature)
```

### Flash Modes

```
💡 OFF  → No flash
🔆 ON   → Always use flash
⚡ AUTO → Flash when needed
```

## Key Features Summary

### ✅ Automatic Optimization

- Images resized to 224x224 pixels
- Orientation corrected automatically
- Center-cropped for best composition
- Compressed for fast upload

### ✅ User Guidance

- Visual frame shows capture area
- Real-time instructions
- Green corner brackets indicate active zone
- Semi-transparent overlay outside capture area

### ✅ Professional Controls

- Flash control (Off/On/Auto)
- Camera flip (front/back)
- High-quality capture mode
- Full-screen preview

### ✅ Seamless Experience

- No app switching required
- Immediate processing after capture
- Auto-upload to ML API
- Fast results display

## Tips for Best Results

### 📸 Photography Tips

1. **Lighting**: Natural daylight works best
2. **Distance**: Fill 70-80% of guide frame with leaf
3. **Focus**: Tap screen to focus on diseased area
4. **Stability**: Hold device steady or use surface support
5. **Background**: Plain, contrasting background helps

### 🔬 Disease Detection Tips

1. **Single Leaf**: One leaf per photo
2. **Diseased Area**: Include affected parts clearly
3. **Whole Leaf**: Show entire leaf when possible
4. **Clean**: Wipe dust/dirt from leaf
5. **Multiple Angles**: Take several photos if unsure

### ⚡ Performance Tips

1. **Good Connection**: Ensure stable internet
2. **Cache Clearing**: Clear app cache if slow
3. **Recent Photos**: Use freshest leaves
4. **Battery**: Keep device charged (camera uses power)

## Troubleshooting

| Issue                 | Solution                                      |
| --------------------- | --------------------------------------------- |
| **Black screen**      | Grant camera permission in Settings           |
| **Blurry photos**     | Clean camera lens, tap to focus               |
| **Flash not working** | Check flash mode setting (top-right)          |
| **Slow processing**   | Check internet connection                     |
| **Wrong orientation** | Auto-corrected, if issue persists restart app |
| **Can't capture**     | Restart camera screen or app                  |

## Comparison: Before vs After

### Before (Default Camera App)

- ❌ Large file sizes (2-5 MB)
- ❌ Inconsistent orientation
- ❌ No guidance for users
- ❌ App switching required
- ❌ Manual image selection
- ❌ Slow upload times

### After (In-App Camera)

- ✅ Optimized file sizes (~100 KB)
- ✅ Auto-corrected orientation
- ✅ Visual guides and tips
- ✅ Seamless in-app flow
- ✅ Automatic processing
- ✅ Fast upload and results

---

**Happy Scanning! 🌾📱**
