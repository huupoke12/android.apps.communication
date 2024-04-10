package io.github.huupoke12.android.apps.communication.data.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationCancelReceiver(): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId: Int = intent?.getIntExtra("cancelNotificationId", 0) ?: 0
        if (context != null && notificationId != 0) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}