package de.brummbeobachter.app

import android.content.Context
import android.content.ContextWrapper
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Real JSON repository roundtrips in cache only. Never uses production analyzer_setups. */
internal object IsolatedSetupSystemTest {
    fun run(context: Context): List<PersistentSystemTest.Result> {
        val results = mutableListOf<PersistentSystemTest.Result>()
        val dir = File(context.cacheDir, "lfn_systemtest_${UUID.randomUUID()}")
        fun record(id: String, ok: Boolean, detail: String) {
            results += PersistentSystemTest.Result(id, if (ok) "PASS" else "FAIL", detail)
        }
        try {
            check(dir.mkdirs()) { "Could not create isolated systemtest directory" }
            val isolated = object : ContextWrapper(context) {
                override fun getFilesDir(): File = dir
            }
            val repository = AnalyzerJsonSetupRepository(isolated)
            val monA = JSONObject(AnalyzerMonitor.snapshotSetupParameters().toString())
            val monB = JSONObject(monA.toString()).put("level",
                if (monA.getDouble("level") == -35.0) -20.0 else -35.0)
            // Exercise the real MON controller's failure path without ever enabling MON
            // or applying a valid draft to productive runtime state. A malformed
            // fixture must be rejected before any parameter is changed.
            val beforeRejectedDraft = AnalyzerMonitor.snapshotSetupParameters().toString()
            val invalidMonDraft = JSONObject(monA.toString()).put("center", -1.0)
            val invalidDraftRejected = runCatching {
                AnalyzerMonitor.applySetupDraft(invalidMonDraft)
            }.isFailure
            record("isolated_mon_controller_rejects_invalid_draft",
                invalidDraftRejected &&
                    AnalyzerMonitor.snapshotSetupParameters().toString() == beforeRejectedDraft,
                "Real MON controller rejected invalid draft with no runtime parameter change; no valid live A/B/A switch claimed")
            // Probe additional real controller rejection paths without activating a
            // valid draft.  Each check must preserve the effective MON state.
            val incompleteDraft = JSONObject(monA.toString()).apply { remove("fade") }
            val beforeIncomplete = AnalyzerMonitor.snapshotSetupParameters().toString()
            val incompleteRejected = runCatching {
                AnalyzerMonitor.applySetupDraft(incompleteDraft)
            }.isFailure
            record("isolated_mon_controller_rejects_incomplete_draft",
                incompleteRejected &&
                    AnalyzerMonitor.snapshotSetupParameters().toString() == beforeIncomplete,
                "Real controller rejects missing MON field without mutating runtime; no live A/B/A claimed")
            val outOfRangeDraft = JSONObject(monA.toString()).put("comp_ratio", 21.0)
            val beforeOutOfRange = AnalyzerMonitor.snapshotSetupParameters().toString()
            val outOfRangeRejected = runCatching {
                AnalyzerMonitor.applySetupDraft(outOfRangeDraft)
            }.isFailure
            record("isolated_mon_controller_rejects_out_of_range_draft",
                outOfRangeRejected &&
                    AnalyzerMonitor.snapshotSetupParameters().toString() == beforeOutOfRange,
                "Real controller rejects out-of-range MON field without mutating runtime; no live A/B/A claimed")
            // Exercise real MON draft acceptance and A/B/A parameter transitions
            // without touching named production setups, preferences, or MON output.
            // The controller guard refuses to run while MON is enabled.
            val beforeProbe = AnalyzerMonitor.snapshotSetupParameters().toString()
            if (AnalyzerMonitor.enabled) {
                results += PersistentSystemTest.Result("isolated_mon_controller_ab_a_probe", "SKIP",
                    "MON is enabled; non-persistent controller probe must not disturb active output")
            } else {
                val probe = runCatching { AnalyzerMonitor.probeSetupDraftRoundtrip(monA, monB) }
                val restored = AnalyzerMonitor.snapshotSetupParameters().toString() == beforeProbe
                record("isolated_mon_controller_ab_a_probe", probe.getOrDefault(false) && restored,
                    "Real MON draft controller accepted A/B/A and original effective parameters restored; " +
                        (probe.exceptionOrNull()?.message ?: "no controller exception"))
            }
            val filterA = JSONObject().put("filter_min_hz", 1).put("filter_max_hz", 100)
                .put("filter_bands", 10)
            val filterB = JSONObject().put("filter_min_hz", 2).put("filter_max_hz", 150)
                .put("filter_bands", 20)
            val a = repository.create("SYSTEMTEST_A", JSONObject().put("monitor", monA).put("legacy", filterA))
            val b = repository.create("SYSTEMTEST_B", JSONObject().put("monitor", monB).put("legacy", filterB))
            val list = repository.listing()
            record("isolated_setup_creation", list.entries.size == 2 && list.activeId == null,
                "Two real versioned JSON setup files created in disposable cache; production repository not opened")
            // Exercise the actual production index rollback API, including the
            // previously unsupported null-active state; do not touch live MON.
            val noSelection = repository.listing()
            repository.select(a.id)
            repository.restoreSelection(noSelection.activeId, noSelection.recent)
            val noSelectionAgain = AnalyzerJsonSetupRepository(isolated).listing()
            record("isolated_rollback_to_no_active_setup",
                noSelectionAgain.activeId == null && noSelectionAgain.recent == noSelection.recent,
                "Real repository index restored to no active setup after failed selection")
            repository.select(a.id)
            repository.select(b.id)
            val beforeRollback = AnalyzerJsonSetupRepository(isolated).listing()
            repository.select(a.id)
            repository.restoreSelection(beforeRollback.activeId, beforeRollback.recent)
            val afterRollback = AnalyzerJsonSetupRepository(isolated).listing()
            record("isolated_rollback_restores_mru",
                afterRollback.activeId == beforeRollback.activeId &&
                afterRollback.recent == beforeRollback.recent,
                "Real repository restores previous active ID and MRU ordering")
            repository.select(a.id)
            repository.select(b.id)
            repository.select(a.id)
            val indexBeforeInvalidRestore = repository.listing()
            val malformedRestoreRejected = runCatching {
                repository.restoreSelection(a.id, listOf(a.id, a.id))
            }.isFailure
            val indexAfterInvalidRestore = AnalyzerJsonSetupRepository(isolated).listing()
            record("isolated_invalid_rollback_keeps_index",
                malformedRestoreRejected &&
                    indexAfterInvalidRestore.activeId == indexBeforeInvalidRestore.activeId &&
                    indexAfterInvalidRestore.recent == indexBeforeInvalidRestore.recent,
                "Invalid duplicate MRU rollback rejected without changing committed index")
            val active = repository.listing()
            record("isolated_setup_mru_switch", active.activeId == a.id && active.recent.take(2) == listOf(a.id, b.id),
                "A→B→A selection; active and MRU persisted to isolated repository index")
            val reloaded = AnalyzerJsonSetupRepository(isolated)
            val savedA = reloaded.load(a.id).getJSONObject("parameters")
            val savedB = reloaded.load(b.id).getJSONObject("parameters")
            val monOk = savedA.getJSONObject("monitor").toString() == monA.toString() &&
                savedB.getJSONObject("monitor").toString() == monB.toString() &&
                savedA.getJSONObject("monitor").getDouble("level") != savedB.getJSONObject("monitor").getDouble("level")
            record("isolated_mon_json_roundtrip", monOk,
                "Distinct MON profiles A/B survived create→switch→repository reopen; live MON never changed")
            val fA = savedA.getJSONObject("legacy")
            val fB = savedB.getJSONObject("legacy")
            val filterOk = fA.toString() == filterA.toString() && fB.toString() == filterB.toString() &&
                fA.getInt("filter_bands") == 10 && fB.getInt("filter_bands") == 20
            record("isolated_filterbank_json_roundtrip", filterOk,
                "Different 10/20-band settings and independent frequency limits persisted in two isolated JSON setups")
            var rejected = false
            try { repository.importNew("{\"schema\":\"invalid\",\"id\":\"broken\"}") }
            catch (_: IllegalArgumentException) { rejected = true }
            catch (_: org.json.JSONException) { rejected = true }
            record("isolated_invalid_setup_rejected", rejected && reloaded.listing().entries.size == 2,
                "Invalid import rejected without modifying the isolated index or setup count")

            // Real atomic-save and persisted A/B isolation: mutate B, then reopen twice.
            val originalA = reloaded.exportCopy(a.id)
            val changedB = JSONObject(reloaded.exportCopy(b.id))
            val changedParams = changedB.getJSONObject("parameters")
            val previousMax = changedParams.getJSONObject("legacy").getInt("filter_max_hz")
            changedParams.getJSONObject("legacy").put("filter_max_hz", previousMax + 7)
            repository.save(changedB)
            val reopened = AnalyzerJsonSetupRepository(isolated)
            val bNewMax = reopened.load(b.id).getJSONObject("parameters")
                .getJSONObject("legacy").getInt("filter_max_hz")
            record("isolated_atomic_save_roundtrip", bNewMax == previousMax + 7,
                "B atomic save persisted through a newly opened repository")
            record("isolated_setup_a_unchanged", reopened.exportCopy(a.id) == originalA,
                "A bytes/JSON export unchanged after saving B")
            record("isolated_active_mru_survives_save", reopened.listing().activeId == a.id &&
                reopened.listing().recent.take(2) == listOf(a.id, b.id),
                "Active A and MRU survived saving inactive B")

            // The real repository's save-before-select contract: persist an edited
            // active A before switching to B, then reopen the repository.  No
            // production files or live controller state are involved.
            repository.select(a.id)
            val activeBeforeSwitch = repository.load(a.id)
            val originalLevel = activeBeforeSwitch.getJSONObject("parameters")
                .getJSONObject("monitor").getDouble("level")
            val changedLevel = if (originalLevel == -29.0) -28.0 else -29.0
            activeBeforeSwitch.getJSONObject("parameters").getJSONObject("monitor")
                .put("level", changedLevel)
            repository.save(activeBeforeSwitch)
            repository.select(b.id)
            val afterSwitch = AnalyzerJsonSetupRepository(isolated)
            record("isolated_save_before_select", afterSwitch.listing().activeId == b.id &&
                afterSwitch.load(a.id).getJSONObject("parameters")
                    .getJSONObject("monitor").getDouble("level") == changedLevel,
                "Edited active A saved before B selection, survives fresh repository instance")

            // Rejected active save must NEVER be followed by selection. This is
            // an isolated fail-closed orchestration check, not a live UI switch.
            repository.select(a.id)
            val beforeRejectedSave = AnalyzerJsonSetupRepository(isolated)
            val unchangedActiveBytes = beforeRejectedSave.exportCopy(a.id)
            val invalidActive = JSONObject(unchangedActiveBytes)
            invalidActive.getJSONObject("parameters").getJSONObject("legacy")
                .put("filter_bands", "invalid")
            val switchResult = runCatching {
                repository.save(invalidActive)
                repository.select(b.id)
            }
            val afterRejectedSave = AnalyzerJsonSetupRepository(isolated)
            record("isolated_failed_save_blocks_switch", switchResult.isFailure &&
                afterRejectedSave.listing().activeId == a.id &&
                afterRejectedSave.exportCopy(a.id) == unchangedActiveBytes,
                "Invalid active save rejected; B not selected; A bytes and active index preserved")

            // Verify the actual persistent A/B/A repository path with a fresh
            // repository instance at *each* selection.  Do not confuse this
            // isolated persistence test with a live controller/MON test.
            val verifiedA = AnalyzerJsonSetupRepository(isolated)
            verifiedA.select(a.id)
            val aMonitorLevel = AnalyzerJsonSetupRepository(isolated).load(a.id)
                .getJSONObject("parameters").getJSONObject("monitor").getDouble("level")
            val bSelection = AnalyzerJsonSetupRepository(isolated)
            bSelection.select(b.id)
            val bMonitorLevel = AnalyzerJsonSetupRepository(isolated).load(b.id)
                .getJSONObject("parameters").getJSONObject("monitor").getDouble("level")
            val finalSelection = AnalyzerJsonSetupRepository(isolated)
            finalSelection.select(a.id)
            val finalRepository = AnalyzerJsonSetupRepository(isolated)
            val finalMonitorLevel = finalRepository.load(a.id)
                .getJSONObject("parameters").getJSONObject("monitor").getDouble("level")
            record("isolated_setup_ab_a_reopen_roundtrip",
                finalRepository.listing().activeId == a.id &&
                finalRepository.listing().recent.take(2) == listOf(a.id, b.id) &&
                aMonitorLevel == changedLevel && finalMonitorLevel == changedLevel &&
                bMonitorLevel != changedLevel,
                "A→B→A selected using fresh repository instances, distinct persisted MON values checked, index/MRU recovered; no live controller changes")

            // Named A/B/A through the production repository selection API AND
            // real MON draft controller. Only disposable cache-backed setup files
            // are selected; runtime draft state is restored by the controller probe.
            // This is deliberately not a UI navigation or active MON recording test.
            val namedBefore = AnalyzerJsonSetupRepository(isolated).listing()
            val monBefore = AnalyzerMonitor.snapshotSetupParameters().toString()
            if (AnalyzerMonitor.enabled) {
                results += PersistentSystemTest.Result("isolated_named_mon_controller_ab_a", "SKIP",
                    "MON output is active; cannot change runtime drafts safely")
            } else {
                val namedResult = runCatching {
                    val stageIds = listOf(a.id, b.id, a.id)
                    val stageValues = stageIds.map { id ->
                        AnalyzerJsonSetupRepository(isolated).load(id)
                            .getJSONObject("parameters").getJSONObject("monitor")
                    }
                    // Freshly read versions of the named documents are used,
                    // including the earlier test's persisted edits.
                    AnalyzerMonitor.probeSetupDraftRoundtrip(stageValues[0], stageValues[1]) { stage ->
                        AnalyzerJsonSetupRepository(isolated).select(stageIds[stage])
                        check(AnalyzerJsonSetupRepository(isolated).listing().activeId == stageIds[stage]) {
                            "Named setup not selected at stage $stage"
                        }
                        check(AnalyzerMonitor.snapshotSetupParameters().toString() == stageValues[stage].toString()) {
                            "MON draft does not match selected named setup at stage $stage"
                        }
                    }
                }
                val repoRestored = runCatching {
                    AnalyzerJsonSetupRepository(isolated).restoreSelection(namedBefore.activeId, namedBefore.recent)
                    AnalyzerJsonSetupRepository(isolated).listing().let {
                        it.activeId == namedBefore.activeId && it.recent == namedBefore.recent
                    }
                }
                record("isolated_named_mon_controller_ab_a",
                    namedResult.getOrDefault(false) && repoRestored.getOrDefault(false) &&
                        AnalyzerMonitor.snapshotSetupParameters().toString() == monBefore,
                    "Named cache-only A/B/A selection with real MON draft controller; runtime and isolated index restored; " +
                        (namedResult.exceptionOrNull()?.message ?: repoRestored.exceptionOrNull()?.message ?: "no exception"))
            }

            // Invalid edits must fail before the existing, valid JSON is replaced.
            val validBytes = reopened.exportCopy(b.id)
            val invalidEdit = JSONObject(validBytes)
            invalidEdit.getJSONObject("parameters").getJSONObject("legacy")
                .put("filter_bands", "not-an-integer")
            val refusedInvalidSave = runCatching { repository.save(invalidEdit) }.isFailure
            record("isolated_invalid_save_preserves_previous", refusedInvalidSave &&
                AnalyzerJsonSetupRepository(isolated).exportCopy(b.id) == validBytes,
                "Invalid edit rejected; last valid setup remains readable and unchanged")
            val beforeInvalidSelect = reopened.listing()
            val refusedMissingSelect = runCatching { repository.select(UUID.randomUUID().toString()) }.isFailure
            val afterInvalidSelect = AnalyzerJsonSetupRepository(isolated).listing()
            record("isolated_missing_selection_preserves_active", refusedMissingSelect &&
                afterInvalidSelect.activeId == beforeInvalidSelect.activeId &&
                afterInvalidSelect.recent == beforeInvalidSelect.recent,
                "Missing setup rejected without modifying active setup or MRU")
        } catch (t: Exception) {
            results += PersistentSystemTest.Result("isolated_fixture_execution", "FAIL", "${t.javaClass.simpleName}: ${t.message}")
        } finally {
            if (dir.exists() && !dir.deleteRecursively()) {
                results += PersistentSystemTest.Result("isolated_fixture_cleanup", "FAIL", "Disposable cache directory could not be deleted")
            } else {
                results += PersistentSystemTest.Result("isolated_fixture_cleanup", "PASS", "Disposable fixture files removed; no production setup changes")
            }
        }
        return results
    }
}
