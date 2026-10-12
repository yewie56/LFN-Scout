from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
checks = []

def check(name, cond):
    checks.append((name, bool(cond)))
    print(f"[{'PASS' if cond else 'FAIL'}] {name}")

build = (ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
service = (ROOT/'app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt').read_text(encoding='utf-8')
main = (ROOT/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
day = (ROOT/'app/src/main/java/de/brummbeobachter/app/SeismicDayPolicy.kt').read_text(encoding='utf-8')
routing = (ROOT/'app/src/main/java/de/brummbeobachter/app/EidaRoutingPolicy.kt').read_text(encoding='utf-8')
changelog = (ROOT/'CHANGELOG.md').read_text(encoding='utf-8')
readme = (ROOT/'README.md').read_text(encoding='utf-8')
project_doc = (ROOT/'docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md').read_text(encoding='utf-8')

check('versionName 0.4.65', 'versionName = "0.4.65"' in build)
check('versionCode 105', 'versionCode = 105' in build)
check('EIDA Federator station endpoint', 'federator.orfeus-eu.org/fdsnws/station/1/query' in service)
check('EIDA routing endpoint', 'eidaws/routing/1/query' in service)
check('old fixed availability prerequisite removed', 'AVAILABILITY_URL' not in service and 'hasCurrentAvailability' not in service)
check('selected date enters service', 'selectedDate: LocalDate' in service)
check('local day policy uses atStartOfDay', 'atStartOfDay(zoneId)' in day)
check('today ends at now', 'if (isToday) now.atZone(zoneId)' in day)
check('past day ends next local midnight', 'date.plusDays(1).atStartOfDay(zoneId)' in day)
check('future dates rejected', 'Future seismic dates are not allowed' in day)
check('routing GET parser exists', 'parseGetResponse' in routing)
check('seismic date state in UI', 'seismicSelectedDate' in main)
check('date picker exists', 'android.app.DatePickerDialog' in main)
check('previous arrow exists', 'Text("◀"' in main)
check('next arrow exists', 'Text("▶"' in main)
check('Heute button exists', 'Text("Heute")' in main)
check('today plot label 00:00 to now', 'Tagesplot von 00:00 lokaler Zeit bis jetzt.' in main)
check('past plot label full day', 'Tagesplot von 00:00 bis 24:00 lokaler Zeit.' in main)
check('SEISMIC_DATE_SELECTED log', 'SEISMIC_DATE_SELECTED' in service)
check('SEISMIC_WINDOW log', 'SEISMIC_WINDOW' in service)
check('SEISMIC_ROUTE_OK log', 'SEISMIC_ROUTE_OK' in service)
check('SEISMIC_ROUTE_NONE log', 'SEISMIC_ROUTE_NONE' in service)
check('README v0.4.65', '# LFN-Scout Android v0.4.65' in readme)
check('CHANGELOG v0.4.65', '## 0.4.65' in changelog)
check('project docs v0.4.65', 'Änderungen in v0.4.65' in project_doc)

failed=[name for name,ok in checks if not ok]
print(f"\n{len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    print('FAILED: ' + ', '.join(failed))
    sys.exit(1)
