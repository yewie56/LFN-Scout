param(
    [string]$Revision = 'R1',
    [string]$OutputDirectory = ''
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $root 'Builds'
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$relativeRoots = @(
    'docs/project/analyzer_v0.7/assets',
    'docs/project/analyzer_v0.7/references',
    'docs/project/handoff_assets',
    'docs/project/FILTERBANK_UI_REFERENCE',
    'docs/reconstruction',
    'supabase/storage_backup'
)

$packageName = 'LFN-Scout_BASE_ASSETS_' + $Revision
$zipPath = Join-Path $OutputDirectory ($packageName + '.zip')
$stageParent = Join-Path $OutputDirectory ('.base_stage_' + [guid]::NewGuid().ToString('N'))
$stageRoot = Join-Path $stageParent $packageName
$verifyDir = Join-Path $OutputDirectory ('.base_verify_' + [guid]::NewGuid().ToString('N'))

try {
    New-Item -ItemType Directory -Force -Path $stageRoot | Out-Null
    $entries = New-Object System.Collections.Generic.List[object]

    foreach ($relativeRoot in $relativeRoots) {
        $source = Join-Path $root $relativeRoot
        if (-not (Test-Path -LiteralPath $source)) {
            throw ('BASE-Quelle fehlt: ' + $relativeRoot)
        }
        Get-ChildItem -LiteralPath $source -Recurse -File -Force | ForEach-Object {
            $relative = $_.FullName.Substring($root.Length + 1)
            $target = Join-Path $stageRoot $relative
            New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null
            Copy-Item -LiteralPath $_.FullName -Destination $target -Force
            $entries.Add([ordered]@{
                path = $relative.Replace('\','/')
                bytes = $_.Length
                sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            })
        }
    }

    $readme = @(
        ('LFN-Scout BASE ASSETS ' + $Revision),
        '',
        'Dieses Paket enthaelt grosse, selten veraenderte Referenz-/Recovery-Artefakte.',
        'Es ist NICHT zum Android-Build erforderlich. Das aktuelle ACTIVE-Paket enthaelt den Build-Quellcode.',
        'Bei Bedarf koennen die enthaltenen relativen Pfade in den ACTIVE-Projektordner zurueckkopiert werden.',
        'Das Paket kann private Supabase-Recovery-Daten enthalten und darf nicht oeffentlich verteilt werden.'
    )
    Set-Content -LiteralPath (Join-Path $stageRoot 'README_BASE_ASSETS.txt') -Value $readme -Encoding UTF8

    $manifest = [ordered]@{
        package_type = 'LFN-Scout BASE ASSETS'
        revision = $Revision
        created_at = (Get-Date).ToString('o')
        source_roots = $relativeRoots
        file_count = $entries.Count
        files = $entries
    }
    $manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $stageRoot 'BASE_CONTENT_MANIFEST.json') -Encoding UTF8

    if (Test-Path -LiteralPath $zipPath) { Remove-Item -LiteralPath $zipPath -Force }
    [System.IO.Compression.ZipFile]::CreateFromDirectory($stageParent, $zipPath, [System.IO.Compression.CompressionLevel]::Optimal, $false)
    New-Item -ItemType Directory -Force -Path $verifyDir | Out-Null
    [System.IO.Compression.ZipFile]::ExtractToDirectory($zipPath, $verifyDir)
    if (-not (Test-Path -LiteralPath (Join-Path $verifyDir ($packageName + '\BASE_CONTENT_MANIFEST.json')))) {
        throw 'BASE-ZIP-Verifikation fehlgeschlagen.'
    }

    $hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $size = (Get-Item -LiteralPath $zipPath).Length
    Set-Content -LiteralPath ($zipPath + '.sha256') -Value ("{0}  {1}" -f $hash, [System.IO.Path]::GetFileName($zipPath)) -Encoding ASCII
    Write-Host ('BASE-ZIP: {0}' -f $zipPath)
    Write-Host ('Bytes: {0}' -f $size)
    Write-Host ('SHA-256: {0}' -f $hash)
    exit 0
}
finally {
    if (Test-Path -LiteralPath $stageParent) { Remove-Item -LiteralPath $stageParent -Recurse -Force -ErrorAction SilentlyContinue }
    if (Test-Path -LiteralPath $verifyDir) { Remove-Item -LiteralPath $verifyDir -Recurse -Force -ErrorAction SilentlyContinue }
}
