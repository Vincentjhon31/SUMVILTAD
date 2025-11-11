@echo off
REM Release Script for SumviltadConnect v1.0.1
REM Simple wrapper to run the PowerShell release script

echo ================================
echo   SUMVILTAD Connect Release
echo   Version 1.0.1
echo ================================
echo.

echo Running release script...
echo.

powershell -ExecutionPolicy Bypass -File release.ps1

echo.
pause
