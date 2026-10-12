from pathlib import Path
import sys
ROOT=Path(__file__).resolve().parent
checks=[]
def check(name, cond):
    checks.append((name,bool(cond))); print(f"[{'PASS' if cond else 'FAIL'}] {name}")

def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
build=text('app/build.gradle.kts')
service=text('app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt')
search=text('app/src/main/java/de/brummbeobachter/app/SeismicSearchPolicy.kt')
routing=text('app/src/main/java/de/brummbeobachter/app/EidaRoutingPolicy.kt')
main=text('app/src/main/java/de/brummbeobachter/app/MainActivity.kt')
day=text('app/src/main/java/de/brummbeobachter/app/SeismicDayPolicy.kt')
readme=text('README.md'); changelog=text('CHANGELOG.md'); project=text('docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md')
check('versionName 0.4.66', 'versionName = "0.4.66"' in build)
check('versionCode 106', 'versionCode = 106' in build)
check('BGR direct station source', 'eida.bgr.de/fdsnws/station/1/query' in search)
check('GEOFON direct station source', 'geofon.gfz.de/fdsnws/station/1/query' in search)
check('federator not primary search source', 'federator.orfeus-eu.org' not in search)
check('federator fallback retained', 'SEISMIC_CHANNEL_FALLBACK' in service and 'FEDERATOR_STATION_URL' in service)
check('direct source OK log', 'SEISMIC_CHANNEL_SOURCE_OK' in service)
check('direct ready log', 'SEISMIC_CHANNEL_DIRECT_READY' in service)
check('routing after candidate search', 'fetchRoutes(candidate' in service)
check('short probe policy', 'PROBE_SECONDS = 120L' in search)
check('fresh data lag policy', 'FRESH_DATA_LAG_SECONDS = 300L' in search)
check('probe start log', 'SEISMIC_PROBE_START' in service)
check('probe success log', 'SEISMIC_PROBE_OK' in service)
check('probe precedes full URL', service.find('SEISMIC_PROBE_START') < service.find('val fullUrl = buildDataUrl'))
check('endpoint stripping supported', 'endpointOnly' in routing)
check('candidate cap 12', 'MAX_WAVEFORM_CANDIDATES = 12' in service)
check('today local day retained', 'if (isToday) now.atZone(zoneId)' in day)
check('past local full day retained', 'date.plusDays(1).atStartOfDay(zoneId)' in day)
check('date picker retained', 'android.app.DatePickerDialog' in main)
check('previous arrow retained', 'Text("◀"' in main)
check('next arrow retained', 'Text("▶"' in main)
check('Heute retained', 'Text("Heute")' in main)
check('README v0.4.66', '# LFN-Scout Android v0.4.66' in readme)
check('CHANGELOG v0.4.66', changelog.startswith('## 0.4.66'))
check('project docs v0.4.66', 'Änderungen in v0.4.66' in project)
failed=[n for n,ok in checks if not ok]
print(f"\n{len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    print('FAILED: '+', '.join(failed)); sys.exit(1)
