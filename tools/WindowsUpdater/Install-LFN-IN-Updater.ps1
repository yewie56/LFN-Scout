$ErrorActionPreference = 'Stop'
$source = Split-Path -Parent $MyInvocation.MyCommand.Path
$target = Join-Path $env:LOCALAPPDATA 'LFN-IN\Updater'
Write-Host "LFN-IN Updater - Installation" -ForegroundColor Cyan
Write-Host "Quelle: $source"
Write-Host "Ziel:   $target"
New-Item -ItemType Directory -Force -Path $target | Out-Null

Get-ChildItem -LiteralPath $source -File | ForEach-Object {
    if ($_.Name -eq 'settings.json' -and (Test-Path (Join-Path $target 'settings.json'))) {
        Write-Host '[KEEP] vorhandene settings.json bleibt erhalten.' -ForegroundColor Yellow
    } else {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $target $_.Name) -Force
    }
}

$desktop = [Environment]::GetFolderPath('Desktop')
$shortcutPath = Join-Path $desktop 'LFN-IN Update.lnk'
$wsh = New-Object -ComObject WScript.Shell
$shortcut = $wsh.CreateShortcut($shortcutPath)
$shortcut.TargetPath = Join-Path $target 'Update-LFN-IN.cmd'
$shortcut.WorkingDirectory = $target
$shortcut.Description = 'LFN-Scout Update und GitHub-Build'
$shortcut.Save()

Write-Host "[PASS] Updater installiert." -ForegroundColor Green
Write-Host "[PASS] Desktop-Verknuepfung: $shortcutPath" -ForegroundColor Green
Write-Host "Konfiguration: $(Join-Path $target 'settings.json')"
