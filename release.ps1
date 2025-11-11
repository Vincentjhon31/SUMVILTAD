# Release Script for SumviltadConnect v1.0.1
# This script automates the git commit, tag, and push process

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  SUMVILTAD Connect Release" -ForegroundColor Cyan
Write-Host "  Version 1.0.1" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if we're in the right directory
if (-Not (Test-Path "app/build.gradle.kts")) {
    Write-Host "❌ Error: Please run this script from the project root directory!" -ForegroundColor Red
    Write-Host "Current directory: $(Get-Location)" -ForegroundColor Yellow
    exit 1
}

Write-Host "📂 Project directory confirmed" -ForegroundColor Green
Write-Host ""

# Check for uncommitted changes
Write-Host "🔍 Checking for uncommitted changes..." -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "📝 Found uncommitted changes:" -ForegroundColor Yellow
    Write-Host $status
    Write-Host ""
    
    $commit = Read-Host "Do you want to commit these changes? (y/n)"
    if ($commit -eq "y" -or $commit -eq "Y") {
        Write-Host ""
        Write-Host "📦 Staging all changes..." -ForegroundColor Yellow
        git add .
        
        Write-Host "✅ Changes staged" -ForegroundColor Green
        Write-Host ""
        Write-Host "💾 Committing changes..." -ForegroundColor Yellow
        
        $commitMessage = @"
Release v1.0.1: Update checker feature and enhanced profile screen

✨ New Features:
- Added automatic GitHub update checker
- Enhanced About and Help & Support dialogs
- Real-time update notifications with visual indicators
- Direct APK download from GitHub releases

🔧 Improvements:
- Better profile screen organization
- Material Design 3 dialogs
- Improved error handling

🐛 Bug Fixes:
- Fixed About section display
- Fixed Help & Support functionality

📦 Technical:
- Added UpdateRepository, UpdateViewModel
- Created GitHubApiService for release checking
- Implemented semantic version comparison
- Added comprehensive documentation
"@
        
        git commit -m $commitMessage
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Commit successful" -ForegroundColor Green
        } else {
            Write-Host "❌ Commit failed" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "⚠️  Skipping commit. Please commit manually." -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host "✅ No uncommitted changes found" -ForegroundColor Green
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Creating Release Tag" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$version = "v1.0.1"

# Check if tag already exists
$tagExists = git tag -l $version
if ($tagExists) {
    Write-Host "⚠️  Tag $version already exists!" -ForegroundColor Yellow
    $delete = Read-Host "Do you want to delete and recreate it? (y/n)"
    if ($delete -eq "y" -or $delete -eq "Y") {
        Write-Host "🗑️  Deleting local tag..." -ForegroundColor Yellow
        git tag -d $version
        
        Write-Host "🗑️  Deleting remote tag..." -ForegroundColor Yellow
        git push --delete origin $version 2>$null
        
        Write-Host "✅ Old tag deleted" -ForegroundColor Green
    } else {
        Write-Host "❌ Release cancelled" -ForegroundColor Red
        exit 0
    }
}

Write-Host "🏷️  Creating tag $version..." -ForegroundColor Yellow
git tag $version

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tag created successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Tag creation failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Pushing to GitHub" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📤 Pushing commits to GitHub..." -ForegroundColor Yellow
git push origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Commits pushed successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Push failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📤 Pushing tag to GitHub..." -ForegroundColor Yellow
Write-Host "⚡ This will trigger the GitHub Actions workflow!" -ForegroundColor Magenta
Write-Host ""

$push = Read-Host "Ready to push the tag and trigger the release? (y/n)"
if ($push -eq "y" -or $push -eq "Y") {
    git push origin $version
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "================================" -ForegroundColor Green
        Write-Host "  ✅ RELEASE INITIATED!" -ForegroundColor Green
        Write-Host "================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "🤖 GitHub Actions workflow is now running!" -ForegroundColor Green
        Write-Host "📊 Monitor progress at:" -ForegroundColor Yellow
        Write-Host "   https://github.com/Vincentjhon31/SUMVILTAD/actions" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "⏱️  Expected completion time: 5-10 minutes" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "📦 Once complete, your release will be available at:" -ForegroundColor Yellow
        Write-Host "   https://github.com/Vincentjhon31/SUMVILTAD/releases/tag/$version" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "🔔 Testers will receive Firebase notification" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "================================" -ForegroundColor Green
        Write-Host "  Next Steps:" -ForegroundColor Yellow
        Write-Host "================================" -ForegroundColor Green
        Write-Host "1. Monitor workflow at GitHub Actions" -ForegroundColor White
        Write-Host "2. Verify release appears at GitHub Releases" -ForegroundColor White
        Write-Host "3. Check Firebase App Distribution" -ForegroundColor White
        Write-Host "4. Test update checker with old version" -ForegroundColor White
        Write-Host "5. Notify stakeholders" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host "❌ Tag push failed" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "❌ Release cancelled" -ForegroundColor Yellow
    Write-Host "💡 You can manually push the tag later with:" -ForegroundColor Yellow
    Write-Host "   git push origin $version" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "🎉 Script completed!" -ForegroundColor Green
Write-Host ""
