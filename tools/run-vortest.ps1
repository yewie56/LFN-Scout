param(
    [switch]$Install
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$buildFile = Join-Path $root 'app\build.gradle.kts'
$buildText = Get-Content -LiteralPath $buildFile -Raw
$versionMatch = [regex]::Match($buildText, 'versionName\s*=\s*"([^"]+)"')
if (-not $versionMatch.Success) {
    throw 'versionName konnte nicht aus app/build.gradle.kts gelesen werden.'
}
$version = $versionMatch.Groups[1].Value
$builds = Join-Path $root 'Builds'
New-Item -ItemType Directory -Force -Path $builds | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $builds ("VORTEST_v{0}_{1}.log" -f $version, $stamp)
$result = Join-Path $builds 'LAST_VORTEST_RESULT.txt'
$apkSource = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
$apkTarget = Join-Path $builds ("LFN-Scout_v{0}-debug.apk" -f $version)
$activeZip = Join-Path $builds ("LFN-Scout_ACTIVE_v{0}.zip" -f $version)
$activeSha = $activeZip + '.sha256'
$baseRequirement = Join-Path $root 'BASE_PACKAGE_REQUIRED.txt'
$installStatus = if ($Install) { 'REQUESTED' } else { 'NOT_REQUESTED' }

$stepStatus = [ordered]@{
    'PREPARE'        = 'NOT RUN'
    'UNIT TESTS'     = 'NOT RUN'
    'ASSEMBLE DEBUG' = 'NOT RUN'
    'APK'            = 'NOT RUN'
    'ACTIVE ZIP'     = 'NOT RUN'
    'ADB DEVICE'     = if ($Install) { 'NOT RUN' } else { 'NOT REQUESTED' }
    'INSTALL'        = if ($Install) { 'NOT RUN' } else { 'NOT REQUESTED' }
}

function Write-ScreenAndLog([string]$text = '') {
    Write-Host $text
    Add-Content -LiteralPath $log -Value $text -Encoding UTF8
}

function Write-StepHeader([string]$name) {
    Write-ScreenAndLog ''
    Write-ScreenAndLog ('=== {0} ===' -f $name)
}

function Write-Summary([string]$overall) {
    Write-ScreenAndLog ''
    Write-ScreenAndLog '============================================================'
    Write-ScreenAndLog 'VORTEST-ZUSAMMENFASSUNG'
    Write-ScreenAndLog '============================================================'
    foreach ($entry in $stepStatus.GetEnumerator()) {
        Write-ScreenAndLog ('{0,-16} {1}' -f ($entry.Key + ':'), $entry.Value)
    }
    Write-ScreenAndLog '------------------------------------------------------------'
    Write-ScreenAndLog ('GESAMTERGEBNIS: {0}' -f $overall)
    Write-ScreenAndLog ('Log:        {0}' -f $log)
    Write-ScreenAndLog ('Ergebnis:   {0}' -f $result)
    if (Test-Path -LiteralPath $apkTarget) {
        Write-ScreenAndLog ('APK:        {0}' -f $apkTarget)
    }
    if (Test-Path -LiteralPath $activeZip) {
        Write-ScreenAndLog ('ACTIVE-ZIP: {0}' -f $activeZip)
    }
    Write-ScreenAndLog '============================================================'
}

function Write-Result([string]$status, [int]$exitCode, [string]$step, [string]$detail) {
    $apkLine = if (Test-Path $apkTarget) { $apkTarget } else { 'nicht erzeugt' }
    $activeLine = if (Test-Path $activeZip) { $activeZip } else { 'nicht erzeugt' }
    $baseLine = if (Test-Path $baseRequirement) { ((Get-Content -LiteralPath $baseRequirement | Select-Object -First 1) -join '') } else { 'nicht vorhanden' }
    $lines = @(
        ("LFN-Scout v{0} - Windows-Vortest" -f $version),
        ('Zeit: {0}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
        ('Status: {0}' -f $status),
        ('ExitCode: {0}' -f $exitCode),
        ('Letzter Schritt: {0}' -f $step),
        ('Detail: {0}' -f $detail),
        ('Log: {0}' -f $log),
        ('APK: {0}' -f $apkLine),
        ('ACTIVE-ZIP: {0}' -f $activeLine),
        ('BASE: {0}' -f $baseLine),
        ('Install: {0}' -f $installStatus),
        '',
        'Schrittstatus:'
    )
    foreach ($entry in $stepStatus.GetEnumerator()) {
        $lines += ('  {0}: {1}' -f $entry.Key, $entry.Value)
    }
    Set-Content -LiteralPath $result -Value $lines -Encoding UTF8
}

function Fail-AndExit([int]$exitCode, [string]$step, [string]$detail) {
    Write-ScreenAndLog ''
    Write-ScreenAndLog ('FEHLER {0}: {1}' -f $exitCode, $step)
    Write-ScreenAndLog $detail
    Write-Result 'FAIL' $exitCode $step $detail
    Write-Summary 'FAIL'
    exit $exitCode
}

# Windows PowerShell 5.1 kann normale STDERR-Ausgabe nativer Programme bei 2>&1
# als NativeCommandError behandeln. Deshalb werden native Prozesse ueber
# System.Diagnostics.Process gestartet. Stdout UND stderr werden zeilenweise
# live auf den Bildschirm und gleichzeitig in die Logdatei geschrieben.
# Erfolg/Fehler richtet sich ausschliesslich nach dem echten Prozess-Exitcode.
function ConvertTo-ProcessArgument([string]$arg) {
    if ($null -eq $arg) { return '""' }
    if ($arg -notmatch '[\s"]') { return $arg }
    return '"' + $arg.Replace('"', '\"') + '"'
}

function Invoke-LiveProcess([string]$filePath, [string[]]$arguments) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $filePath
    $psi.Arguments = (($arguments | ForEach-Object { ConvertTo-ProcessArgument $_ }) -join ' ')
    $psi.WorkingDirectory = $root
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi

    Write-ScreenAndLog ('Programm: {0}' -f $filePath)
    if ($arguments.Count -gt 0) {
        Write-ScreenAndLog ('Argumente: {0}' -f $psi.Arguments)
    }

    if (-not $process.Start()) {
        throw ("Prozess konnte nicht gestartet werden: {0}" -f $filePath)
    }

    $stdoutLines = New-Object System.Collections.Generic.List[string]
    $stderrLines = New-Object System.Collections.Generic.List[string]
    $stdoutDone = $false
    $stderrDone = $false
    $stdoutTask = $process.StandardOutput.ReadLineAsync()
    $stderrTask = $process.StandardError.ReadLineAsync()

    while (-not ($process.HasExited -and $stdoutDone -and $stderrDone)) {
        if (-not $stdoutDone -and $stdoutTask.IsCompleted) {
            $line = $stdoutTask.Result
            if ($null -eq $line) {
                $stdoutDone = $true
            } else {
                [void]$stdoutLines.Add([string]$line)
                Write-ScreenAndLog ([string]$line)
                $stdoutTask = $process.StandardOutput.ReadLineAsync()
            }
        }

        if (-not $stderrDone -and $stderrTask.IsCompleted) {
            $line = $stderrTask.Result
            if ($null -eq $line) {
                $stderrDone = $true
            } else {
                [void]$stderrLines.Add([string]$line)
                # STDERR wird bewusst ebenfalls normal auf dem Screen ausgegeben.
                Write-ScreenAndLog ([string]$line)
                $stderrTask = $process.StandardError.ReadLineAsync()
            }
        }

        if (-not ($process.HasExited -and $stdoutDone -and $stderrDone)) {
            Start-Sleep -Milliseconds 20
        }
    }

    $process.WaitForExit()
    $exitCode = $process.ExitCode
    $process.Dispose()

    return [pscustomobject]@{
        ExitCode = [int]$exitCode
        StdOut = [string]($stdoutLines -join "`r`n")
        StdErr = [string]($stderrLines -join "`r`n")
    }
}

function Invoke-ProcessStep([string]$name, [string]$statusKey, [string]$filePath, [string[]]$arguments, [int]$failCode) {
    Write-StepHeader $name
    try {
        $run = Invoke-LiveProcess $filePath $arguments
    }
    catch {
        $stepStatus[$statusKey] = 'FAIL'
        Fail-AndExit $failCode $name $_.Exception.Message
    }
    if ($run.ExitCode -ne 0) {
        $stepStatus[$statusKey] = 'FAIL'
        Fail-AndExit $failCode $name ("Prozess-Endcode {0}" -f $run.ExitCode)
    }
    $stepStatus[$statusKey] = 'PASS'
    Write-ScreenAndLog ('PASS: {0}' -f $name)
    return $run
}

function Find-Adb {
    $candidates = New-Object System.Collections.Generic.List[string]
    $cmd = Get-Command adb.exe -ErrorAction SilentlyContinue
    if ($cmd) { $candidates.Add($cmd.Source) }

    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
    $localProps = Join-Path $root 'local.properties'
    if (Test-Path -LiteralPath $localProps) {
        $line = Get-Content -LiteralPath $localProps | Where-Object { $_ -match '^\s*sdk\.dir\s*=' } | Select-Object -Last 1
        if ($line) {
            $sdk = ($line -replace '^\s*sdk\.dir\s*=\s*','').Trim().Replace('/','\')
            if ($sdk) { $sdkRoots += $sdk }
        }
    }
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        $sdkRoots += (Join-Path $env:LOCALAPPDATA 'Android\Sdk')
    }
    foreach ($sdkRoot in $sdkRoots) {
        if (-not [string]::IsNullOrWhiteSpace($sdkRoot)) {
            $candidate = Join-Path $sdkRoot 'platform-tools\adb.exe'
            if (Test-Path -LiteralPath $candidate) { $candidates.Add($candidate) }
        }
    }
    return ($candidates | Select-Object -Unique | Select-Object -First 1)
}

Set-Content -LiteralPath $log -Value @(
    ("LFN-Scout v{0} - automatischer Windows-Vortest" -f $version),
    ('Start: {0}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
    ('Projekt: {0}' -f $root),
    ('Install angefordert: {0}' -f [bool]$Install),
    'Ausgabe: stdout und stderr live auf Bildschirm UND gleichzeitig ins Log.',
    'Native Prozessauswertung: System.Diagnostics.Process; Exitcode entscheidet.'
) -Encoding UTF8

Write-ScreenAndLog ('LFN-Scout v{0}: Vortest gestartet.' -f $version)
Write-ScreenAndLog ('Logdatei: {0}' -f $log)
Write-ScreenAndLog 'Alle Meldungen von stdout und stderr werden live angezeigt und protokolliert.'

$cmdExe = if ([string]::IsNullOrWhiteSpace($env:ComSpec)) { 'cmd.exe' } else { $env:ComSpec }
Invoke-ProcessStep 'PREPARE_PROJECT' 'PREPARE' $cmdExe @('/d', '/c', 'call PREPARE_PROJECT.bat --no-pause') 10 | Out-Null
Invoke-ProcessStep 'UNIT TESTS' 'UNIT TESTS' $cmdExe @('/d', '/c', 'call gradlew.bat :app:testDebugUnitTest --stacktrace') 20 | Out-Null
Invoke-ProcessStep 'ASSEMBLE DEBUG' 'ASSEMBLE DEBUG' $cmdExe @('/d', '/c', 'call gradlew.bat assembleDebug --stacktrace') 30 | Out-Null

Write-StepHeader 'APK CHECK'
if (-not (Test-Path -LiteralPath $apkSource)) {
    $stepStatus['APK'] = 'FAIL'
    Fail-AndExit 40 'APK CHECK' ("Erwartete APK fehlt: {0}" -f $apkSource)
}
Copy-Item -LiteralPath $apkSource -Destination $apkTarget -Force
$apkHash = (Get-FileHash -LiteralPath $apkTarget -Algorithm SHA256).Hash.ToLowerInvariant()
$apkSize = (Get-Item -LiteralPath $apkTarget).Length
Write-ScreenAndLog ('APK: {0}' -f $apkTarget)
Write-ScreenAndLog ('Bytes: {0}' -f $apkSize)
Write-ScreenAndLog ('SHA-256: {0}' -f $apkHash)
$stepStatus['APK'] = 'PASS'
Write-ScreenAndLog 'PASS: APK CHECK'

Invoke-ProcessStep 'ACTIVE PACKAGE' 'ACTIVE ZIP' 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'tools\package-active.ps1', '-OutputDirectory', $builds) 50 | Out-Null
Write-StepHeader 'ACTIVE PACKAGE CHECK'
if (-not (Test-Path -LiteralPath $activeZip) -or -not (Test-Path -LiteralPath $activeSha)) {
    $stepStatus['ACTIVE ZIP'] = 'FAIL'
    Fail-AndExit 51 'ACTIVE PACKAGE CHECK' 'ACTIVE-ZIP oder SHA-256-Datei fehlt.'
}
$activeHash = (Get-FileHash -LiteralPath $activeZip -Algorithm SHA256).Hash.ToLowerInvariant()
$activeSize = (Get-Item -LiteralPath $activeZip).Length
Write-ScreenAndLog ('ACTIVE-ZIP: {0}' -f $activeZip)
Write-ScreenAndLog ('Bytes: {0}' -f $activeSize)
Write-ScreenAndLog ('SHA-256: {0}' -f $activeHash)
$stepStatus['ACTIVE ZIP'] = 'PASS'
Write-ScreenAndLog 'PASS: ACTIVE PACKAGE CHECK'

if ($Install) {
    Write-StepHeader 'ADB CHECK'
    $adb = Find-Adb
    if (-not $adb) {
        $installStatus = 'FAIL_ADB_NOT_FOUND'
        $stepStatus['ADB DEVICE'] = 'FAIL'
        Fail-AndExit 60 'ADB CHECK' 'adb.exe wurde nicht gefunden. Android SDK Platform-Tools installieren/pruefen.'
    }
    Write-ScreenAndLog ('ADB: {0}' -f $adb)

    try {
        Write-ScreenAndLog 'ADB-Server starten/pruefen ...'
        $adbStart = Invoke-LiveProcess $adb @('start-server')
        if ($adbStart.ExitCode -ne 0) {
            $installStatus = 'FAIL_ADB_START'
            $stepStatus['ADB DEVICE'] = 'FAIL'
            Fail-AndExit 60 'ADB CHECK' ("adb start-server fehlgeschlagen; Endcode {0}" -f $adbStart.ExitCode)
        }
    }
    catch {
        $installStatus = 'FAIL_ADB_EXECUTION'
        $stepStatus['ADB DEVICE'] = 'FAIL'
        Fail-AndExit 60 'ADB CHECK' $_.Exception.Message
    }

    Write-ScreenAndLog ''
    Write-ScreenAndLog 'Warte auf genau ein freigegebenes Android-Geraet ...'
    Write-ScreenAndLog 'USB-Datenkabel anschliessen, Handy entsperren und USB-Debugging bestaetigen.'
    Write-ScreenAndLog 'Abbruch jederzeit mit Ctrl+C.'

    $serial = $null
    $poll = 0
    while ([string]::IsNullOrWhiteSpace($serial)) {
        $poll++
        Write-ScreenAndLog ''
        Write-ScreenAndLog ('ADB-Pruefung #{0} ...' -f $poll)
        try {
            $adbDevices = Invoke-LiveProcess $adb @('devices')
        }
        catch {
            $installStatus = 'FAIL_ADB_EXECUTION'
            $stepStatus['ADB DEVICE'] = 'FAIL'
            Fail-AndExit 60 'ADB CHECK' $_.Exception.Message
        }
        if ($adbDevices.ExitCode -ne 0) {
            $installStatus = 'FAIL_ADB_DEVICES'
            $stepStatus['ADB DEVICE'] = 'FAIL'
            Fail-AndExit 62 'ADB CHECK' ("adb devices fehlgeschlagen; Endcode {0}" -f $adbDevices.ExitCode)
        }

        $deviceLines = @($adbDevices.StdOut -split "`r?`n")
        $devices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device\s*$' })
        $unauthorized = @($deviceLines | Where-Object { $_ -match '^\S+\s+unauthorized\s*$' })
        $offline = @($deviceLines | Where-Object { $_ -match '^\S+\s+offline\s*$' })
        $attached = @($deviceLines | Where-Object { $_ -match '^\S+\s+\S+\s*$' -and $_ -notmatch '^List of devices attached' })

        Write-ScreenAndLog ('Freigegebene ADB-Geraete: {0}' -f $devices.Count)
        Write-ScreenAndLog ('Nicht autorisiert:          {0}' -f $unauthorized.Count)
        Write-ScreenAndLog ('Offline:                    {0}' -f $offline.Count)
        Write-ScreenAndLog ('Insgesamt erkannt:          {0}' -f $attached.Count)

        if ($attached.Count -eq 1 -and $devices.Count -eq 1 -and $unauthorized.Count -eq 0 -and $offline.Count -eq 0) {
            $serial = (($devices[0] -split '\s+')[0]).Trim()
            break
        }

        if ($unauthorized.Count -gt 0) {
            Write-ScreenAndLog 'WARTEN: Handy entsperren und "USB-Debugging zulassen?" bestaetigen.'
        } elseif ($attached.Count -gt 1) {
            Write-ScreenAndLog 'WARTEN: Mehrere ADB-Geraete erkannt. Bitte alle bis auf das Zielgeraet trennen.'
        } elseif ($offline.Count -gt 0) {
            Write-ScreenAndLog 'WARTEN: ADB-Geraet ist offline. USB-Verbindung/Debugging pruefen.'
        } else {
            Write-ScreenAndLog 'WARTEN: Noch kein ADB-Geraet. USB-Datenkabel anschliessen und USB-Debugging aktivieren.'
        }
        Write-ScreenAndLog 'Erneute Pruefung in 2 Sekunden ...'
        Start-Sleep -Seconds 2
    }

    $stepStatus['ADB DEVICE'] = 'PASS'
    Write-ScreenAndLog ('PASS: ADB DEVICE - {0}' -f $serial)

    Write-StepHeader 'ADB INSTALL'
    Write-ScreenAndLog ('Geraet: {0}' -f $serial)
    Write-ScreenAndLog ('APK: {0}' -f $apkTarget)
    try {
        $adbInstall = Invoke-LiveProcess $adb @('-s', $serial, 'install', '-r', $apkTarget)
    }
    catch {
        $installStatus = 'FAIL_INSTALL'
        $stepStatus['INSTALL'] = 'FAIL'
        Fail-AndExit 63 'ADB INSTALL' $_.Exception.Message
    }
    if ($adbInstall.ExitCode -ne 0) {
        $installStatus = 'FAIL_INSTALL'
        $stepStatus['INSTALL'] = 'FAIL'
        Fail-AndExit 63 'ADB INSTALL' ("adb install -r fehlgeschlagen; Geraet {0}; Endcode {1}" -f $serial, $adbInstall.ExitCode)
    }
    $installStatus = 'PASS'
    $stepStatus['INSTALL'] = 'PASS'
    Write-ScreenAndLog ('PASS: APK per ADB auf {0} installiert.' -f $serial)
}

Write-ScreenAndLog ('Ende: {0}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'))
$detail = "APK {0} Bytes SHA-256 {1}; ACTIVE {2} Bytes SHA-256 {3}" -f $apkSize, $apkHash, $activeSize, $activeHash
if ($Install) { $detail += '; ADB-Installation PASS' }
Write-Result 'PASS' 0 $(if ($Install) { 'ADB INSTALL' } else { 'ACTIVE PACKAGE CHECK' }) $detail
Write-Summary 'PASS'
exit 0
