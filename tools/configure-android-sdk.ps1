# LFN-Scout v0.4.86 – local.properties is machine-local and never shipped.
# Reuse an installed Android SDK for CLI and Android Studio without global changes.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$props = Join-Path $root 'local.properties'
$candidates = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
    $candidates += (Join-Path $env:LOCALAPPDATA 'Android\Sdk')
}
$found = $null
foreach ($candidate in $candidates) {
    if (-not [string]::IsNullOrWhiteSpace($candidate)) {
        $platforms = Join-Path $candidate 'platforms'
        if (Test-Path -LiteralPath $platforms) {
            $found = [System.IO.Path]::GetFullPath($candidate)
            break
        }
    }
}
if (-not $found) {
    Write-Host '[LFN-Scout] HINWEIS: Kein lokales Android SDK erkannt. Android Studio kann es beim ersten Sync konfigurieren.'
    exit 0
}
$forward = $found.Replace('\','/')
$lines = if (Test-Path -LiteralPath $props) { @(Get-Content -LiteralPath $props) } else { @() }
$lines = @($lines | Where-Object { $_ -notmatch '^\s*sdk\.dir\s*[:=]' })
$lines += 'sdk.dir=' + $forward
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($props, [string[]]$lines, $utf8)
Write-Host ('[LFN-Scout] Android SDK fuer dieses Projekt: ' + $found)
if (-not (Test-Path -LiteralPath (Join-Path $found 'platforms\android-36\android.jar'))) {
    Write-Host '[LFN-Scout] HINWEIS: Android SDK Platform 36 fehlt evtl.; bitte ueber Android Studio SDK Manager installieren.'
}
