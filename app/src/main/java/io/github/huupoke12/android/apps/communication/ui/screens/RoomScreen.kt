package io.github.huupoke12.android.apps.communication.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.huupoke12.android.apps.communication.R
import io.github.huupoke12.android.apps.communication.data.models.CustomEventType
import io.github.huupoke12.android.apps.communication.data.models.JitsiRoomInfo
import io.github.huupoke12.android.apps.communication.data.models.toJitsiRoomInfo
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.Centered
import io.github.huupoke12.android.apps.communication.ui.components.DividerWithContent
import io.github.huupoke12.android.apps.communication.ui.components.IconText
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.ReverseLayout
import io.github.huupoke12.android.apps.communication.ui.components.RoomAvatar
import io.github.huupoke12.android.apps.communication.ui.components.UserAvatar
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.CallType
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import io.github.huupoke12.android.apps.communication.util.formattedHourMinute
import io.github.huupoke12.android.apps.communication.util.getPreviewText
import io.github.huupoke12.android.apps.communication.util.mimeToIcon
import io.github.huupoke12.android.apps.communication.util.toSystemLocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.api.session.events.model.Content
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.LocalEcho
import org.matrix.android.sdk.api.session.events.model.getMsgType
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.model.message.MessageAudioContent
import org.matrix.android.sdk.api.session.room.model.message.MessageContent
import org.matrix.android.sdk.api.session.room.model.message.MessageFileContent
import org.matrix.android.sdk.api.session.room.model.message.MessageImageContent
import org.matrix.android.sdk.api.session.room.model.message.MessageTextContent
import org.matrix.android.sdk.api.session.room.model.message.MessageType
import org.matrix.android.sdk.api.session.room.model.message.MessageVideoContent
import org.matrix.android.sdk.api.session.room.model.message.MessageWithAttachmentContent
import org.matrix.android.sdk.api.session.room.model.message.getFileName
import org.matrix.android.sdk.api.session.room.sender.SenderInfo
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.util.MimeTypes
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeAudio
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeImage
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeVideo
import org.matrix.android.sdk.api.util.toMatrixItem
import java.io.File

enum class RoomAction {
    CALL, VIDEO_CALL
}

enum class CallAction {
    CREATE, JOIN, ASK
}

