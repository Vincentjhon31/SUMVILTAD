# Fix for 502 Error in Disease Detection

## Problem Identified

The 502 error occurs because the **backend PHP/Laravel server is returning HTML instead of JSON**. This happens when:

1. ✅ **Mobile app code is working correctly** - It properly handles the API call
2. ❌ **Backend server issue** - The PHP script or ML API times out/crashes
3. ❌ **Web server returns HTML error page** instead of JSON

## Root Causes

### 1. Python ML API Server Not Running
The CropHealthController tries to call `http://127.0.0.1:8000/predict/` but the Python server might not be running.

### 2. ML Processing Takes Too Long
Even with `set_time_limit(300)` and `timeout(180)`, the web server (Apache/Nginx) might have shorter timeout settings.

### 3. Laravel Middleware Returns HTML on Errors
When an error occurs, Laravel might return an HTML error page instead of JSON, especially if the API routes aren't properly configured.

---

## Fixes Required (Backend Only)

### **Fix 1: Ensure JSON Responses for API Errors**

Add this to your `App\Exceptions\Handler.php`:

```php
public function render($request, Throwable $exception)
{
    // Force JSON responses for API routes
    if ($request->is('api/*') || $request->wantsJson()) {
        // Don't return HTML error pages
        if ($exception instanceof \Symfony\Component\HttpKernel\Exception\HttpException) {
            return response()->json([
                'success' => false,
                'message' => $exception->getMessage() ?: 'Server error',
                'error' => class_basename($exception)
            ], $exception->getStatusCode());
        }

        // Handle other exceptions
        return response()->json([
            'success' => false,
            'message' => $exception->getMessage(),
            'error' => class_basename($exception)
        ], 500);
    }

    return parent::render($request, $exception);
}
```

### **Fix 2: Add Timeout Handling in CropHealthController**

Update the `upload()` method to handle timeouts better:

```php
public function upload(Request $request)
{
    // Set PHP execution time limit for image processing (10 minutes for ML processing)
    set_time_limit(600);

    // Increase memory limit
    ini_set('memory_limit', '512M');

    Log::info('Upload method called', ['user_id' => Auth::id()]);

    try {
        $request->validate([
            'image' => 'required|image|max:5120', // Increased to 5MB
        ]);

        // Store image
        $imagePath = $request->file('image')->store('crop-images', 'public');
        $fullPath = $this->resolveImageAbsolutePath($imagePath);

        // Get user preferences
        $user = Auth::user();
        $language = $user->recommendation_language ?? 'english';

        Log::info('Calling ML API for disease detection', ['apiUrl' => $this->apiUrl]);

        // Call ML API with extended timeout
        try {
            $apiResponse = $this->analyzeImageWithRetry($fullPath, 3); // 3 retries
        } catch (\Exception $e) {
            Log::error('ML API failed after retries', ['error' => $e->getMessage()]);
            
            // Return JSON error response for API requests
            if ($request->wantsJson() || $request->is('api/*')) {
                return response()->json([
                    'success' => false,
                    'message' => 'Failed to analyze image. ML API is not responding.',
                    'error' => 'ML_API_TIMEOUT',
                    'details' => $e->getMessage()
                ], 502);
            }
            
            return back()->with('error', 'Failed to analyze image. Please try again later.');
        }

        // Check if not a rice leaf
        if (isset($apiResponse['is_rice_leaf']) && $apiResponse['is_rice_leaf'] === false) {
            Log::warning('Image is not a rice leaf');
            
            if ($request->wantsJson() || $request->is('api/*')) {
                return response()->json([
                    'success' => false,
                    'message' => 'The uploaded image does not appear to be a rice leaf.',
                    'is_rice_leaf' => false
                ], 400);
            }
            
            return back()->with('error', 'The uploaded image does not appear to be a rice leaf.');
        }

        // Create crop health record
        $cropHealth = new CropHealth();
        $cropHealth->user_id = Auth::id();
        $cropHealth->image = $imagePath;
        $cropHealth->disease = $apiResponse['disease'] ?? null;
        $cropHealth->confidence = $apiResponse['confidence'] ?? null;
        $cropHealth->predictions = json_encode($apiResponse['predictions'] ?? []);
        $cropHealth->inference_time_seconds = $apiResponse['inference_time_seconds'] ?? null;
        $cropHealth->recommendation = $apiResponse['recommendation'] ?? $this->getRecommendation($cropHealth->disease, $language);
        $cropHealth->details = $apiResponse['details'] ?? null;
        $cropHealth->save();

        Log::info('Crop health record saved', ['record_id' => $cropHealth->id]);

        // Return JSON response for API requests
        if ($request->wantsJson() || $request->is('api/*')) {
            return response()->json([
                'success' => true,
                'message' => 'Image uploaded and analyzed successfully',
                'record' => [
                    'id' => $cropHealth->id,
                    'image' => $cropHealth->image,
                    'disease' => $cropHealth->disease,
                    'confidence' => $cropHealth->confidence,
                    'recommendation' => $cropHealth->recommendation,
                    'details' => $cropHealth->details,
                    'predictions' => $cropHealth->predictions,
                    'inference_time_seconds' => $cropHealth->inference_time_seconds,
                    'api_status' => $apiResponse['api_status'] ?? 'online',
                    'is_offline' => false,
                    'created_at' => $cropHealth->created_at->toISOString()
                ]
            ]);
        }

        return back()->with('success', 'Image uploaded and analyzed successfully.');

    } catch (\Exception $e) {
        Log::error('Upload failed', [
            'error' => $e->getMessage(),
            'trace' => $e->getTraceAsString()
        ]);

        // Always return JSON for API requests
        if ($request->wantsJson() || $request->is('api/*')) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to process image',
                'error' => $e->getMessage()
            ], 500);
        }

        return back()->with('error', 'Failed to process image: ' . $e->getMessage());
    }
}

/**
 * Analyze image with retry logic
 */
private function analyzeImageWithRetry($imagePath, $maxRetries = 3)
{
    $lastException = null;
    
    for ($attempt = 1; $attempt <= $maxRetries; $attempt++) {
        try {
            Log::info("ML API analysis attempt $attempt of $maxRetries");
            
            // Check if ML API is running
            $healthCheck = Http::timeout(5)->get($this->apiUrl . '/health');
            
            if (!$healthCheck->successful()) {
                throw new \Exception('ML API health check failed');
            }
            
            // Send image for analysis with 3-minute timeout
            $file = fopen($imagePath, 'r');
            $response = Http::timeout(180)->attach(
                'file', $file, basename($imagePath)
            )->post($this->apiUrl . '/predict/');
            fclose($file);
            
            if ($response->successful()) {
                Log::info('ML API analysis successful');
                return $response->json();
            }
            
            throw new \Exception('ML API returned error: ' . $response->status());
            
        } catch (\Exception $e) {
            $lastException = $e;
            Log::warning("ML API attempt $attempt failed", ['error' => $e->getMessage()]);
            
            if ($attempt < $maxRetries) {
                $backoff = pow(2, $attempt); // Exponential backoff: 2, 4, 8 seconds
                Log::info("Retrying in $backoff seconds...");
                sleep($backoff);
            }
        }
    }
    
    throw new \Exception('ML API failed after ' . $maxRetries . ' attempts: ' . $lastException->getMessage());
}
```

