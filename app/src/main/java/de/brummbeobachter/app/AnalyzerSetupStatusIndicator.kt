package de.brummbeobachter.app

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Live status based on mode pin AND exact on-disk/live comparison; never infer SAVE from a name. */
@Composable
internal fun AnalyzerSetupStatusIndicator(context: Context) {
    var status by remember(context) { mutableStateOf("Setup: wird geprüft") }
    var warning by remember(context) { mutableStateOf(false) }
    LaunchedEffect(context) {
        while (true) {
            val result = AnalyzerSetupLiveStatus.read(context)
            if (status != result.first || warning != result.second) {
                status = result.first
                warning = result.second
            }
            delay(400L)
        }
    }
    Text(status, color = if (warning) Color(0xFFFFC978) else Color(0xFFB6DEE4), fontSize = 12.sp)
}
