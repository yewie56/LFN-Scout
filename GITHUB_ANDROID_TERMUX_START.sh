#!/data/data/com.termux/files/usr/bin/bash
# LFN-Scout mobile GitHub publishing, private repository only.
set -euo pipefail
REPO_NAME="${1:-LFN-IN}"
if ! command -v git >/dev/null || ! command -v gh >/dev/null; then
  echo 'Bitte zuerst in Termux: pkg update && pkg install git gh unzip'
  exit 1
fi
if [ ! -d .github/workflows ] || [ ! -f app/build.gradle.kts ]; then
  echo 'Bitte im entpackten Projektverzeichnis starten.'; exit 1
fi
if ! gh auth status >/dev/null 2>&1; then
  echo 'GitHub-Anmeldung wird gestartet. Im Browser die Anmeldung bestaetigen.'
  gh auth login --web --git-protocol https
fi
OWNER="$(gh api user --jq .login)"
git config --global user.name "${OWNER}"
git config --global user.email "${OWNER}@users.noreply.github.com"
if [ ! -d .git ]; then git init -b main; fi
git add .
echo '--- Dateien zum Upload (Auszug) ---'
git diff --cached --name-only | head -40
if git diff --cached --name-only | grep -E '(^supabase/storage_backup|(^|/)\.env($|\.)|\.keystore$|\.jks$|(^|/)local\.properties$)'; then
  echo 'ABBRUCH: verdächtige Datei im Git-Index'; exit 2
fi
git commit -m 'LFN-Scout v0.5.45.8 Android GitHub build' || true
if ! gh repo view "$OWNER/$REPO_NAME" >/dev/null 2>&1; then
  gh repo create "$REPO_NAME" --private --source . --remote origin --push
else
  if ! git remote get-url origin >/dev/null 2>&1; then git remote add origin "https://github.com/$OWNER/$REPO_NAME.git"; fi
  git push -u origin main
fi
echo "FERTIG: https://github.com/$OWNER/$REPO_NAME/actions"
echo 'Unter Actions -> Build LFN-Scout -> Run workflow starten (falls nicht automatisch).'
