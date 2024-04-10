package io.github.huupoke12.android.apps.communication.data.models

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat.getActivity
import androidx.core.app.Person
import io.github.huupoke12.android.apps.communication.R
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.data.network.MatrixService
import io.github.huupoke12.android.apps.communication.util.await
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import io.github.huupoke12.android.apps.communication.util.isMuted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.matrix.android.sdk.api.session.events.model.Event
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.model.message.MessageContent
import org.matrix.android.sdk.api.session.sync.model.RoomSync
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


class NotificationService(): Service() {
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private var started = false
    private var me: Person = Person.Builder().setName("You").build()
    private lateinit var matrixService: MatrixService

    override fun onCreate() {
        super.onCreate()
        matrixService = ServiceLocator.matrixService
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!started) {
            matrixService.currentSession.value?.syncService()?.startSync(false)
            sendNotifications()
            started = true
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    private fun sendNotifications() {
        coroutineScope.launch {
            matrixService.currentSession.value?.syncService()?.syncFlow()?.collect { syncResponse ->
                syncResponse.rooms?.join?.forEach {
                    sendRoomNotifications(it.key, it.value)
                }
            }
        }
    }

    private suspend fun sendRoomNotifications(roomId: String, roomSync: RoomSync) {
        val room = matrixService.getRoom(roomId)!!
        if (room.roomPushRuleService().getLiveRoomNotificationState().await().isMuted()) {
            return
        }
        val notificationManager = NotificationManagerCompat.from(this)
        var jitsiCallEventWrapper: Pair<Event, Person>? = null
        var messagingStyle = NotificationCompat.MessagingStyle(me)
            .setConversationTitle(room.roomSummary()!!.displayName)
            .setGroupConversation(!room.roomSummary()!!.isDirect)
        var hasMessage = false

        roomSync.timeline?.events?.forEach { event ->
            val senderItem = event.senderId?.takeIf{ it != matrixService.getMyUserId() }?.let {
                room.membershipService().getRoomMember(it)
            }?.toMatrixItem()
            val senderPerson = senderItem?.let {
                Person.Builder()
                    .setName(it.displayNameOrUsername())
                    .build()
            }
            senderPerson?.let { sendPerson ->
                event.originServerTs?.let { sendTs ->
                    if (event.getClearType() == EventType.MESSAGE &&
                        Clock.System.now() - Instant.fromEpochMilliseconds(sendTs) < 1.hours) {
                        event.getClearContent()?.toModel<MessageContent>()?.let {
                            hasMessage = true
                            messagingStyle = messagingStyle
                                .addMessage(it.body, sendTs, sendPerson)
                        }
                    } else if (event.getClearType() == CustomEventType.Types.JITSI_CALL.matrixName &&
                        Clock.System.now() - Instant.fromEpochMilliseconds(sendTs) < 20.seconds) {
                        if (sendTs > (jitsiCallEventWrapper?.first?.originServerTs ?: 0)) {
                            jitsiCallEventWrapper = event to sendPerson
                        }
                    }
                }
            }
        }
        val notificationBuilder = NotificationCompat.Builder(this, Notification.Channels.MESSAGES.name)
            .setStyle(messagingStyle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        try {
            val lastRoomVisit = ServiceLocator.lastRoomVisit
            if (hasMessage && (lastRoomVisit.first != roomId || Clock.System.now().toEpochMilliseconds() - lastRoomVisit.second > 5000)) {
                notificationManager.notify(
                    Random.nextInt(), notificationBuilder.build()
                )
            }
            jitsiCallEventWrapper?.let {
                it.first.getClearContent()?.toJitsiRoomInfo()?.let { jitsiRoomInfo ->
                    val notificationId = Random.nextInt(0, Int.MAX_VALUE)
                    val notificationCancelIntent = getNotificationCancelIntent(notificationId)
                    val jitsiIntent = ServiceLocator.jitsiService.getIntent(
                        this, ServiceLocator.jitsiService.getOptions(jitsiRoomInfo)
                    )
                    jitsiIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(notificationCancelIntent))
                    val answerPendingIntent = getActivity(this, 0, jitsiIntent, PendingIntent.FLAG_UPDATE_CURRENT, true)!!
                    val declinePendingIntent = notificationCancelIntent
                    val callNotificationBuilder = NotificationCompat.Builder(this, Notification.Channels.CALLS.name)
                        .setStyle(messagingStyle)
                        .setSmallIcon(R.mipmap.ic_launcher)
//                    .setStyle(
//                        NotificationCompat.CallStyle.forIncomingCall(
//                            it.second,
//                            declinePendingIntent,
//                            answerPendingIntent,
//                        )
//                    )
                        .setStyle(
                            NotificationCompat.MessagingStyle(me)
                                .setConversationTitle(room.roomSummary()!!.displayName)
                                .setGroupConversation(!room.roomSummary()!!.isDirect)
                                .addMessage(
                                    "has started a ${jitsiRoomInfo.callType.name.lowercase()} call",
                                    it.first.originServerTs ?: 0,
                                    it.second,
                                )
                        )
                        .addAction(
                            R.drawable.baseline_call_24, "Join call", answerPendingIntent
                        )
                        .addAction(
                            R.drawable.baseline_cancel_24, "Dismiss", declinePendingIntent
                        )
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                    notificationManager.notify(
                        notificationId, callNotificationBuilder.build()
                    )
                }
            }
        } catch (_: SecurityException) {
        }
    }

    private fun rebuildMe() {
        me = Person.Builder()
            .setName("You")
            .build()
    }

    private fun buildPersonFromUser(userItem: MatrixItem.UserItem): Person {
        return Person.Builder()
            .setName(userItem.displayNameOrUsername())
            .build()
    }

    private fun getNotificationCancelIntent(notificationId: Int): PendingIntent {
        val cancelIntent = Intent(this, NotificationCancelReceiver::class.java)
        cancelIntent.putExtra("cancelNotificationId", notificationId)
        return PendingIntent.getBroadcast(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}