from pathlib import Path
import hashlib, json, sys
root=Path(__file__).resolve().parent
recon=root/'docs'/'reconstruction'
required=[
'00_RECONSTRUCTION_INDEX.md','PROJECT_STATE_CURRENT.md','01_PROJECT_OVERVIEW.md','02_REQUIREMENTS_SRS.md','03_SYSTEM_ARCHITECTURE.md','04_SOURCE_CODE_MAP.md','05_UI_SPECIFICATION.md','06_AUDIO_SYSTEM.md','07_WATERFALL_AND_SPECTRUM.md','08_SEISMIC_SYSTEM.md','09_OBSERVATIONS_AND_RATINGS.md','10_SUPABASE_BACKEND.md','11_SETTINGS_AND_PERSISTENCE.md','12_FILE_FORMATS_AND_NAMING.md','13_BUILD_AND_TOOLCHAIN.md','14_DEPENDENCIES.md','15_TEST_AND_VALIDATION.md','16_KNOWN_ISSUES_AND_LIMITATIONS.md','17_DESIGN_DECISIONS.md','18_VERSION_HISTORY.md','19_REBUILD_FROM_ZERO.md','20_RECOVERY_AND_MIGRATION.md','21_SUPABASE_DISASTER_RECOVERY.md','SOURCE_PROVENANCE.md','RECONSTRUCTION_CHECKLIST.md','project_manifest.json','settings_schema.json','dependency_manifest.json','external_services.json','build_environment.json','file_manifest.sha256']
errors=[]
for name in required:
    if not (recon/name).is_file(): errors.append(f'missing {name}')
for name in ['project_manifest.json','settings_schema.json','dependency_manifest.json','external_services.json','build_environment.json']:
    try: json.loads((recon/name).read_text(encoding='utf-8'))
    except Exception as e: errors.append(f'invalid json {name}: {e}')
# Verify all manifest entries except files added after manifest generation. Manifest is intended to cover project at generation time;
# this verifier itself and any later BUILD_VERIFICATION artifact are intentionally not required in manifest.
manifest=(recon/'file_manifest.sha256').read_text(encoding='utf-8').splitlines()
for line in manifest:
    if not line.strip(): continue
    h, rel=line.split('  ',1)
    p=root/rel
    if not p.is_file(): errors.append(f'manifest missing file {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=h: errors.append(f'hash mismatch {rel}')
# Critical invariants
app=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
if 'applicationId = "de.brummbeobachter.app"' not in app: errors.append('applicationId invariant')
# Source-vs-manifest version and source-priority contract (do not hard-code a past release).
meta=json.loads((recon/'project_manifest.json').read_text(encoding='utf-8'))
name=meta.get('documented_version')
code=meta.get('version_code')
if not isinstance(name,str) or f'versionName = "{name}"' not in app: errors.append('version invariant')
if not isinstance(code,int) or f'versionCode = {code}' not in app: errors.append('versionCode invariant')
idx=(recon/'00_RECONSTRUCTION_INDEX.md').read_text(encoding='utf-8')
if f'Aktueller Quellcode v{name}' not in idx: errors.append('source priority missing')
status=(root/'PROJECT_STATUS.md').read_text(encoding='utf-8') if (root/'PROJECT_STATUS.md').is_file() else ''
if f'v{name}' not in status: errors.append('handoff status/version missing')
print('PASS' if not errors else 'FAIL')
for e in errors: print(e)
sys.exit(1 if errors else 0)
