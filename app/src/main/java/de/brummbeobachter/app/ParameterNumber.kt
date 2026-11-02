package de.brummbeobachter.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

/** DevMode-only red identifier; conversion based on Android xdpi (4 typographic points). */
internal object ParameterNumber {
    @Composable fun Marker(id: Int, developerMode: Boolean) {
        if (!developerMode) return
        val metrics = LocalContext.current.resources.displayMetrics
        // 1 pt = 1/72 inch. Convert physical pixels to sp accounting for font scale.
        val sizeSp = if (metrics.scaledDensity > 0f) {
            (4f * metrics.xdpi / 72f / metrics.scaledDensity).coerceAtLeast(1f)
        } else 4f
        Text(id.toString(), color = Color.Red, fontSize = sizeSp.sp, maxLines = 1)
    }
}
