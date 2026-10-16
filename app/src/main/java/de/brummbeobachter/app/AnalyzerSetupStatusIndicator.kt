package de.brummbeobachter.app

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

/** Reports repository selection honestly, without claiming that audio START has loaded it. */
@Composable
internal fun AnalyzerSetupStatusIndicator(context: Context) {
    val status = runCatching {
        val repo = AnalyzerJsonSetupRepository(context.applicationContext)
        val listing = repo.listing()
        val entry = listing.entries.firstOrNull { it.id == listing.activeId }
        if (entry == null) "Setup: keines" else "Setup: ${entry.name} (zugeordnet)"
    }.getOrElse { "Setup: Status nicht lesbar" }
    Text(status, color = Color(0xFFB6DEE4), fontSize = 12.sp)
}