@Composable
fun RoomScreen(
    navigateBack: () -> Unit,
    navigateToPreviewScreen: (File) -> Unit,
    navigateToRoomDetailScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    roomViewModel: RoomViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by roomViewModel.uiState.collectAsStateWithLifecycle()
    val roomSummaryState by roomViewModel.roomSummaryLive.observeAsState()
    val roomMemberSummaryState by roomViewModel.roomMemberSummaryLive.observeAsState()

    var callAction: CallAction? by rememberSaveable {
        if (uiState.roomAction != null) mutableStateOf(CallAction.CREATE) else mutableStateOf(null)
    }
    var callType by rememberSaveable {
        if (uiState.roomAction == RoomAction.VIDEO_CALL) mutableStateOf(CallType.VIDEO) else mutableStateOf(
            CallType.VOICE
        )
    }
    LaunchedEffect(null) {
        roomViewModel.clearRoomAction()
    }
    roomSummaryState?.let { roomSummaryWrapper ->
        roomSummaryWrapper.getOrNull()?.let { roomSummary ->
            val isJoined = roomSummary.membership == Membership.JOIN
            Scaffold(
                modifier = modifier,
                topBar = {
                    RoomTopBar(
                        roomSummary = roomSummary,
                        navigateBack = navigateBack,
                        navigateToRoomDetailScreen = navigateToRoomDetailScreen,
                        resolveAvatar = roomViewModel::resolveAvatar,
                        directRoomMember = roomViewModel.getDirectRoomMember(),
                        onJoinRoom = { roomViewModel.joinRoom() },
                        onLeaveRoom = { roomViewModel.leaveRoom() },
                        onMakeCall = {
                            callAction = CallAction.ASK
                            callType = CallType.VOICE
                        },
                        onMakeVideoCall = {
                            callAction = CallAction.ASK
                            callType = CallType.VIDEO
                        },
                    )
                },
                bottomBar = {
                    Column {
                        if (roomSummary.typingUsers.isNotEmpty()) {
                            Text(
                                text = roomSummary.typingUsers.joinToString(
                                    separator = ", ",
                                    postfix = " is typing ...",
                                ) {
                                    it.disambiguatedDisplayName
                                },
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (isJoined) {
                            RoomBottomBar(
                                message = uiState.composingMessage,
                                onMessageChange = roomViewModel::setComposingMessage,
                                sendMessage = roomViewModel::send,
                                attachments = uiState.attachments,
                                onAttachFiles = roomViewModel::setAttachments,
                                onRemoveFile = roomViewModel::removeAttachment,
                            )
                        }
                    }
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    callAction?.let { currentCallAction ->
                        CallChecker(
                            callAction = currentCallAction,
                            onSetCallAction = { setCallAction -> callAction = setCallAction },
                            onDismiss = { callAction = null },
                            onCreateJitsiCall = {
                                roomViewModel.createJitsiCall(
                                    roomItem = roomSummary.toMatrixItem(),
                                    callType = callType
                                )
                            },
                            joinJitsiCall = roomViewModel::joinJitsiCall,
                            isAlone = roomMemberSummaryState?.none {
                                it.membership == Membership.JOIN && it.userId != roomViewModel.myUserId
                            } ?: true,
                            latestJitsiCall = uiState.latestJitsiCall,
                        )
                    }
                    TimelineEventListing(
                        timelineEventList = uiState.timelineEventList,
                        isMyEventCheck = roomViewModel::isMyEvent,
                        navigateToPreviewScreen = navigateToPreviewScreen,
                        onDelete = roomViewModel::delete,
                        onOpenFile = roomViewModel::getSharableContentUri,
                        onSaveFile = roomViewModel::downloadFile,
                        onLoadMore = roomViewModel::loadMore,
                        onMarkAsRead = roomViewModel::markAsRead,
                        isFileInCache = roomViewModel::isFileInCache,
                        canLoadMoreCheck = roomViewModel::canLoadMore,
                        resolveMediaThumbnail = roomViewModel::resolveMediaThumbnail,
                        resolveAvatar = roomViewModel::resolveAvatar,
                        joinJitsiCall = roomViewModel::joinJitsiCall,
                    )
                }
            }
        } ?: run {
            Centered {
                Text(
                    text = "Room does not exist",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    } ?: run {
        LoadingContainer()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomTopBar(
    roomSummary: RoomSummary,
    navigateBack: () -> Unit,
    navigateToRoomDetailScreen: (String) -> Unit,
    resolveAvatar: (String, AvatarSize) -> String?,
    directRoomMember: RoomMemberSummary?,
    onJoinRoom: () -> Unit,
    onLeaveRoom: () -> Unit,
    onMakeCall: () -> Unit,
    onMakeVideoCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isJoined = roomSummary.membership == Membership.JOIN
    Column(
        modifier = modifier,
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoomAvatar(
                        roomItem = roomSummary.toMatrixItem(),
                        resolveAvatar = resolveAvatar,
                        directUserItem = directRoomMember?.toMatrixItem(),
                        modifier = Modifier.padding(12.dp),
                    )
                    Text(
                        roomSummary.displayName,
                        maxLines = 1,
                    )
                }

            },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onMakeCall,
                    enabled = isJoined,
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call",
                    )
                }
                IconButton(
                    onClick = onMakeVideoCall,
                    enabled = isJoined,
                ) {
                    Icon(
                        Icons.Default.VideoCall,
                        contentDescription = "Video call",
                    )
                }
                IconButton(onClick = {
                    navigateToRoomDetailScreen(roomSummary.roomId)
                }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "View room detail",
                    )
                }
            }
        )
        if (roomSummary.membership == Membership.INVITE) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = CenterHorizontally,
            ) {
                roomSummary.inviterId?.let {
                    Text(
                        text = "You have been invited by $it",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = { onJoinRoom() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                    ) {
                        Text("Join")
                    }
                    Button(
                        onClick = { onLeaveRoom() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    ) {
                        Text("Leave")
                    }
                }
            }
        }
    }
}

