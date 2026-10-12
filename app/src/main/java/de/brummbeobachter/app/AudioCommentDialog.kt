package de.brummbeobachter.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * One shared comment dialog for observer and Analyzer audio saves.
 * The caller must freeze/finish the audio BEFORE composing this dialog. No microphone/audio
 * operation is started here, so keyboard/tap noise can never extend the selected audio window.
 */
@Composable
internal fun AudioCommentDialog(
    recordingId: String,
    noCommentTimeoutSeconds: Int,
    commentTimeoutSeconds: Int,
    busy: Boolean,
    actionWord: String,
    busyText: String,
    confirmLabel: String,
    withoutCommentLabel: String,
    onInteraction: (Int) -> Unit = {},
    onCommentChanged: (String) -> Unit = {},
    onAutoComplete: (String?, Boolean) -> Unit,
    onConfirm: (String?) -> Unit,
    onWithoutComment: () -> Unit
) {
    var comment by remember(recordingId) { mutableStateOf("") }
    var interacted by remember(recordingId) { mutableStateOf(false) }
    var countdown by remember(recordingId, noCommentTimeoutSeconds) {
        mutableIntStateOf(noCommentTimeoutSeconds)
    }
    val latestAutoComplete = rememberUpdatedState(onAutoComplete)

    LaunchedEffect(recordingId, interacted, comment) {
        val timeout = if (interacted) commentTimeoutSeconds else noCommentTimeoutSeconds
        for (remaining in timeout downTo 1) {
            countdown = remaining
            delay(1_000L)
        }
        val normalized = if (interacted) {
            comment.take(AudioArchive.MAX_COMMENT_LENGTH).takeIf { it.isNotBlank() }
        } else null
        latestAutoComplete.value(normalized, interacted)
    }

    AlertDialog(
        onDismissRequest = {
            // Same policy as observer panel: outside taps must not discard the pending save.
        },
        title = { Text("Kommentar zur Audioaufnahme") },
        text = {
            Column {
                Text(
                    if (interacted) {
                        "$commentTimeoutSeconds s nach der letzten Textänderung wird automatisch $actionWord."
                    } else {
                        "Ohne Kommentareingabe wird nach $noCommentTimeoutSeconds s ohne Kommentar automatisch $actionWord."
                    }
                )
                Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { value ->
                        if (!interacted) onInteraction(commentTimeoutSeconds)
                        interacted = true
                        comment = value.take(AudioArchive.MAX_COMMENT_LENGTH)
                        onCommentChanged(comment)
                    },
                    label = { Text("Kommentar (optional)") },
                    minLines = 3,
                    maxLines = 6,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${comment.length} / ${AudioArchive.MAX_COMMENT_LENGTH}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    if (busy) busyText else "Automatisch in $countdown s",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(comment.takeIf { it.isNotBlank() }) },
                enabled = !busy
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onWithoutComment, enabled = !busy) {
                Text(withoutCommentLabel)
            }
        }
    )
}
