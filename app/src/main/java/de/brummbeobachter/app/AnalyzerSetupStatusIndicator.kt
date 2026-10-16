package de.brummbeobachter.app

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

/** Reports repository selection honestly, without claiming that audio START has loaded it. */
@Composable
internal fun AnalyzerSetupStatusIndicator(context: Context) {
    var status by remember(context) { mutableStateOf("Setup: wird geladen") }
    LaunchedEffect(context) {
        while (true) {
            val updated = runCatching {
        val repo = AnalyzerJsonSetupRepository(context.applicationContext)
        val listing = repo.listing()
        val entry = listing.entries.firstOrNull { it.id == listing.activeId }
        if (entry == null) "Setup: keines" else "Setup: ${entry.name} (zugeordnet)"
            }.getOrElse { "Setup: Status nicht lesbar" }
            if (status != updated) status = updated
            delay(400L)
        }
    }
    Text(status, color = Color(0xFFB6DEE4), fontSize = 12.sp)
}
