package io.github.huupoke12.android.apps.communication.ui.screens

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.data.models.CustomEventType
import io.github.huupoke12.android.apps.communication.data.models.JitsiRoomInfo
import io.github.huupoke12.android.apps.communication.data.models.toContent
import io.github.huupoke12.android.apps.communication.data.models.toJitsiRoomInfo
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.CallType
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import io.github.huupoke12.android.apps.communication.util.getAlphaNumericRandomString
import io.github.huupoke12.android.apps.communication.util.getPreviewText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.api.session.room.model.message.MessageWithAttachmentContent
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem
import java.io.File
import java.util.Timer
import kotlin.concurrent.timerTask

class RoomViewModel(
    private val savedStateHandle: SavedStateHandle,
): ViewModel(), Timeline.Listener {
    private val matrixService = ServiceLocator.matrixService
    private val jitsiService = ServiceLocator.jitsiService
    private val paginateSize = 16
    private val timerPeriod = 5000L
    private val timer = Timer()
    private val timeline: Timeline
    private val roomId: String = savedStateHandle["roomId"]!!
    val myUserId = matrixService.getMyUserId()

    val roomSummaryLive = matrixService.getLiveRoomSummary(roomId)
    val roomMemberSummaryLive = matrixService.getLiveRoomMembers(roomId)
    val uiState = MutableStateFlow(RoomUiState(
        roomAction = try {
            RoomAction.valueOf(savedStateHandle.get<String?>("roomAction").orEmpty())
        } catch (_: IllegalArgumentException) {
            null
        }
    ))

    init {
        val room = matrixService.getRoom(roomId)!!
        timeline = room.timelineService().createTimeline(
            eventId = null,
            settings = TimelineSettings(
                initialSize = paginateSize,
                useLiveSenderInfo = true,
            )
        )
        timeline.addListener(this)
        timeline.start()
        uiState.value = uiState.value.copy(
            timelineEventList = timeline.getSnapshot(),
        )
        timer.scheduleAtFixedRate(
            timerTask {
                ServiceLocator.lastRoomVisit = roomId to Clock.System.now().toEpochMilliseconds()
                matrixService.resendAllFailedMessages(roomId)
            },
            0,
            timerPeriod,
        )
    }

    fun dispose() {
        timer.cancel()
        timeline.removeAllListeners()
        timeline.dispose()
    }

    suspend fun markAsRead() {
        matrixService.markAsRead(roomId)
    }

    fun clearRoomAction() {
        savedStateHandle.set<String?>("roomAction", null)
        uiState.update {
            it.copy(
                roomAction = null,
            )
        }
    }

    fun setComposingMessage(message: String) {
        uiState.value = uiState.value.copy(
            composingMessage = message
        )
        if (message.isNotEmpty()) {
            matrixService.sendTypingIndicator(roomId)
        } else {
            matrixService.sendNotTypingIndicator(roomId)
        }
    }

    fun setAttachments(attachments: List<ContentAttachmentData>) {
        uiState.value = uiState.value.copy(
            attachments = attachments,
        )
    }

    fun removeAttachment(uri: Uri) {
        uiState.value = uiState.value.copy(
            attachments = uiState.value.attachments.filter {
                it.queryUri != uri
            },
        )
    }

    fun loadMore() {
        timeline.paginate(Timeline.Direction.BACKWARDS, paginateSize)
    }

    fun canLoadMore(): Boolean {
        return timeline.hasMoreToLoad(Timeline.Direction.BACKWARDS)
    }

//    fun canSendMessage(): Boolean {
//        return powerLevelsHelper.isUserAllowedToSend(
//            userId = matrixService.getMyUserId(),
//            isState = false,
//            eventType = EventType.MESSAGE,
//        )
//    }

    fun send() = viewModelScope.launch {
        uiState.value.composingMessage.let {
            if (it.isNotEmpty()) {
                matrixService.sendTextMessage(
                    roomId = roomId,
                    text = it
                )
            }
        }
        setComposingMessage("")
        uiState.value.attachments.let {
            if (it.isNotEmpty()) {
                matrixService.sendFiles(
                    roomId = roomId,
                    attachments = uiState.value.attachments,
                )
            }
        }
        setAttachments(emptyList())
        markAsRead()
    }

    fun isMyEvent(senderId: String): Boolean {
        return senderId == matrixService.getMyUserId()
    }

    fun joinRoom() = viewModelScope.launch {
        matrixService.joinRoom(roomId)
    }

    fun leaveRoom() = viewModelScope.launch {
        matrixService.leaveRoom(roomId)
    }

    fun delete(timelineEvent: TimelineEvent) {
//        matrixService.cancelSend(
//            roomId = roomId,
//            eventId = timelineEvent.eventId,
//        )
        matrixService.deleteFailedEcho(
            roomId = roomId,
            localEcho = timelineEvent,
        )
    }

    suspend fun getSharableContentUri(messageContent: MessageWithAttachmentContent): Uri {
        downloadFile(messageContent)
        return matrixService.getTemporarySharableURI(messageContent)!!
    }

    suspend fun downloadFile(messageContent: MessageWithAttachmentContent): File {
        return matrixService.downloadFile(messageContent)
    }

    fun isFileInCache(messageContent: MessageWithAttachmentContent): Boolean {
        return matrixService.isFileInCache(messageContent)
    }

    fun resolveMediaThumbnail(url: String?): String? {
        return matrixService.resolveMediaThumbnail(
            contentUrl = url
        )
    }

    fun createJitsiCall(roomItem: MatrixItem, callType: CallType): JitsiMeetConferenceOptions {
        val directRoomMember = getDirectRoomMember()
        val jitsiRoomInfo = JitsiRoomInfo(
            serverUrl = "https://jitsi.huupoke.com:8443",
            id = getAlphaNumericRandomString(16),
            displayName = directRoomMember?.toMatrixItem()?.displayNameOrUsername() ?: roomItem.displayName.orEmpty(),
            callType = callType,
        )
        matrixService.sendEvent(
            roomId = roomId,
            eventType = CustomEventType.Types.JITSI_CALL.matrixName,
            content = jitsiRoomInfo.toContent()
        )
        return joinJitsiCall(jitsiRoomInfo)
    }

    fun joinJitsiCall(jitsiRoomInfo: JitsiRoomInfo): JitsiMeetConferenceOptions {
        return jitsiService.getOptions(jitsiRoomInfo)
    }


    override fun onNewTimelineEvents(eventIds: List<String>) {

    }

    override fun onStateUpdated(direction: Timeline.Direction, state: Timeline.PaginationState) {

    }

    override fun onTimelineFailure(throwable: Throwable) {

    }

    override fun onTimelineUpdated(snapshot: List<TimelineEvent>) {
        uiState.update { roomUiState ->
            val latestJitsiCall = snapshot.filter {
                it.root.getClearType() == CustomEventType.Types.JITSI_CALL.matrixName
            }.maxByOrNull {
                it.root.originServerTs ?: 0
            }?.let {
                it.root.getClearContent()?.toJitsiRoomInfo()
            }
            roomUiState.copy(
                timelineEventList = snapshot.filter { timelineEvent ->
                    timelineEvent.getPreviewText() != null
                },
                latestJitsiCall = latestJitsiCall,
            )
        }
    }

    fun getDirectRoomMember() = matrixService.getDirectRoomMember(roomId)

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}

data class RoomUiState(
    val roomAction: RoomAction?,
    val composingMessage: String = "",
    val latestJitsiCall: JitsiRoomInfo? = null,
    val attachments: List<ContentAttachmentData> = emptyList(),
    val timelineEventList: List<TimelineEvent> = emptyList(),
)