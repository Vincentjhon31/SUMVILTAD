# 📤 Quick Upload Checklist for Hosting

## ✅ Files to Upload

### Required File:
- **bootstrap/app.php** ← Modified with JSON error handling

### Location on Server:
```
/public_html/sumviltadCo/bootstrap/app.php
```

---

## 🚀 Step-by-Step Upload Process

### Option 1: Using cPanel File Manager

1. **Login to cPanel**
2. **Open File Manager**
3. **Navigate to:** `/public_html/sumviltadCo/bootstrap/`
4. **Backup existing file:**
   - Right-click `app.php`
   - Click "Copy"
   - Name it `app.php.backup`
5. **Upload new file:**
   - Click "Upload"
   - Select your modified `app.php`
   - Overwrite existing file
6. **Verify file size changed** (should be larger now)

---

### Option 2: Using FTP (FileZilla)

1. **Connect to your hosting** via FTP
2. **Navigate to:** `/public_html/sumviltadCo/bootstrap/`
3. **Backup:** Rename `app.php` to `app.php.backup`
4. **Upload:** Drag and drop your modified `app.php`
5. **Verify:** Check file modification date

---

### Option 3: Using SSH (Advanced)

```bash
# SSH into server
ssh username@your-domain.com

# Navigate to project
cd /home/username/public_html/sumviltadCo

# Backup original
cp bootstrap/app.php bootstrap/app.php.backup

# Upload new file (use your preferred method: scp, git, etc.)

# Set correct permissions
chmod 644 bootstrap/app.php

# Clear cache
php artisan cache:clear
php artisan config:clear
php artisan route:clear
```

---

## 🧹 Clear Cache After Upload

### Via SSH (Recommended):
```bash
cd /path/to/project
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan config:cache
```

### Via cPanel (Alternative):
1. Open **File Manager**
2. Navigate to `bootstrap/cache/`
3. **Delete all files** except `.gitignore`
4. Navigate to `storage/framework/cache/`
5. **Delete all files** in subdirectories

### Via PHP Script (Last Resort):
Create `clear-cache.php` in public folder:
```php
<?php
require __DIR__.'/../vendor/autoload.php';
$app = require_once __DIR__.'/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->call('cache:clear');
$kernel->call('config:clear');
$kernel->call('route:clear');
echo "Cache cleared!";
```

Visit: `https://your-domain.com/clear-cache.php`

---

## ✅ Verify Upload Success

### Test 1: Check File Exists
Visit: `https://your-domain.com/` (should still work)

### Test 2: Test API Endpoint
```bash
curl -H "Accept: application/json" https://your-domain.com/api/ping
```

Expected: JSON response

### Test 3: Test Error Handling
```bash
curl -H "Accept: application/json" https://your-domain.com/api/nonexistent
```

Expected: JSON error (not HTML)

### Test 4: Mobile App
- Open app
- Try uploading rice leaf image
- Check for proper error messages (no crashes)

---

## 🎯 What to Expect

### ✅ Success Indicators:
- No more "API returned HTML instead of JSON" in LogCat
- Proper error messages in app
- No app crashes on server errors

### ❌ If Still Not Working:

**Check 1: File uploaded correctly?**
- Compare file sizes (local vs server)
- Check modification date

**Check 2: Cache cleared?**
- Run cache clear commands again
- Delete `bootstrap/cache/*.php` files

**Check 3: PHP version compatible?**
- Requires PHP 8.1+ for Laravel 11
- Check via: `php -v`

**Check 4: File permissions?**
```bash
chmod 644 bootstrap/app.php
chmod 755 bootstrap/
```

---

## 🆘 Rollback if Needed

If something breaks:

### Quick Rollback:
1. **Rename** `app.php` to `app.php.new`
2. **Rename** `app.php.backup` to `app.php`
3. **Clear cache**
4. **Test site**

---

## 📞 Final Checklist

- [ ] Backed up original `app.php`
- [ ] Uploaded modified `app.php`
- [ ] Cleared Laravel cache
- [ ] Tested API endpoint returns JSON
- [ ] Tested mobile app
- [ ] Verified no more HTML errors in LogCat

---

## 🎉 Done!

Once uploaded and cache cleared:
- Your API will always return JSON (never HTML)
- Mobile app will show proper error messages
- No more 502 crashes!

**Time to complete:** 5-10 minutes

Good luck! 🚀