@Composable
fun RoomBottomBar(
    message: String,
    onMessageChange: (String) -> Unit,
    attachments: List<ContentAttachmentData>,
    onAttachFiles: (List<ContentAttachmentData>) -> Unit,
    onRemoveFile: (Uri) -> Unit,
    sendMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        if (attachments.isNotEmpty()) {
            AttachedFilesPreview(
                attachments = attachments,
                onRemoveFile = onRemoveFile,
            )
        }
        BottomAppBar(
        ) {
            OutlinedTextField(
                value = message,
                placeholder = { Text("Type a message") },
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f, true),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                )
            )
            val context = LocalContext.current
            val filesPickerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetMultipleContents()
            ) { uris ->
                onAttachFiles(
                    uris.map { uri ->
                        var fileName: String? = null
                        val fileMimeType = context.contentResolver.getType(uri)
                        var fileWidth = 0L
                        var fileHeight = 0L
                        val fileType: ContentAttachmentData.Type = fileMimeType?.let {
                            if (it.isMimeTypeImage()) {
                                val options = BitmapFactory.Options().apply {
                                    inJustDecodeBounds = true
                                }
                                BitmapFactory.decodeStream(
                                    context.contentResolver.openInputStream(uri),
                                    null,
                                    options,
                                )
                                fileWidth = options.outWidth.toLong()
                                fileHeight = options.outHeight.toLong()
                                ContentAttachmentData.Type.IMAGE
                            } else if (it.isMimeTypeVideo()) {
                                ContentAttachmentData.Type.VIDEO
                            } else if (it.isMimeTypeAudio()) {
                                ContentAttachmentData.Type.AUDIO
                            } else {
                                ContentAttachmentData.Type.FILE
                            }
                        } ?: ContentAttachmentData.Type.FILE
                        context.contentResolver.query(
                            uri, null, null, null, null
                        )?.apply {
                            moveToFirst()
                            fileName =
                                getString(getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                            close()
                        }
                        ContentAttachmentData(
                            width = fileWidth,
                            height = fileHeight,
                            queryUri = uri,
                            mimeType = fileMimeType,
                            type = fileType,
                            name = fileName,
                        )
                    }
                )
            }
            IconButton(
                onClick = {
                    filesPickerLauncher.launch(MimeTypes.Any)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach files",
                )
            }
            IconButton(
                onClick = {
                    filesPickerLauncher.launch(MimeTypes.Images)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Attach images",
                )
            }
            IconButton(
                onClick = sendMessage,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                enabled = message.isNotBlank() || attachments.isNotEmpty(),
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send message",
                )
            }
        }
    }
}

