param(
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$toolsDir = Join-Path $projectRoot '.tools'
$cacheFile = Join-Path $toolsDir 'java17.path'
$localCurrent = Join-Path $toolsDir 'jdk17\current'
$androidProjectsRoot = Split-Path $projectRoot -Parent

New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

function Say([string]$Text) {
    if (-not $Quiet) { Write-Host $Text }
}

function Get-JavaMajor([string]$JdkHome) {
    if ([string]::IsNullOrWhiteSpace($JdkHome)) { return $null }
    $java = Join-Path $JdkHome 'bin\java.exe'
    if (-not (Test-Path $java) -or -not (Test-Path (Join-Path $JdkHome 'bin\javac.exe'))) { return $null }

    try {
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = $java
        $psi.Arguments = '-XshowSettings:properties -version'
        $psi.UseShellExecute = $false
        $psi.RedirectStandardOutput = $true
        $psi.RedirectStandardError = $true
        $psi.CreateNoWindow = $true

        $proc = New-Object System.Diagnostics.Process
        $proc.StartInfo = $psi

        if (-not $proc.Start()) { return $null }

        $stdout = $proc.StandardOutput.ReadToEnd()
        $stderr = $proc.StandardError.ReadToEnd()
        $proc.WaitForExit()

        $out = ($stdout + [Environment]::NewLine + $stderr)

        if ($out -match 'java\.version\s*=\s*([0-9]+)') {
            return [int]$Matches[1]
        }
        if ($out -match 'version\s+"([0-9]+)') {
            return [int]$Matches[1]
        }
    } catch {}
    return $null
}

function Try-JavaHome([string]$JdkHome, [string]$Label) {
    if ([string]::IsNullOrWhiteSpace($JdkHome)) { return $null }
    try { $full = [IO.Path]::GetFullPath($JdkHome) } catch { return $null }
    if ((Get-JavaMajor $full) -eq 17) {
        Say ("Gefunden: {0}" -f $Label)
        Say ("  {0}" -f $full)
        return $full
    }
    return $null
}

function Save-JavaHome([string]$JdkHome) {
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($cacheFile, $JdkHome + [Environment]::NewLine, $utf8)
    return $JdkHome
}

function Download-WithProgress([string]$Url, [string]$Destination) {
    Say ''
    Say 'Kein vorhandenes Java 17 gefunden.'
    Say 'JDK 17 wird jetzt einmalig heruntergeladen ...'

    $handler = New-Object System.Net.Http.HttpClientHandler
    $client = New-Object System.Net.Http.HttpClient($handler)
    $client.Timeout = [TimeSpan]::FromMinutes(30)

    try {
        $response = $client.GetAsync(
            $Url,
            [System.Net.Http.HttpCompletionOption]::ResponseHeadersRead
        ).GetAwaiter().GetResult()
        $response.EnsureSuccessStatusCode() | Out-Null

        $total = $response.Content.Headers.ContentLength
        $input = $response.Content.ReadAsStreamAsync().GetAwaiter().GetResult()
        $output = [System.IO.File]::Create($Destination)

        try {
            $buffer = New-Object byte[] (1024 * 1024)
            [long]$done = 0
            $lastPct = -1

            while (($read = $input.Read($buffer, 0, $buffer.Length)) -gt 0) {
                $output.Write($buffer, 0, $read)
                $done += $read

                if ($total -and $total -gt 0) {
                    $pct = [Math]::Min(100, [int](($done * 100) / $total))
                    if ($pct -ne $lastPct) {
                        $lastPct = $pct
                        $filled = [Math]::Min(40, [int][Math]::Floor($pct * 40 / 100))
                        $bar = ('#' * $filled) + ('-' * (40 - $filled))
                        $mbDone = [Math]::Round($done / 1MB, 1)
                        $mbTotal = [Math]::Round($total / 1MB, 1)
                        Write-Host -NoNewline ("`r[{0}] {1,3}%  {2}/{3} MB" -f $bar, $pct, $mbDone, $mbTotal)
                    }
                } else {
                    $mbDone = [Math]::Round($done / 1MB, 1)
                    Write-Host -NoNewline ("`rGeladen: {0} MB" -f $mbDone)
                }
            }
        } finally {
            $output.Dispose()
            $input.Dispose()
        }

        Write-Host ''
        Say 'Download abgeschlossen.'
    } finally {
        $client.Dispose()
        $handler.Dispose()
    }
}

# v0.4.87: Do not prefer a cached path to an old project over current/project-independent JDKs.
# 2) Current project-local JDK
$found = Try-JavaHome $localCurrent 'Java 17 in diesem Projekt'
if ($found) { Save-JavaHome $found; exit 0 }

# 4) Android Studio JBR, but only if it is actually Java 17.
$studioCandidates = @(
    (Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'),
    (Join-Path ${env:ProgramFiles(x86)} 'Android\Android Studio\jbr'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Android Studio\jbr')
) | Where-Object { $_ }

foreach ($candidate in $studioCandidates) {
    $found = Try-JavaHome $candidate 'Android-Studio-JBR (Java 17)'
    if ($found) { Save-JavaHome $found; exit 0 }
}


# 5) JDKs managed by Android Studio / JetBrains.
$managedRoots = @(
    (Join-Path $env:USERPROFILE '.jdks'),
    (Join-Path $env:USERPROFILE '.gradle\jdks')
) | Where-Object { $_ -and (Test-Path $_) }

foreach ($managedRoot in $managedRoots) {
    foreach ($dir in (Get-ChildItem $managedRoot -Directory -ErrorAction SilentlyContinue)) {
        $found = Try-JavaHome $dir.FullName 'von Android Studio/Gradle verwaltetes Java 17'
        if ($found) { Save-JavaHome $found; exit 0 }
    }
}

# 6) Common installed JDK 17 locations.
$installRoots = @(
    (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
    (Join-Path $env:ProgramFiles 'Java'),
    (Join-Path $env:ProgramFiles 'Microsoft')
) | Where-Object { $_ -and (Test-Path $_) }

foreach ($installRoot in $installRoots) {
    foreach ($dir in (Get-ChildItem $installRoot -Directory -ErrorAction SilentlyContinue)) {
        $found = Try-JavaHome $dir.FullName ('installiertes Java 17')
        if ($found) { Save-JavaHome $found; exit 0 }
    }
}

# 7) JAVA_HOME only when it is Java 17 (never change global JAVA_HOME).
$found = Try-JavaHome $env:JAVA_HOME 'JAVA_HOME (bereits Java 17)'
if ($found) { Save-JavaHome $found; exit 0 }

# 1) Previously cached JDK
if (Test-Path $cacheFile) {
    $cached = (Get-Content $cacheFile -ErrorAction SilentlyContinue | Select-Object -First 1)
    $found = Try-JavaHome $cached 'bereits gespeichertes Java 17'
    if ($found) { Save-JavaHome $found; exit 0 }
}

# 3) Other AndroidStudioProjects, newest first.
Say 'Suche vorhandenes Java 17 in AndroidStudioProjects ...'
if (Test-Path $androidProjectsRoot) {
    $siblings = Get-ChildItem -Path $androidProjectsRoot -Directory -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending

    foreach ($dir in $siblings) {
        if ($dir.FullName -eq $projectRoot) { continue }

        $candidate = Join-Path $dir.FullName '.tools\jdk17\current'
        $found = Try-JavaHome $candidate ("anderes Projekt: " + $dir.Name)
        if ($found) { Save-JavaHome $found; exit 0 }

        $jdkRoot = Join-Path $dir.FullName '.tools\jdk17'
        if (Test-Path $jdkRoot) {
            foreach ($sub in (Get-ChildItem $jdkRoot -Directory -ErrorAction SilentlyContinue)) {
                $found = Try-JavaHome $sub.FullName ("anderes Projekt: " + $dir.Name)
                if ($found) { Save-JavaHome $found; exit 0 }
            }
        }
    }
}

# 8) Last resort: download project-local Temurin 17.
$jdkRoot = Join-Path $toolsDir 'jdk17'
$extractDir = Join-Path $jdkRoot 'extract'
$zipFile = Join-Path $toolsDir 'temurin17.zip'
$url = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk'

if (Test-Path $extractDir) { Remove-Item $extractDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $extractDir | Out-Null

Download-WithProgress -Url $url -Destination $zipFile

Say 'JDK 17 wird entpackt ...'
Expand-Archive -Path $zipFile -DestinationPath $extractDir -Force
Remove-Item $zipFile -Force

$jdk = Get-ChildItem $extractDir -Directory |
    Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
    Select-Object -First 1

if (-not $jdk) {
    throw 'Entpacktes JDK 17 wurde nicht gefunden.'
}

if (Test-Path $localCurrent) { Remove-Item $localCurrent -Recurse -Force }
New-Item -ItemType Directory -Force -Path (Split-Path $localCurrent -Parent) | Out-Null
Move-Item -Path $jdk.FullName -Destination $localCurrent
Remove-Item $extractDir -Recurse -Force

$found = Try-JavaHome $localCurrent 'neu geladenes projektlokales Java 17'
if (-not $found) { throw 'Heruntergeladenes Java ist nicht Java 17.' }

Save-JavaHome $found | Out-Null
Say 'Java 17 ist bereit.'
exit 0
