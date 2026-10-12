param(
    [string]$ZipPath,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2.0

function Write-Step([string]$Text) { Write-Host "`n==> $Text" -ForegroundColor Cyan }
function Write-Pass([string]$Text) { Write-Host "[PASS] $Text" -ForegroundColor Green }
function Write-Warn([string]$Text) { Write-Host "[WARN] $Text" -ForegroundColor Yellow }
function Fail([string]$Text) { throw $Text }
function Expand-Env([string]$Text) { [Environment]::ExpandEnvironmentVariables($Text) }
function Run-Git([string[]]$Args) {
    & git -C $script:WorkDir @Args
    if ($LASTEXITCODE -ne 0) { Fail "git $($Args -join ' ') fehlgeschlagen (Exit $LASTEXITCODE)." }
}
function Rollback-WorkingTree {
    Write-Warn 'Aenderungen werden auf den Stand vor dem Update zurueckgesetzt.'
    & git -C $script:WorkDir reset --hard $script:BeforeHead | Out-Host
    & git -C $script:WorkDir clean -fd -e Builds/ | Out-Host
}

Write-Host '============================================================' -ForegroundColor DarkCyan
Write-Host ' LFN-Scout - Windows Updater' -ForegroundColor Cyan
Write-Host '============================================================' -ForegroundColor DarkCyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$settingsFile = Join-Path $scriptDir 'settings.json'
if (-not (Test-Path $settingsFile)) { Fail "settings.json fehlt: $settingsFile" }
$cfg = Get-Content -LiteralPath $settingsFile -Raw -Encoding UTF8 | ConvertFrom-Json

$script:WorkDir = Expand-Env $cfg.workDirectory
$downloads = Expand-Env $cfg.downloadsDirectory
$artifactDir = Expand-Env $cfg.artifactDownloadDirectory
$branch = [string]$cfg.branch
$repo = [string]$cfg.repository
$repoSlug = [string]$cfg.repositorySlug
$workflowFile = [string]$cfg.workflowFile

Write-Step 'Voraussetzungen pruefen'
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Fail "Git fehlt. Installiere es z.B. mit: winget install --id Git.Git -e"
}
Write-Pass 'Git gefunden.'
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Fail "GitHub CLI (gh) fehlt. Installiere sie mit: winget install --id GitHub.cli -e"
}
Write-Pass 'GitHub CLI gefunden.'
& gh auth status *> $null
if ($LASTEXITCODE -ne 0) { Fail "GitHub CLI ist nicht angemeldet. Einmal ausfuehren: gh auth login" }
Write-Pass 'GitHub CLI ist angemeldet.'

Write-Step 'Lokales Repository vorbereiten'
if (-not (Test-Path $script:WorkDir)) {
    $parent = Split-Path -Parent $script:WorkDir
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
    & git clone $repo $script:WorkDir
    if ($LASTEXITCODE -ne 0) { Fail 'git clone fehlgeschlagen.' }
    Write-Pass "Repository wurde nach $script:WorkDir geklont."
}
if (-not (Test-Path (Join-Path $script:WorkDir '.git'))) {
    Fail "$script:WorkDir ist kein Git-Repository (.git fehlt). Verschiebe/loesche den Ordner oder passe workDirectory an."
}

$statusBefore = (& git -C $script:WorkDir status --porcelain) -join "`n"
if ($statusBefore.Trim()) {
    Write-Host $statusBefore
    Fail 'Das Repository enthaelt lokale Aenderungen. Bitte zuerst committen, verwerfen oder sichern. Der Updater bricht absichtlich ab.'
}
Run-Git @('checkout', $branch)
Run-Git @('pull', '--ff-only', 'origin', $branch)
$script:BeforeHead = (& git -C $script:WorkDir rev-parse HEAD).Trim()
Write-Pass "Repository ist sauber und aktuell ($branch)."