@Composable
fun TimelineEventListing(
    timelineEventList: List<TimelineEvent>,
    isMyEventCheck: (String) -> Boolean,
    navigateToPreviewScreen: (File) -> Unit,
    resolveAvatar: (String, AvatarSize) -> String?,
    onMarkAsRead: suspend () -> Unit,
    joinJitsiCall: (JitsiRoomInfo) -> JitsiMeetConferenceOptions,
    onLoadMore: () -> Unit,
    onDelete: (TimelineEvent) -> Unit,
    onOpenFile: suspend (MessageWithAttachmentContent) -> Uri,
    onSaveFile: suspend (MessageWithAttachmentContent) -> File,
    isFileInCache: (MessageWithAttachmentContent) -> Boolean,
    canLoadMoreCheck: () -> Boolean,
    resolveMediaThumbnail: (String?) -> String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (timelineEventList.isNotEmpty()) {
        val listState = rememberLazyListState()
        listState.canScrollForward.let {
            if (!it && canLoadMoreCheck()) {
                LoadingContainer(
                    modifier = Modifier.height(64.dp)
                )
                onLoadMore()
            }
        }
        listState.canScrollBackward.let {
            LaunchedEffect(it) {
                if (!it) {
                    onMarkAsRead()
                }
            }
        }
        Column(
            modifier = modifier,
        ) {
            LazyColumn(
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                reverseLayout = true,
            ) {
                itemsIndexed(timelineEventList) { index, timelineEvent ->
                    val eventLocalDateTime = Instant
                        .fromEpochMilliseconds(timelineEvent.root.originServerTs ?: 0)
                        .toSystemLocalDateTime()
                    val nextEventLocalDateTime = if (index != timelineEventList.lastIndex) Instant
                        .fromEpochMilliseconds(
                            timelineEventList[index + 1].root.originServerTs ?: 0
                        )
                        .toSystemLocalDateTime() else null
                    val displayTime = eventLocalDateTime.time.formattedHourMinute()
                    val eventType = timelineEvent.root.getClearType()
                    val content = timelineEvent.root.getClearContent()
                    val isMyEvent = isMyEventCheck(timelineEvent.senderInfo.userId)
                    when (eventType) {
                        EventType.MESSAGE -> MessageOverlay(
                            content = content!!,
                            type = timelineEvent.root.getMsgType()!!,
                            navigateToPreviewScreen = navigateToPreviewScreen,
                            resolveAvatar = resolveAvatar,
                            onOpenFile = onOpenFile,
                            onSaveFile = onSaveFile,
                            onDelete = { onDelete(timelineEvent) },
                            isFileInCache = isFileInCache,
                            isMyMessage = isMyEvent,
                            isLocalEcho = LocalEcho.isLocalEchoId(timelineEvent.eventId),
                            resolveMediaThumbnail = resolveMediaThumbnail,
                            senderInfo = timelineEvent.senderInfo,
                            sendTime = displayTime,
                        )
                        CustomEventType.Types.JITSI_CALL.matrixName -> {
                            val jitsiRoomInfo = content?.toJitsiRoomInfo()
                            CallOverlay(
                                text = timelineEvent.getPreviewText(
                                    isSenderMyself = isMyEvent
                                ) ?: "null",
                                isMyCall = isMyEvent,
                                isLocalEcho = LocalEcho.isLocalEchoId(timelineEvent.eventId),
                                onJoinCall = {
                                    jitsiRoomInfo?.let {
                                        JitsiMeetActivity.launch(context, joinJitsiCall(it))
                                    }
                                },
                                resolveAvatar = resolveAvatar,
                                senderInfo = timelineEvent.senderInfo,
                                sendTime = displayTime,
                                callType = jitsiRoomInfo?.callType ?: CallType.VOICE
                            )
                        }
                        else -> timelineEvent.getPreviewText()?.let {
                            GenericEvent(
                                body = it,
                            )
                        }
                    }
                    if (eventLocalDateTime.date != nextEventLocalDateTime?.date) {
                        DividerWithContent {
                            Text(
                                text = eventLocalDateTime.date.toString(),
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Center,
        ) {
            Text(
                text = "Nothing to display"
            )
        }
    }
}

@Composable
fun GenericEvent(
    body: String,
    modifier: Modifier = Modifier,
    time: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = CenterHorizontally,
    ) {
        time?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun SenderHighlightEvent(
    isMyEvent: Boolean,
    resolveAvatar: (String, AvatarSize) -> String?,
    isLocalEcho: Boolean,
    senderInfo: SenderInfo,
    sendTime: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val avatarSize = AvatarSize.VERY_SMALL
    val padding =  if (isMyEvent) 0.dp else (1.5 * avatarSize.size).dp
    ReverseLayout(
        reverse = isMyEvent,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!isMyEvent) {
                Text(
                    text = senderInfo.toMatrixItem().displayNameOrUsername(),
                    modifier = Modifier.padding(start = padding),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isMyEvent) {
                    UserAvatar(
                        userItem = senderInfo.toMatrixItem(),
                        resolveAvatar = resolveAvatar,
                        avatarSize = avatarSize,
                    )
                }
                ReverseLayout(
                    reverse = isMyEvent,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(
                                if (isMyEvent) MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = if (isLocalEcho) 0.75f else 1f
                                )
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        content()
                    }
                }
            }
            Text(
                text = if (!isLocalEcho) sendTime else "Sending",
                modifier = Modifier.padding(start = padding),
                fontStyle = if (!isLocalEcho) null else FontStyle.Italic,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}


@Composable
fun CallOverlay(
    text: String,
    onJoinCall: (CallType) -> Unit,
    callType: CallType,
    resolveAvatar: (String, AvatarSize) -> String?,
    isMyCall: Boolean,
    isLocalEcho: Boolean,
    senderInfo: SenderInfo,
    sendTime: String,
    modifier: Modifier = Modifier
) {
    SenderHighlightEvent(
        isMyEvent = isMyCall,
        resolveAvatar = resolveAvatar,
        isLocalEcho = isLocalEcho,
        senderInfo = senderInfo,
        sendTime = sendTime,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.width(IntrinsicSize.Max),
        ) {
            IconText(
                imageVector = if (callType == CallType.VIDEO) Icons.Default.VideoCall else Icons.Default.Call,
                text = text,
            )
            TextButton(
                onClick = { onJoinCall(callType) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Join call",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageOverlay(
    content: Content,
    type: String,
    navigateToPreviewScreen: (File) -> Unit,
    onOpenFile: suspend (MessageWithAttachmentContent) -> Uri,
    onSaveFile: suspend (MessageWithAttachmentContent) -> File,
    onDelete: (() -> Unit)?,
    resolveAvatar: (String, AvatarSize) -> String?,
    isFileInCache: (MessageWithAttachmentContent) -> Boolean,
    resolveMediaThumbnail: (String?) -> String?,
    isMyMessage: Boolean,
    isLocalEcho: Boolean,
    senderInfo: SenderInfo,
    sendTime: String,
    modifier: Modifier = Modifier
) {
    SenderHighlightEvent(
        isMyEvent = isMyMessage,
        resolveAvatar = resolveAvatar,
        isLocalEcho = isLocalEcho,
        senderInfo = senderInfo,
        sendTime = sendTime,
        modifier = modifier,
    ) {
        if (!isLocalEcho) {
            MessageContent(
                content = content,
                type = type,
                navigateToPreviewScreen = navigateToPreviewScreen,
                onOpenFile = onOpenFile,
                onSaveFile = onSaveFile,
                isFileInCache = isFileInCache,
                resolveMediaThumbnail = resolveMediaThumbnail,
            )
        } else {
            var isActionSheetVisible by rememberSaveable {
                mutableStateOf(false)
            }
            val messageContent = content.toModel<MessageContent>()!!.body
            Column(
                modifier = Modifier
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            isActionSheetVisible = true
                        }
                    ),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    text = messageContent,
                    fontStyle = FontStyle.Italic,
                )
            }
            if (isActionSheetVisible) {
                val clipboardManager = LocalClipboardManager.current
                val context = LocalContext.current
                MessageActionSheet(
                    onDismiss = { isActionSheetVisible = false },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(messageContent))
                        Toast.makeText(
                            context,
                            R.string.clipboard_copied,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onDelete = {
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageContent(
    content: Content,
    type: String,
    navigateToPreviewScreen: (File) -> Unit,
    onOpenFile: suspend (MessageWithAttachmentContent) -> Uri,
    onSaveFile: suspend (MessageWithAttachmentContent) -> File,
    isFileInCache: (MessageWithAttachmentContent) -> Boolean,
    resolveMediaThumbnail: (String?) -> String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val typedContent: MessageContent = when (type) {
        MessageType.MSGTYPE_TEXT -> content.toModel<MessageTextContent>()!!
        MessageType.MSGTYPE_FILE -> content.toModel<MessageFileContent>()!!
        MessageType.MSGTYPE_IMAGE -> content.toModel<MessageImageContent>()!!
        MessageType.MSGTYPE_VIDEO -> content.toModel<MessageVideoContent>()!!
        MessageType.MSGTYPE_AUDIO -> content.toModel<MessageAudioContent>()!!
        else -> content.toModel<MessageContent>()!!
    }
    var onOpen: () -> Unit = {}
    var onSave: (() -> Unit)? = null
    var isActionSheetVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val openActionSheet = {
        isActionSheetVisible = true
    }
    var savedFile: File? by rememberSaveable {
        mutableStateOf(null)
    }
    if (typedContent is MessageWithAttachmentContent) {
        val fileDownloadLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(
                mimeType = typedContent.mimeType ?: "*/*"
            )
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val file = onSaveFile(typedContent)
                    savedFile = file
                    context.contentResolver.openOutputStream(uri)!!.apply {
                        write(file.readBytes())
                        close()
                    }
                }
            }
        }
        onOpen = {
            coroutineScope.launch {
                savedFile = onSaveFile(typedContent)
                navigateToPreviewScreen(savedFile!!)
            }
            Unit
        }
        onSave = { fileDownloadLauncher.launch(typedContent.getFileName()) }
    }
    Column(
        modifier = modifier
            .combinedClickable(
                onClick = onOpen,
                onLongClick = openActionSheet,
            ),
        horizontalAlignment = CenterHorizontally,
    ) {
        if (typedContent is MessageWithAttachmentContent) {
            if (savedFile == null && isFileInCache(typedContent)) {
                LaunchedEffect(null) {
                    savedFile = onSaveFile(typedContent)
                }
            }
            when (typedContent) {
                is MessageImageContent -> {
                    val imageModel = savedFile ?: resolveMediaThumbnail(typedContent.info?.thumbnailUrl)
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = typedContent.getFileName(),
                        )
                    } else {
                        LaunchedEffect(null) {
                            savedFile = onSaveFile(typedContent)
                        }
                        CircularProgressIndicator()
                    }
                }

                is MessageVideoContent -> {
                    val imageModel = savedFile ?: resolveMediaThumbnail(typedContent.videoInfo?.thumbnailUrl)
                    if (imageModel != null) {
                        Box(
                            contentAlignment = Center,
                        ) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = typedContent.getFileName(),
                            )
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play video",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    } else {
                        IconText(
                            imageVector = Icons.Default.AttachFile,
                            text = typedContent.getFileName(),
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }

                is MessageAudioContent -> IconText(
                    imageVector = Icons.Default.AudioFile,
                    text = typedContent.getFileName(),
                    textDecoration = TextDecoration.Underline,
                )

                else -> IconText(
                    imageVector = Icons.Default.AttachFile,
                    text = typedContent.getFileName(),
                    textDecoration = TextDecoration.Underline,
                )
            }
        } else {
            when (typedContent) {
                else -> Text(
                    text = typedContent.body,
                )
            }
        }

        if (isActionSheetVisible) {
            val clipboardManager = LocalClipboardManager.current
            MessageActionSheet(
                onDismiss = { isActionSheetVisible = false },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(typedContent.body))
                    Toast.makeText(context, R.string.clipboard_copied, Toast.LENGTH_SHORT).show()
                },
                onSave = onSave,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionSheet(
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onSave: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text("Copy") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                )
            },
            modifier = Modifier.clickable(
                onClick = { onDismiss(); onCopy() },
            )
        )
        onSave?.let {
            ListItem(
                headlineContent = { Text("Save") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(
                    onClick = { onDismiss(); it() },
                )
            )
        }
        onDelete?.let {
            ListItem(
                headlineContent = {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                modifier = Modifier.clickable(
                    onClick = { onDismiss(); it() },
                )
            )
        }
        Spacer(
            modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
        )
    }
}

@Composable
fun AttachedFilesPreview(
    attachments: List<ContentAttachmentData>,
    onRemoveFile: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 192.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        itemsIndexed(attachments) { index, attachment ->
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .padding(4.dp),
                horizontalAlignment = CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Center,
                ) {
                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Center,
                    ) {
                        if (attachment.mimeType.isMimeTypeImage() || attachment.mimeType.isMimeTypeVideo()) {
                            AsyncImage(
                                model = attachment.queryUri,
                                contentDescription = "Attached media",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Icon(
                                imageVector = mimeToIcon(attachment.mimeType),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        if (attachment.mimeType.isMimeTypeVideo()) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }

                    IconButton(
                        onClick = { onRemoveFile(attachment.queryUri) },
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Close file",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
                Text(
                    text = attachment.name ?: "Attachment #${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun CallChecker(
    callAction: CallAction,
    onSetCallAction: (CallAction) -> Unit,
    onDismiss: () -> Unit,
    isAlone: Boolean,
    latestJitsiCall: JitsiRoomInfo?,
    onCreateJitsiCall: () -> JitsiMeetConferenceOptions,
    joinJitsiCall: (JitsiRoomInfo) -> JitsiMeetConferenceOptions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (isAlone) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onDismiss,
                ) {
                    Text("Close")
                }
            },
            title = {
                Text("Cannot call")
            },
            text = {
                Text("At least 2 people is required to call. Invite others and wait for them to join.")
            }
        )
    } else {
        when (callAction) {
            CallAction.CREATE -> {
                LaunchedEffect(null) {
                    JitsiMeetActivity.launch(context, onCreateJitsiCall())
                    onDismiss()
                }
            }
            CallAction.JOIN -> {
                LaunchedEffect(null) {
                    latestJitsiCall?.let {
                        JitsiMeetActivity.launch(context, joinJitsiCall(it))
                    } ?: run {
                        Toast.makeText(context, "Can't find an existing call", Toast.LENGTH_SHORT)
                            .show()
                    }
                    onDismiss()
                }
            }
            CallAction.ASK -> {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    confirmButton = {
                        Row() {
                            Button(
                                onClick = {
                                    onSetCallAction(CallAction.CREATE)
                                },
                            ) {
                                Text("Create call")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSetCallAction(CallAction.JOIN)
                                },
                                enabled = latestJitsiCall != null
                            ) {
                                Text("Join call")
                            }
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onDismiss,
                        ) {
                            Text("Cancel")
                        }
                    },
                    modifier = modifier,
                    text = {
                        Text("Do you want to create a new call or join the latest call?")
                    }
                )
            }
        }
    }
}