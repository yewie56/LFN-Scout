package de.brummbeobachter.app

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ReminderAlarmActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null

    private val closeReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (
                    intent?.action ==
                    ACTION_CLOSE_REMINDER_FULLSCREEN
                ) {
                    DebugLogger.log(
                        applicationContext,
                        "FULL_SCREEN_ACTIVITY_CLOSE_SIGNAL",
                        "reason=response_window_timeout"
                    )
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareLockScreenPresentation()
        wakeDeviceBriefly()

        DebugLogger.log(
            applicationContext,
            "FULL_SCREEN_ACTIVITY_OPENED"
        )

        ReminderFullscreenController
            .register(
                this
            )

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                closeReceiver,
                IntentFilter(
                    ACTION_CLOSE_REMINDER_FULLSCREEN
                ),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(
                closeReceiver,
                IntentFilter(
                    ACTION_CLOSE_REMINDER_FULLSCREEN
                )
            )
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Text(
                            text = "LFN-SCOUT",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        Text(
                            text = "JETZT BRUMMTON BEWERTEN",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(28.dp)
                        )

                        Button(
                            onClick = {
                                // First action: silence everything.
                                ReminderAlertController.stop(
                                    applicationContext,
                                    "FULLSCREEN_RATE_BUTTON"
                                )

                                openMainApp()
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "BEWERTEN",
                                fontSize = 22.sp
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                // First action: silence everything.
                                ReminderAlertController.stop(
                                    applicationContext,
                                    "FULLSCREEN_PAUSE_1H_BUTTON"
                                )

                                sendBroadcast(
                                    Intent(
                                        this@ReminderAlarmActivity,
                                        ReminderActionReceiver::class.java
                                    ).apply {
                                        action =
                                            ReminderActionReceiver
                                                .ACTION_PAUSE_ONE_HOUR
                                    }
                                )

                                finish()
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Text("1 h pausieren")
                        }
                    }
                }
            }
        }
    }

    private fun prepareLockScreenPresentation() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            val keyguardManager =
                getSystemService(
                    Context.KEYGUARD_SERVICE
                ) as KeyguardManager

            keyguardManager
                .requestDismissKeyguard(
                    this,
                    null
                )
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams
                    .FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams
                        .FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams
                        .FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(
            WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON
        )
    }

    @Suppress("DEPRECATION")
    private fun wakeDeviceBriefly() {
        val powerManager =
            getSystemService(
                Context.POWER_SERVICE
            ) as PowerManager

        wakeLock =
            powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                "LFN-Scout:ReminderWake"
            ).apply {
                acquire(30_000L)
            }
    }

    private fun openMainApp() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

                putExtra(
                    ReminderActionReceiver
                        .EXTRA_FROM_REMINDER,
                    true
                )
            }
        )

        finish()
    }

    override fun onDestroy() {
        /*
         * Activity lifecycle is not a user response. Android may destroy and
         * recreate this full-screen UI while the response window is still open.
         * Therefore do NOT stop ReminderAlertController here: stop() also
         * cancels tone packages 2/3. Button actions still stop explicitly.
         */
        DebugLogger.log(
            applicationContext,
            "FULL_SCREEN_ACTIVITY_DESTROYED",
            "action=release_ui_resources_only"
        )

        ReminderFullscreenController
            .unregister(
                this
            )

        runCatching {
            unregisterReceiver(
                closeReceiver
            )
        }

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        wakeLock = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_CLOSE_REMINDER_FULLSCREEN =
            "de.brummbeobachter.app.action.CLOSE_REMINDER_FULLSCREEN"
    }
}
