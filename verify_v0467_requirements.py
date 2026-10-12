from pathlib import Path
import sys
ROOT=Path(__file__).resolve().parent
checks=[]
def check(name, cond):
    checks.append((name,bool(cond))); print(f"[{'PASS' if cond else 'FAIL'}] {name}")
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
build=text('app/build.gradle.kts')
wave=text('app/src/main/java/de/brummbeobachter/app/SeismicWaveformPolicy.kt')
service=text('app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt')
routing=text('app/src/main/java/de/brummbeobachter/app/EidaRoutingPolicy.kt')
selftest=text('SeismicWaveformSelfTest.kt')
readme=text('README.md'); changelog=text('CHANGELOG.md'); project=text('docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md')
check('versionName 0.4.67', 'versionName = "0.4.67"' in build)
check('versionCode 107', 'versionCode = 107' in build)
check('Steim control nibble indexing fixed', '30 - wordIndex * 2' in wave and '(wordIndex - 1)' not in wave)
check('Steim1 regression fixture corrected', '1 shl 24' in selftest)
check('Steim2 regression fixture corrected', '2 shl 24' in selftest)
check('Steim multi-frame regression present', 'makeMultiFrameSteim1MiniSeed' in selftest and '1 shl 28' in selftest)
check('route-none logs skip', 'SEISMIC_ROUTE_NONE' in service and 'action=skip' in service)
check('route-none immediately continues', '"stream=${streamId(candidate)} action=skip"' in service and service.find('"stream=${streamId(candidate)} action=skip"') < service.find('EidaRoutingPolicy.probeEndpoints'))
check('probe endpoint policy exists', 'fun probeEndpoints' in routing)
check('empty routes return no endpoints', 'if (routes.isEmpty()) return emptyList()' in routing)
check('federator fallback retained after route', 'fallbackEndpoint' in routing)
check('README v0.4.67', '# LFN-Scout Android v0.4.67' in readme and 'Neu in v0.4.67' in readme)
check('CHANGELOG v0.4.67', changelog.startswith('## 0.4.67'))
check('project docs v0.4.67', 'Änderungen in v0.4.67' in project)
failed=[n for n,ok in checks if not ok]
print(f"\n{len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    print('FAILED: '+', '.join(failed)); sys.exit(1)
