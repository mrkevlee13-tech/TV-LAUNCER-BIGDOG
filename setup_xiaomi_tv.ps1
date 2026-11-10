<#
.SYNOPSIS
    BigDog Launcher (B2) - Xiaomi TV ADB Setup & Home Screen Override Script
.DESCRIPTION
    Connects to the Xiaomi TV over wireless ADB, verifies BigDog Launcher is installed,
    enables BigDogHomeAccessibilityService by APPENDING to existing accessibility services
    (so no existing service is lost), and optionally disables the stock launcher.
.EXAMPLE
    .\setup_xiaomi_tv.ps1 -TvIp "192.168.3.16:5555"
    .\setup_xiaomi_tv.ps1 -TvIp "192.168.3.16:5555" -DisableStockLauncher
    .\setup_xiaomi_tv.ps1 -TvIp "192.168.3.16:5555" -Undo
#>

param(
    [string]$TvIp = "192.168.3.16:5555",
    [switch]$InstallApk,
    [switch]$DisableStockLauncher,
    [switch]$DisableXiaomiOverlays,
    [switch]$Undo
)

$ErrorActionPreference = "Stop"
$adb = "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$packageName = "com.aistudio.bigdoglauncher.b2tv"
$serviceComponent = "$packageName/com.example.service.BigDogHomeAccessibilityService"
$mainActivity = "$packageName/com.example.MainActivity"

if (-not (Test-Path $adb)) {
    Write-Warning "ADB not found at default SDK path ($adb). Falling back to 'adb' in PATH."
    $adb = "adb"
}

Write-Host "=== BigDog Launcher (B2) Xiaomi TV Setup ===" -ForegroundColor Cyan
Write-Host "Connecting to TV at $TvIp ..."
& $adb connect $TvIp

if ($Undo) {
    Write-Host "`n[UNDO] Re-enabling stock Android TV & Xiaomi packages..." -ForegroundColor Yellow
    & $adb shell pm enable com.google.android.apps.tv.launcherx 2>$null
    & $adb shell pm enable com.google.android.tvlauncher 2>$null
    & $adb shell pm enable com.xiaomi.mitv.systemui 2>$null
    & $adb shell pm enable com.xiaomi.mitv.smartpanel 2>$null
    & $adb shell pm enable com.xiaomi.mitv.tvplayer 2>$null
    & $adb shell pm enable com.xiaomi.tvqs 2>$null
    Write-Host "Stock packages re-enabled." -ForegroundColor Green
    exit 0
}

if ($InstallApk) {
    $apkPath = ".\app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path $apkPath)) {
        Write-Error "Debug APK not found at $apkPath. Run .\gradlew assembleDebug first."
    }
    Write-Host "`nInstalling BigDog Launcher APK..." -ForegroundColor Cyan
    & $adb install -r $apkPath
}

# Verify BigDog Launcher is installed before changing Home or Accessibility settings
$installedCheck = (& $adb shell pm list packages $packageName) | Out-String
if ($installedCheck -notmatch $packageName) {
    Write-Error "Package '$packageName' is not installed on the TV yet! Install the APK first before running Step A or Step B."
}

Write-Host "`n[Step A] Enabling BigDog Home Accessibility Service (safe append)..." -ForegroundColor Cyan
$currentServices = (& $adb shell settings get secure enabled_accessibility_services).Trim()

if ([string]::IsNullOrWhiteSpace($currentServices) -or $currentServices -eq "null") {
    $newServices = $serviceComponent
} elseif ($currentServices -split ":" -contains $serviceComponent) {
    $newServices = $currentServices
    Write-Host "Service is already present in enabled_accessibility_services." -ForegroundColor Gray
} else {
    $newServices = "$currentServices`:$serviceComponent"
}

& $adb shell settings put secure enabled_accessibility_services "$newServices"
& $adb shell settings put secure accessibility_enabled 1

$verifyServices = (& $adb shell settings get secure enabled_accessibility_services).Trim()
Write-Host "Active Accessibility Services: $verifyServices" -ForegroundColor Green

Write-Host "`nLaunching BigDog Launcher (B2)..." -ForegroundColor Cyan
& $adb shell am start -n $mainActivity

if ($DisableStockLauncher) {
    Write-Host "`n[Step B] Disabling stock TV launcher packages..." -ForegroundColor Yellow
    & $adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx 2>$null
    & $adb shell pm disable-user --user 0 com.google.android.tvlauncher 2>$null
    Write-Host "Stock launchers disabled." -ForegroundColor Green
}

if ($DisableXiaomiOverlays) {
    Write-Host "`n[Step C] Disabling Xiaomi overlay packages..." -ForegroundColor Yellow
    & $adb shell pm disable-user --user 0 com.xiaomi.mitv.smartpanel 2>$null
    & $adb shell pm disable-user --user 0 com.xiaomi.mitv.tvplayer 2>$null
    & $adb shell pm disable-user --user 0 com.xiaomi.tvqs 2>$null
    Write-Host "Xiaomi overlay packages disabled." -ForegroundColor Green
}

Write-Host "`nSetup complete! Press the Home button on your Xiaomi remote to verify." -ForegroundColor Green
