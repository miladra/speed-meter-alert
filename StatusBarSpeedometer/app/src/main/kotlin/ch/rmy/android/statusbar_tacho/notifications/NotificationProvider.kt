package ch.rmy.android.statusbar_tacho.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi

import ch.rmy.android.statusbar_tacho.R
import ch.rmy.android.statusbar_tacho.activities.SettingsActivity
import ch.rmy.android.statusbar_tacho.receivers.DismissReceiver

class NotificationProvider(context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val builder: Notification.Builder

    init {
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val contentIntent = Intent(context, SettingsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val deleteIntent = Intent(context, DismissReceiver::class.java)
            .setAction(DismissReceiver.DISMISS_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If a channel already exists but doesn't allow vibration or has lower-than-default importance,
            // delete and recreate it so we can enable vibration and the desired importance.
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel != null) {
                if (!existingChannel.shouldVibrate() || existingChannel.importance < NotificationManager.IMPORTANCE_DEFAULT) {
                    notificationManager.deleteNotificationChannel(CHANNEL_ID)
                }
            }
            notificationManager.createNotificationChannel(createChannel(context))
        }
        val turnOffPendingIntent = PendingIntent.getBroadcast(context, 1, deleteIntent, pendingIntentFlags)

        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(R.drawable.icon_unknown)
            .setContentTitle(context.getString(R.string.current_speed))
            .setContentText(context.getString(R.string.unknown))
            .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, pendingIntentFlags))
            .setDeleteIntent(turnOffPendingIntent)
            .setLocalOnly(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    it.addAction(
                        Notification.Action.Builder(
                            null,
                            context.getString(R.string.notification_action_turn_off),
                            turnOffPendingIntent,
                        )
                            .build()
                    )
                } else {
                    it
                }
            }
            .let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    it.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                } else {
                    it
                }
            }
            // On pre-O devices, set a vibration pattern on the notification itself. On O+ the channel controls vibration.
            .let {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    it.setVibrate(VIBRATION_PATTERN)
                } else {
                    it
                }
            }
    }

    fun getInitialNotification(): Notification =
        builder.build()

    fun updateNotification(message: String, @DrawableRes smallIcon: Int) {
        val notification = builder
            .setContentText(message)
            .setSmallIcon(smallIcon)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context): NotificationChannel =
        NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel),
            // Use DEFAULT importance so vibration is allowed by the channel
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .apply {
                enableLights(false)
                enableVibration(true)
                // Provide a vibration pattern (wait, vibrate, wait, vibrate)
                setVibrationPattern(VIBRATION_PATTERN)
                setShowBadge(false)
            }

    companion object {

        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "notification"

        // Vibration pattern used for pre-O notifications and the channel on O+
        private val VIBRATION_PATTERN = longArrayOf(0L, 250L, 100L, 250L)

    }

}
