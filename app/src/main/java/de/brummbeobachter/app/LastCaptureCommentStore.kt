package de.brummbeobachter.app

import android.content.Context

/**
 * One durable last-used comment for Scout audio, Analyzer rolling audio, and Analyzer screenshots.
 * An automatic timeout marker is for the individual capture only: never store it here.
 */
internal object LastCaptureCommentStore {
    private const val PREFS = "lfn_capture_comment"
    private const val LAST_COMMENT = "last_comment"

    fun read(context: Context): String = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(LAST_COMMENT, "").orEmpty()

    fun save(context: Context, comment: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(LAST_COMMENT, comment).apply()
    }

    fun forTimeout(comment: String, maxLength: Int): String = "${comment.take((maxLength - 1).coerceAtLeast(0))}#"
}