Write-Step 'Update-ZIP bestimmen'
if (-not $ZipPath) {
    if (-not (Test-Path $downloads)) { Fail "Download-Ordner fehlt: $downloads" }
    $candidate = Get-ChildItem -LiteralPath $downloads -Filter ([string]$cfg.zipPattern) -File |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $candidate) { Fail "Keine passende ZIP in $downloads gefunden: $($cfg.zipPattern)" }
    $ZipPath = $candidate.FullName
}
$ZipPath = (Resolve-Path -LiteralPath $ZipPath).Path
Write-Pass "ZIP: $ZipPath"

$temp = Join-Path ([IO.Path]::GetTempPath()) ("LFN-IN-update-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $temp | Out-Null
try {
    Expand-Archive -LiteralPath $ZipPath -DestinationPath $temp -Force
    $sourceRoot = $temp
    if (-not (Test-Path (Join-Path $sourceRoot 'app\build.gradle.kts'))) {
        $roots = @(Get-ChildItem -LiteralPath $temp -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'app\build.gradle.kts') })
        if ($roots.Count -ne 1) { Fail 'Projektwurzel in der ZIP konnte nicht eindeutig erkannt werden.' }
        $sourceRoot = $roots[0].FullName
    }
    foreach ($required in @('app\build.gradle.kts','settings.gradle.kts','build.gradle.kts','gradlew','gradlew.bat','.github\workflows\build-android.yml')) {
        if (-not (Test-Path (Join-Path $sourceRoot $required))) { Fail "ZIP unvollstaendig; fehlt: $required" }
    }
    $buildText = Get-Content -LiteralPath (Join-Path $sourceRoot 'app\build.gradle.kts') -Raw
    $m = [regex]::Match($buildText, 'versionName\s*=\s*"([^"]+)"')
    if (-not $m.Success) { Fail 'versionName konnte aus app/build.gradle.kts nicht gelesen werden.' }
    $newVersion = $m.Groups[1].Value
    Write-Pass "Neue Version erkannt: $newVersion"

    Write-Step 'Projektdateien einspielen'
    $tracked = @(& git -C $script:WorkDir ls-files)
    foreach ($rel in $tracked) {
        $relWin = $rel -replace '/', '\'
        if ($rel -like 'Builds/*') { continue }
        if (-not (Test-Path (Join-Path $sourceRoot $relWin))) {
            $dest = Join-Path $script:WorkDir $relWin
            if (Test-Path $dest) { Remove-Item -LiteralPath $dest -Force -Recurse }
        }
    }

    $robocopyArgs = @($sourceRoot, $script:WorkDir, '/E', '/XD', '.git', 'Builds', '.gradle', '.idea', '.tools', '/XF', 'local.properties', '/R:2', '/W:1', '/NFL', '/NDL', '/NJH', '/NJS', '/NP')
    & robocopy @robocopyArgs | Out-Null
    if ($LASTEXITCODE -gt 7) { Fail "robocopy fehlgeschlagen (Exit $LASTEXITCODE)." }

    $changes = (& git -C $script:WorkDir status --short) -join "`n"
    if (-not $changes.Trim()) {
        Write-Warn 'Keine Aenderungen gefunden. Es gibt nichts hochzuladen.'
        exit 0
    }
    Write-Host "`nGeplante Git-Aenderungen:" -ForegroundColor White
    Write-Host $changes
    Write-Host "`nAktueller Commit: $script:BeforeHead"
    Write-Host "Neue Version:     $newVersion"
    Write-Host 'Geschuetzt:       .git\ und Builds\'

    if ($DryRun) {
        Write-Warn 'Dry-Run: Kein Commit und kein Push.'
        Rollback-WorkingTree
        exit 0
    }

    $answer = Read-Host 'Aenderungen committen und zu GitHub pushen? [J/N]'
    if ($answer -notmatch '^[JjYy]') {
        Rollback-WorkingTree
        Write-Warn 'Update abgebrochen.'
        exit 2
    }

    Write-Step 'Commit und Push'
    & git -C $script:WorkDir add -A
    if ($LASTEXITCODE -ne 0) { Fail 'git add fehlgeschlagen.' }
    $name = (& git -C $script:WorkDir config user.name).Trim()
    $email = (& git -C $script:WorkDir config user.email).Trim()
    if (-not $name) {
        $name = Read-Host 'Git user.name fehlt. Bitte Namen eingeben (z.B. yewie56)'
        & git -C $script:WorkDir config user.name $name
    }
    if (-not $email) {
        $email = Read-Host 'Git user.email fehlt. Bitte E-Mail oder GitHub-Noreply-Adresse eingeben'
        & git -C $script:WorkDir config user.email $email
    }
    $commitMessage = "LFN-Scout v$newVersion"
    & git -C $script:WorkDir commit -m $commitMessage
    if ($LASTEXITCODE -ne 0) { Fail 'git commit fehlgeschlagen.' }
    & git -C $script:WorkDir push origin $branch
    if ($LASTEXITCODE -ne 0) { Fail 'git push fehlgeschlagen.' }
    $pushedSha = (& git -C $script:WorkDir rev-parse HEAD).Trim()
    Write-Pass "Push abgeschlossen: $pushedSha"

    if (-not [bool]$cfg.waitForGithubBuild) {
        Write-Pass 'Update hochgeladen. Warten auf GitHub Actions ist laut settings.json deaktiviert.'
        exit 0
    }

    Write-Step 'Passenden GitHub-Actions-Lauf suchen'
    $run = $null
    for ($i=0; $i -lt 30 -and -not $run; $i++) {
        Start-Sleep -Seconds 2
        $json = & gh run list --repo $repoSlug --workflow $workflowFile --branch $branch --limit 10 --json databaseId,headSha,status,conclusion
        if ($LASTEXITCODE -ne 0) { Fail 'gh run list fehlgeschlagen.' }
        $runs = $json | ConvertFrom-Json
        $run = $runs | Where-Object { $_.headSha -eq $pushedSha } | Select-Object -First 1
    }
    if (-not $run) { Fail 'Der GitHub-Actions-Lauf fuer den neuen Commit wurde nicht innerhalb von 60 Sekunden gefunden.' }
    Write-Pass "Workflow-Lauf gefunden: ID $($run.databaseId)"

    Write-Step 'GitHub-Build beobachten'
    & gh run watch $run.databaseId --repo $repoSlug --exit-status
    if ($LASTEXITCODE -ne 0) {
        Write-Warn "GitHub-Build ist fehlgeschlagen. Details: gh run view $($run.databaseId) --repo $repoSlug --log-failed"
        exit 10
    }
    Write-Pass 'GitHub Tests und Build erfolgreich.'

    if ([bool]$cfg.storeApkInRepository) {
        Write-Step 'APK-Commit aus dem Repository holen (Variante B)'
        Start-Sleep -Seconds 2
        Run-Git @('pull', '--ff-only', 'origin', $branch)
        $apk = Join-Path $script:WorkDir ("Builds\debug\LFN-Scout-v{0}-debug.apk" -f $newVersion)
        if (Test-Path $apk) {
            Write-Pass "APK lokal verfuegbar: $apk"
        } else {
            Write-Warn "Build war erfolgreich, aber die erwartete APK wurde noch nicht in Builds\debug gefunden: $apk"
        }
    } elseif ([bool]$cfg.downloadArtifactWhenNotStored) {
        Write-Step 'Artifact herunterladen (Variante A)'
        New-Item -ItemType Directory -Force -Path $artifactDir | Out-Null
        $artifactName = "LFN-Scout-v$newVersion-debug"
        & gh run download $run.databaseId --repo $repoSlug --name $artifactName --dir $artifactDir
        if ($LASTEXITCODE -ne 0) { Fail 'Artifact-Download fehlgeschlagen.' }
        Write-Pass "Artifact heruntergeladen nach: $artifactDir"
    }

    Write-Host "`n============================================================" -ForegroundColor DarkCyan
    Write-Host " UPDATE UND BUILD ABGESCHLOSSEN: v$newVersion" -ForegroundColor Green
    Write-Host "============================================================" -ForegroundColor DarkCyan
}
finally {
    if (Test-Path $temp) { Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue }
}