### **Fix 3: Increase Web Server Timeouts**

#### For Apache (.htaccess):
```apache
# Add to public/.htaccess or your virtual host config
<IfModule mod_fcgid.c>
    FcgidIOTimeout 600
    FcgidIdleTimeout 600
    FcgidConnectTimeout 60
</IfModule>

<IfModule mod_proxy_fcgi.c>
    ProxyTimeout 600
</IfModule>

php_value max_execution_time 600
php_value max_input_time 600
```

#### For Nginx (nginx.conf or site config):
```nginx
location /api/ {
    fastcgi_read_timeout 600;
    fastcgi_send_timeout 600;
    fastcgi_connect_timeout 60;
    proxy_read_timeout 600;
    proxy_connect_timeout 60;
    proxy_send_timeout 600;
}
```

### **Fix 4: Ensure Python ML API is Running**

Make sure the Python API server is running:

```bash
# Check if it's running
curl http://localhost:8000/health

# If not, start it
cd rice-disease-api
python app.py

# Or use the batch file
start-api-server.bat
```

### **Fix 5: Add API Middleware Check**

Create `app/Http/Middleware/ForceJsonResponse.php`:

```php
<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class ForceJsonResponse
{
    public function handle(Request $request, Closure $next)
    {
        // Force Accept: application/json header for API routes
        $request->headers->set('Accept', 'application/json');
        
        $response = $next($request);
        
        // Ensure response is JSON
        if (!$response->headers->has('Content-Type')) {
            $response->headers->set('Content-Type', 'application/json');
        }
        
        return $response;
    }
}
```

Register in `app/Http/Kernel.php`:

```php
protected $middlewareGroups = [
    'api' => [
        \App\Http\Middleware\ForceJsonResponse::class, // Add this
        'throttle:api',
        \Illuminate\Routing\Middleware\SubstituteBindings::class,
    ],
];
```

---

## Testing the Fix

### 1. Test ML API Directly:
```bash
curl -X POST http://localhost:8000/predict/ \
  -F "file=@path/to/rice-leaf.jpg"
```

Expected output:
```json
{
  "disease": "Bacterial Blight",
  "confidence": 0.85,
  "predictions": [...],
  "is_rice_leaf": true
}
```

### 2. Test Laravel API:
```bash
curl -X POST https://your-domain.com/api/crop-health/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Accept: application/json" \
  -F "image=@path/to/rice-leaf.jpg"
```

Expected output:
```json
{
  "success": true,
  "message": "Image uploaded and analyzed successfully",
  "record": {
    "disease": "Bacterial Blight",
    "confidence": "85.5%",
    ...
  }
}
```

### 3. Check Laravel Logs:
```bash
tail -f storage/logs/laravel.log
```

Look for:
- "Upload method called"
- "ML API analysis successful"
- "Crop health record saved"

---

## Summary

### ✅ Mobile App (No Changes Needed)
The mobile app is working correctly. It:
- Properly sends the image to `/api/crop-health/upload`
- Correctly handles 502 errors
- Shows appropriate error messages

### ❌ Backend Server (Fixes Required)
1. **Add JSON error handling** in Exception Handler
2. **Increase timeouts** (PHP, Apache/Nginx)
3. **Add retry logic** for ML API calls
4. **Force JSON responses** for API routes
5. **Ensure ML API is running** on port 8000

### Priority Actions:
1. ✅ Check if Python ML API is running: `curl http://localhost:8000/health`
2. ✅ Add JSON error handling to `App\Exceptions\Handler.php`
3. ✅ Update `CropHealthController::upload()` method with retry logic
4. ✅ Increase web server timeouts
5. ✅ Test with Postman/curl before testing with mobile app

Once these backend fixes are applied, the mobile app will work perfectly! 🎉

