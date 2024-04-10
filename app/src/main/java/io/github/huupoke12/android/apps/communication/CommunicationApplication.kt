package io.github.huupoke12.android.apps.communication

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.data.models.CustomEventType
import io.github.huupoke12.android.apps.communication.data.models.Notification
import io.github.huupoke12.android.apps.communication.data.network.JitsiService
import io.github.huupoke12.android.apps.communication.data.network.MatrixService
import io.github.huupoke12.android.apps.communication.util.DefaultRoomDisplayNameFallbackProvider
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration

class CommunicationApplication: Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.apply {
            matrixService = MatrixService(
                Matrix(
                    this@CommunicationApplication,
                    MatrixConfiguration(
                        roomDisplayNameFallbackProvider = DefaultRoomDisplayNameFallbackProvider(),
                        customEventTypesProvider = CustomEventType.Provider,
                    )
                )
            )
            jitsiService = JitsiService()
        }

        val messageChannel = NotificationChannelCompat.Builder(
            Notification.Channels.MESSAGES.name, NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName("Messages")
            .build()
        val callChannel = NotificationChannelCompat.Builder(
            Notification.Channels.CALLS.name, NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName("Calls")
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannel(messageChannel)
        notificationManager.createNotificationChannel(callChannel)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}