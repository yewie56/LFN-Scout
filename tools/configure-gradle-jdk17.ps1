# LFN-Scout v0.4.87 – Gradle daemon AND Kotlin/Java toolchain configuration.
# Called from PREPARE_PROJECT.bat; does not edit the global JAVA_HOME or IDE settings.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$cache = Join-Path $root '.tools\java17.path'
$props = Join-Path $root 'gradle.properties'
if (-not (Test-Path -LiteralPath $cache)) { throw 'Missing .tools\java17.path. Run tools\prepare-java17.ps1 first.' }
$jdkHome = (Get-Content -LiteralPath $cache -TotalCount 1).Trim()
if ([string]::IsNullOrWhiteSpace($jdkHome)) { throw 'Java 17 path is empty.' }
$jdkHome = [System.IO.Path]::GetFullPath($jdkHome)
$java = Join-Path $jdkHome 'bin\java.exe'
$javac = Join-Path $jdkHome 'bin\javac.exe'
if (-not (Test-Path -LiteralPath $java) -or -not (Test-Path -LiteralPath $javac)) {
    throw "A complete JDK was not found at: $jdkHome"
}
function Invoke-Version([string]$Executable, [string]$Arguments) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $Executable
    $psi.Arguments = $Arguments
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    $process = [System.Diagnostics.Process]::Start($psi)
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) { throw "Java tool failed: $Executable" }
    return ($stdout + [Environment]::NewLine + $stderr)
}
$reported = Invoke-Version $java '-XshowSettings:properties -version'
if ($reported -notmatch 'java\.version\s*=\s*17(?:[.\s]|$)') {
    throw "The configured Java is not JDK 17: $jdkHome"
}
$version = Invoke-Version $javac '-version'
if ($version -notmatch 'javac\s+17(?:[.\s]|$)') {
    throw "The compiler is not JDK 17: $jdkHome"
}
# Normalize separators so neither Java Properties escapes nor Gradle CSV path parsing
# misinterpret Windows backslashes. The path must not contain a comma.
$portable = $jdkHome.Replace('\','/')
if ($portable.Contains(',')) { throw 'JDK directory may not contain a comma (Gradle toolchain path separator).' }
$lines = if (Test-Path -LiteralPath $props) { @(Get-Content -LiteralPath $props) } else { @() }
$lines = @($lines | Where-Object { $_ -notmatch '^\s*org\.gradle\.java\.(?:home|installations\.paths)\s*[:=]' })
$lines += 'org.gradle.java.home=' + $portable
$lines += 'org.gradle.java.installations.paths=' + $portable
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($props, [string[]]$lines, $utf8)
# Fail if the resulting configuration does not reference the exact discovered JDK.
$actual = @(Get-Content -LiteralPath $props)
foreach ($key in @('org.gradle.java.home', 'org.gradle.java.installations.paths')) {
    $matching = @($actual | Where-Object { $_ -eq ($key + '=' + $portable) })
    if ($matching.Count -ne 1) { throw "JDK property missing or duplicated: $key" }
}
Write-Host '[LFN-Scout] PASS: JDK 17 for Gradle daemon AND analyzer-core JVM toolchain.'
Write-Host ('[LFN-Scout] JDK: ' + $jdkHome)
Write-Host '[LFN-Scout] No global JAVA_HOME or Android Studio user configuration was changed.'
