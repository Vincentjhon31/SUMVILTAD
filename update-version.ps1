# SUMVILTAD Connect - Version Update Script
# This script automates the version update and release process

param(
    [Parameter(Mandatory=$false)]
    [string]$NewVersion,
    
    [Parameter(Mandatory=$false)]
    [switch]$Help
)

function Show-Help {
    Write-Host "SUMVILTAD Connect - Version Update Script" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  .\update-version.ps1 -NewVersion <version>"
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Yellow
    Write-Host "  .\update-version.ps1 -NewVersion 1.0.1     # Patch release"
    Write-Host "  .\update-version.ps1 -NewVersion 1.1.0     # Minor release"
    Write-Host "  .\update-version.ps1 -NewVersion 2.0.0     # Major release"
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  -NewVersion    The new semantic version (MAJOR.MINOR.PATCH)"
    Write-Host "  -Help          Show this help message"
    Write-Host ""
    exit 0
}

function Test-SemanticVersion {
    param([string]$Version)
    
    if ($Version -notmatch '^\d+\.\d+\.\d+$') {
        Write-Host "✗ Invalid version format. Use MAJOR.MINOR.PATCH (e.g., 1.0.1)" -ForegroundColor Red
        return $false
    }
    return $true
}

function Update-BuildGradle {
    param(
        [string]$Version,
        [string]$FilePath = "app\build.gradle.kts"
    )
    
    if (-not (Test-Path $FilePath)) {
        Write-Host "✗ Could not find $FilePath" -ForegroundColor Red
        return $false
    }
    
    $content = Get-Content $FilePath -Raw
    
    # Extract current versionCode
    if ($content -match 'versionCode = (\d+)') {
        $currentVersionCode = [int]$matches[1]
        $newVersionCode = $currentVersionCode + 1
        
        # Update versionCode
        $content = $content -replace 'versionCode = \d+', "versionCode = $newVersionCode"
        
        # Update versionName
        $content = $content -replace 'versionName = "[\d\.]+"', "versionName = `"$Version`""
        
        # Save file
        Set-Content -Path $FilePath -Value $content -NoNewline
        
        Write-Host "✓ Updated $FilePath" -ForegroundColor Green
        Write-Host "  Version Code: $currentVersionCode → $newVersionCode" -ForegroundColor Gray
        Write-Host "  Version Name: → $Version" -ForegroundColor Gray
        return $true
    } else {
        Write-Host "✗ Could not find versionCode in $FilePath" -ForegroundColor Red
        return $false
    }
}

function Update-Changelog {
    param([string]$Version)
    
    $changelogPath = "CHANGELOG.md"
    if (-not (Test-Path $changelogPath)) {
        Write-Host "⚠ CHANGELOG.md not found, skipping..." -ForegroundColor Yellow
        return $true
    }
    
    $date = Get-Date -Format "yyyy-MM-dd"
    $newSection = @"

## [$Version] - $date

### Added
- 

### Changed
- 

### Fixed
- 

"@
    
    $content = Get-Content $changelogPath -Raw
    
    # Insert after "## [Unreleased]" section
    if ($content -match '## \[Unreleased\]') {
        $content = $content -replace '(## \[Unreleased\][^\#]*)', "`$1$newSection"
        Set-Content -Path $changelogPath -Value $content -NoNewline
        Write-Host "✓ Added section to CHANGELOG.md" -ForegroundColor Green
        Write-Host "  ⚠ Please fill in the changelog details!" -ForegroundColor Yellow
    } else {
        Write-Host "⚠ Could not find [Unreleased] section in CHANGELOG.md" -ForegroundColor Yellow
    }
    
    return $true
}

function Show-GitStatus {
    Write-Host ""
    Write-Host "Git Status:" -ForegroundColor Cyan
    git status --short
    Write-Host ""
}

function Prompt-Confirmation {
    param([string]$Message)
    
    $response = Read-Host "$Message (y/n)"
    return $response -eq 'y' -or $response -eq 'Y'
}

# Main script execution
Clear-Host

if ($Help) {
    Show-Help
}

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     SUMVILTAD Connect - Version Update Script        ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if in correct directory
if (-not (Test-Path "app\build.gradle.kts")) {
    Write-Host "✗ Error: Not in the project root directory" -ForegroundColor Red
    Write-Host "Please run this script from the project root." -ForegroundColor Yellow
    exit 1
}

# Get version if not provided
if (-not $NewVersion) {
    Write-Host "Current versions in project:" -ForegroundColor Yellow
    Select-String -Path "app\build.gradle.kts" -Pattern "version(Code|Name) = " | ForEach-Object { Write-Host "  $($_.Line.Trim())" }
    Write-Host ""
    $NewVersion = Read-Host "Enter new version (MAJOR.MINOR.PATCH)"
}

# Validate version format
if (-not (Test-SemanticVersion -Version $NewVersion)) {
    exit 1
}

Write-Host ""
Write-Host "Updating to version: $NewVersion" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""

# Step 1: Update build.gradle.kts
Write-Host "[1/5] Updating build.gradle.kts..." -ForegroundColor Yellow
if (-not (Update-BuildGradle -Version $NewVersion)) {
    exit 1
}

# Step 2: Update CHANGELOG.md
Write-Host ""
Write-Host "[2/5] Updating CHANGELOG.md..." -ForegroundColor Yellow
Update-Changelog -Version $NewVersion

# Step 3: Show git status
Write-Host ""
Write-Host "[3/5] Checking git status..." -ForegroundColor Yellow
Show-GitStatus

# Step 4: Confirm and commit
Write-Host "[4/5] Git operations..." -ForegroundColor Yellow
if (Prompt-Confirmation "Do you want to commit these changes?") {
    git add app\build.gradle.kts CHANGELOG.md
    git commit -m "chore(release): bump version to $NewVersion"
    Write-Host "✓ Changes committed" -ForegroundColor Green
    
    if (Prompt-Confirmation "Do you want to create and push the version tag?") {
        $tagName = "v$NewVersion"
        git tag -a $tagName -m "Release version $NewVersion"
        Write-Host "✓ Created tag: $tagName" -ForegroundColor Green
        
        if (Prompt-Confirmation "Push to remote (origin)?") {
            git push origin main
            git push origin $tagName
            Write-Host "✓ Pushed to remote" -ForegroundColor Green
            Write-Host ""
            Write-Host "🚀 GitHub Actions will now build and release the APK!" -ForegroundColor Green
            Write-Host "   Check progress at: https://github.com/Vincentjhon31/SUMVILTAD/actions" -ForegroundColor Cyan
        }
    }
} else {
    Write-Host "⚠ Changes not committed. You can commit manually later." -ForegroundColor Yellow
}

# Step 5: Next steps
Write-Host ""
Write-Host "[5/5] Next steps:" -ForegroundColor Yellow
Write-Host "  1. Review and complete CHANGELOG.md entries" -ForegroundColor Gray
Write-Host "  2. Wait for GitHub Actions to complete the build" -ForegroundColor Gray
Write-Host "  3. Test the release APK" -ForegroundColor Gray
Write-Host "  4. Edit the GitHub Release notes if needed" -ForegroundColor Gray
Write-Host ""
Write-Host "✓ Version update script completed!" -ForegroundColor Green
Write-Host ""
