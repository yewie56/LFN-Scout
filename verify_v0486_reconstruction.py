from pathlib import Path
import json, re, sys
ROOT=Path(__file__).resolve().parent
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
text=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
need('versionCode = 129' in text,'versionCode 125 fehlt')
need('versionName = "0.4.89"' in text,'versionName 0.4.85 fehlt')
required=[
'supabase/schema_full.sql','supabase/data_full.sql','supabase/auth_schema.sql','supabase/storage_schema.sql',
'supabase/roles_original.sql','supabase/roles.sql','supabase/project_config.json','supabase/secret_names.txt',
'docs/reconstruction/21_SUPABASE_DISASTER_RECOVERY.md','supabase/SECRETS_REQUIRED.md']
for r in required:
    p=ROOT/r; need(p.is_file() and p.stat().st_size>0,f'fehlt/leer: {r}')
wavs=list((ROOT/'supabase/storage_backup').rglob('*.wav'))
need(len(wavs)==53,f'WAV-Anzahl {len(wavs)} statt 53')
need(sum(p.stat().st_size for p in wavs)==12722332,'WAV-Gesamtgröße weicht von verifiziertem Stand ab')
funcs={'submit_observation','upload_wav','get_recent_observations','get_radius_histograms','get_all_observations'}
local={p.name for p in (ROOT/'supabase/functions').iterdir() if p.is_dir()}
need(funcs <= local,f'Edge Functions fehlen: {sorted(funcs-local)}')

# v0.4.84 audio-archive regression must remain intact
a=(ROOT/'app/src/main/java/de/brummbeobachter/app/AudioArchive.kt').read_text(encoding='utf-8')
pol=(ROOT/'app/src/main/java/de/brummbeobachter/app/AudioArchiveCompatibilityPolicy.kt').read_text(encoding='utf-8')
need('Download/LFN-Scout/' in pol,'aktueller Audio-Pfad fehlt')
need('Download/BrummBeobachter/' in pol,'Legacy-Audio-Pfad fehlt')
need('findPublishedWavByName' in a,'MediaStore-Dateinamen-Fallback fehlt')
need('ALREADY_ABSENT' in a and 'shouldRemoveMetadataAfterDelete' in a,'idempotente Cleanup-Regel fehlt')
# These files may contain role *names*, but must not contain obvious plaintext token values.
for rel in ['supabase/project_config.json','supabase/secret_names.txt','supabase/SECRETS_REQUIRED.md']:
    t=(ROOT/rel).read_text(encoding='utf-8',errors='ignore')
    for pat in [r'eyJ[a-zA-Z0-9_-]{20,}\.',r'sb_secret_[A-Za-z0-9_-]{16,}',r'postgres(?:ql)?://[^\s:/]+:[^\s@]+@']:
        need(re.search(pat,t) is None,f'möglicher Klartext-Secret/Token in {rel}')
if errors:
    print('FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('PASS v0.4.89 reconstruction')
print(f'WAV files: {len(wavs)}; bytes: {sum(p.stat().st_size for p in wavs)}')
