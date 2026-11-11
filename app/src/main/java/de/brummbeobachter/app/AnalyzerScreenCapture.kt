package de.brummbeobachter.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Capture the real app window (including Compose controls), never an analyzer re-render.
 * Only usable while the analyzer UI is actually visible. No screen-off fake captures.
 * The app cannot capture Android's separately composited system bars without MediaProjection.
 */
internal object AnalyzerScreenCapture {
    @Volatile private var analyzerActivity: WeakReference<Activity>? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun attach(activity: Activity) { analyzerActivity = WeakReference(activity) }
    fun detach(activity: Activity) {
        if (analyzerActivity?.get() === activity) analyzerActivity = null
    }

    fun capture(): Bitmap? {
        val activity = analyzerActivity?.get() ?: return null
        if (Looper.myLooper() == Looper.getMainLooper()) return drawWindow(activity)
        val latch = CountDownLatch(1)
        var result: Bitmap? = null
        mainHandler.post {
            try { result = if (analyzerActivity?.get() === activity) drawWindow(activity) else null }
            finally { latch.countDown() }
        }
        return if (latch.await(2500, TimeUnit.MILLISECONDS)) result else null
    }

    private fun drawWindow(activity: Activity): Bitmap? {
        val power = activity.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        if (!power.isInteractive || activity.isFinishing || activity.isDestroyed || !activity.window.decorView.isShown) return null
        val view: View = activity.window.decorView
        if (view.width <= 0 || view.height <= 0) return null
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        return bitmap
    }
}
