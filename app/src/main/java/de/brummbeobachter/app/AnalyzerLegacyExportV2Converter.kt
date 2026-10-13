package de.brummbeobachter.app

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Converts an old READ-ONLY v2 inspection copy to a new, INACTIVE setup document.
 * Does not install, select or apply a setup. Missing values stay absent, with provenance.
 * Unsupported/fixed values are retained for archival review but NEVER auto-applied.
 */
internal object AnalyzerLegacyExportV2Converter {
    private const val SCHEMA = "lfn-scout-analyzer-setup-export-v2"
    private const val SOURCE = "lfn_scout_live_analyzer"
    private val statuses = setOf("AVAILABLE", "UNSUPPORTED", "FIXED")
    private val provenanceStatuses = setOf("SAVED_SLOT", "MISSING_IN_SAVED_SLOT")

    data class Conversion(
        val document: JSONObject,
        val missing: List<String>,
        val notApplicable: List<String>,
        val warnings: List<String>
    )

    fun convert(json: String): Conversion = convert(JSONObject(json))

    fun convert(input: JSONObject): Conversion {
        require(input.optString("schema") == SCHEMA) { "Not a v2 inspection export" }
        require(input.optInt("schemaVersion", -1) == 2) { "Unsupported export version" }
        require(input.optBoolean("read_only_copy", false)) { "Expected read-only inspection copy" }
        require(input.optString("source") == SOURCE) { "Unknown settings source" }
        val slot = input.getInt("slot")
        require(slot in 1..4) { "Invalid source slot" }
        val mode = input.getString("mode")
        require(mode == if (slot == 4) "DIRECT" else "8K") { "Slot/mode mismatch" }
        val settings = input.getJSONObject("settings")
        val availability = input.getJSONObject("availability")
        val sourceProvenance = input.getJSONObject("provenance")
        val groups = input.getJSONObject("groups")
        val keys = settings.keys().asSequence().toSet()
        require(keys.isNotEmpty() && keys.all { AnalyzerSetupFormat.isLegacyField(it) }) { "Unknown or empty settings" }
        require(availability.keys().asSequence().toSet() == keys) { "Availability field mismatch" }
        require(sourceProvenance.keys().asSequence().toSet() == keys) { "Provenance field mismatch" }
        val missing = mutableListOf<String>()
        val notApplicable = mutableListOf<String>()
        val legacy = JSONObject()
        for (key in keys.sorted()) {
            val value = settings.get(key)
            val status = availability.getString(key)
            val origin = sourceProvenance.getString(key)
            require(status in statuses && origin in provenanceStatuses) { "Unknown field status: $key" }
            val isMissing = value == JSONObject.NULL
            require(isMissing == (origin == "MISSING_IN_SAVED_SLOT")) { "Missing/value conflict: $key" }
            if (isMissing) missing.add(key) else legacy.put(key, value)
            if (status != "AVAILABLE") notApplicable.add(key)
        }
        // The grouped projection must not contain conflicting values. Exact field-group
        // assignment is not required: original legacy exports might vary in group layout.
        val groupKeys = groups.keys()
        val encountered = mutableSetOf<String>()
        while (groupKeys.hasNext()) {
            val group = groups.getJSONObject(groupKeys.next())
            val memberKeys = group.keys()
            while (memberKeys.hasNext()) {
                val field = memberKeys.next()
                require(field in keys && encountered.add(field)) { "Duplicate/unknown grouped field: $field" }
                require(JSONArray().put(group.get(field)).toString() == JSONArray().put(settings.get(field)).toString()) {
                    "Group/value conflict: $field"
                }
            }
        }
        require(encountered == keys) { "Incomplete group projection" }
        val provenance = JSONObject()
            .put("sourceSlot", slot).put("mode", mode).put("sourceStore", SOURCE)
            .put("missingFields", JSONArray(missing))
            .put("notApplicableFields", JSONArray(notApplicable))
        val parameters = JSONObject().put("legacy", legacy).put("provenance", provenance)
        AnalyzerSetupFormat.validate(parameters)
        val label = input.optString("name").trim().ifEmpty { "Import v2 Slot $slot" }
        val description = input.optString("description")
        val document = JSONObject()
            .put("schema", AnalyzerJsonSetupRepository.SCHEMA)
            .put("schemaVersion", AnalyzerJsonSetupRepository.VERSION)
            .put("id", UUID.randomUUID().toString())
            .put("name", label).put("description", description)
            .put("parameters", parameters)
        val warnings = buildList {
            if (missing.isNotEmpty()) add("${missing.size} fehlende Felder; keine Ersatzwerte erzeugt")
            if (notApplicable.isNotEmpty()) add("${notApplicable.size} FIXED/UNSUPPORTED-Felder; nicht automatisch anwenden")
        }
        return Conversion(document, missing, notApplicable, warnings)
    }
}
