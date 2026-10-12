param(
    [string]$OutputDirectory = ''
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $root

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $root 'Builds'
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$buildFile = Join-Path $root 'app\build.gradle.kts'
$buildText = Get-Content -LiteralPath $buildFile -Raw
$versionMatch = [regex]::Match($buildText, 'versionName\s*=\s*"([^"]+)"')
$codeMatch = [regex]::Match($buildText, 'versionCode\s*=\s*(\d+)')
if (-not $versionMatch.Success -or -not $codeMatch.Success) {
    throw 'versionName/versionCode konnten nicht aus app/build.gradle.kts gelesen werden.'
}
$version = $versionMatch.Groups[1].Value
$versionCode = [int]$codeMatch.Groups[1].Value

$zipPath = Join-Path $OutputDirectory ("LFN-Scout_ACTIVE_v{0}.zip" -f $version)
$shaPath = $zipPath + '.sha256'
$stageParent = Join-Path $OutputDirectory ('.active_stage_' + [guid]::NewGuid().ToString('N'))
$projectName = 'LFN-Scout_Android_v' + $version
$stageRoot = Join-Path $stageParent $projectName
$verifyDir = Join-Path $OutputDirectory ('.active_verify_' + [guid]::NewGuid().ToString('N'))

$basePrefixes = @(
    'docs/project/analyzer_v0.7/assets/',
    'docs/project/analyzer_v0.7/references/',
    'docs/project/handoff_assets/',
    'docs/project/FILTERBANK_UI_REFERENCE/',
    'docs/reconstruction/',
    'supabase/storage_backup'
)
$excludedDirectoryNames = @('.git', '.gradle', '.idea', '.tools', 'Builds', 'build')
$excludedFileNames = @('local.properties')

function Normalize-Relative([string]$path) {
    return $path.Replace('\','/').TrimStart([char[]]'./')
}

function Test-ExcludedRelative([string]$relative) {
    $r = Normalize-Relative $relative
    foreach ($prefix in $basePrefixes) {
        if ($r.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) { return $true }
    }
    $parts = $r.Split('/')
    foreach ($part in $parts) {
        if ($excludedDirectoryNames -contains $part) { return $true }
    }
    if ($excludedFileNames -contains ([System.IO.Path]::GetFileName($r))) { return $true }
    if ($r -match '\.(apk|aab|log)$') { return $true }
    return $false
}

try {
    New-Item -ItemType Directory -Force -Path $stageRoot | Out-Null

    $rootPrefixLength = $root.Length + 1
    $files = Get-ChildItem -LiteralPath $root -Recurse -File -Force | Where-Object {
        $relative = $_.FullName.Substring($rootPrefixLength)
        -not (Test-ExcludedRelative $relative)
    }

    foreach ($file in $files) {
        $relative = $file.FullName.Substring($rootPrefixLength)
        $target = Join-Path $stageRoot $relative
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null
        Copy-Item -LiteralPath $file.FullName -Destination $target -Force
    }

    # PREPARE may have written machine-local JDK paths into gradle.properties.
    # Keep the portable base properties, remove only machine-local additions.
    $stageGradleProps = Join-Path $stageRoot 'gradle.properties'
    if (Test-Path -LiteralPath $stageGradleProps) {
        $lines = Get-Content -LiteralPath $stageGradleProps | Where-Object {
            $_ -notmatch '^\s*org\.gradle\.java\.home\s*=' -and
            $_ -notmatch '^\s*org\.gradle\.java\.installations\.paths\s*='
        }
        $utf8 = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllLines($stageGradleProps, [string[]]$lines, $utf8)
    }

    $baseRequirement = Join-Path $stageRoot 'BASE_PACKAGE_REQUIRED.txt'
    $baseText = if (Test-Path -LiteralPath $baseRequirement) {
        Get-Content -LiteralPath $baseRequirement -Raw
    } else {
        'Kein BASE_PACKAGE_REQUIRED.txt im Quellstand gefunden.'
    }

    $manifest = [ordered]@{
        package_type = 'LFN-Scout ACTIVE'
        version = $version
        version_code = $versionCode
        created_at = (Get-Date).ToString('o')
        purpose = 'Buildfaehiger aktiver Quellstand; grosse selten veraenderte Referenz-/Recovery-Artefakte liegen im separat versionierten BASE-Paket.'
        excluded_base_paths = $basePrefixes
        excluded_generated = @('.git', '.gradle', '.idea', '.tools', 'Builds', '**/build', 'local.properties', '*.apk', '*.aab', '*.log')
        base_requirement_text = $baseText.Trim()
        source_file_count = @($files).Count
    }
    $manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $stageRoot 'ACTIVE_PACKAGE_MANIFEST.json') -Encoding UTF8

    if (Test-Path -LiteralPath $zipPath) { Remove-Item -LiteralPath $zipPath -Force }
    [System.IO.Compression.ZipFile]::CreateFromDirectory($stageParent, $zipPath, [System.IO.Compression.CompressionLevel]::Optimal, $false)

    # Verify that the generated ZIP can be opened and fully extracted.
    New-Item -ItemType Directory -Force -Path $verifyDir | Out-Null
    [System.IO.Compression.ZipFile]::ExtractToDirectory($zipPath, $verifyDir)
    $expectedMain = Join-Path $verifyDir ($projectName + '\app\build.gradle.kts')
    if (-not (Test-Path -LiteralPath $expectedMain)) {
        throw ('ACTIVE-ZIP-Verifikation fehlgeschlagen, erwartete Datei fehlt: ' + $expectedMain)
    }

    $hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $size = (Get-Item -LiteralPath $zipPath).Length
    Set-Content -LiteralPath $shaPath -Value ("{0}  {1}" -f $hash, [System.IO.Path]::GetFileName($zipPath)) -Encoding ASCII

    Write-Host ('ACTIVE-ZIP: {0}' -f $zipPath)
    Write-Host ('Bytes: {0}' -f $size)
    Write-Host ('SHA-256: {0}' -f $hash)
    exit 0
}
finally {
    if (Test-Path -LiteralPath $stageParent) { Remove-Item -LiteralPath $stageParent -Recurse -Force -ErrorAction SilentlyContinue }
    if (Test-Path -LiteralPath $verifyDir) { Remove-Item -LiteralPath $verifyDir -Recurse -Force -ErrorAction SilentlyContinue }
}
