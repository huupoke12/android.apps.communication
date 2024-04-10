package io.github.huupoke12.android.apps.communication.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.Centered
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.RoomAvatar
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.isMuted
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.util.toMatrixItem

@Composable
fun RoomListingSubScreen(
    navigateToRoomScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    roomListingViewModel: RoomListingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val roomListState by roomListingViewModel.roomListLive.observeAsState()
    roomListState?.let { roomList ->
        var showActiveRoomList by rememberSaveable {
            mutableStateOf(true)
        }
        val displayRoomList = if (showActiveRoomList) roomList.filter {
            it.membership.isActive()
        } else roomList.filter {
            it.membership.isLeft()
        }
        Column(
            modifier = modifier,
        ) {
            TabRow(
                selectedTabIndex = if (showActiveRoomList) 0 else 1,
            ) {
                Tab(
                    selected = showActiveRoomList,
                    onClick = { showActiveRoomList = true }
                ) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(vertical = 16.dp),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Tab(
                    selected = !showActiveRoomList,
                    onClick = { showActiveRoomList = false }
                ) {
                    Text(
                        text = "Inactive",
                        modifier = Modifier.padding(vertical = 16.dp),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            LazyColumn(
            ) {
                items(displayRoomList) { roomSummary ->
                    val notificationState by roomListingViewModel
                        .getLiveRoomNotificationState(roomSummary.roomId)
                        .observeAsState(RoomNotificationState.ALL_MESSAGES)
                    RoomListItem(
                        roomSummary = roomSummary,
                        resolveAvatar = roomListingViewModel::resolveAvatar,
                        isMuted = notificationState.isMuted(),
                        directRoomMember = roomListingViewModel.getDirectRoomMember(roomSummary.roomId),
                        previewText = roomListingViewModel.getPreviewText(roomSummary),
                        onClick = { navigateToRoomScreen(roomSummary.roomId) },
                    )
                }
            }
            if (displayRoomList.isEmpty()) {
                Centered {
                    Text("No rooms")
                }
            }

        }

    } ?: run {
        LoadingContainer()
    }

}

@Composable
fun RoomListItem(
    roomSummary: RoomSummary,
    resolveAvatar: (String, AvatarSize) -> String?,
    isMuted: Boolean,
    directRoomMember: RoomMemberSummary?,
    previewText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text = roomSummary.displayName,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        modifier = modifier.clickable(
            onClick = onClick,
        ),
        leadingContent = {
             RoomAvatar(
                 roomItem = roomSummary.toMatrixItem(),
                 directUserItem = directRoomMember?.toMatrixItem(),
                 resolveAvatar = resolveAvatar,
             )
        },
        supportingContent = previewText?.let {
            {
                val hasNewEvent = roomSummary.hasNewMessages
                Text(
                text = it,
                color = if (hasNewEvent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (hasNewEvent) FontWeight.Bold else FontWeight.Normal,
                style = if (hasNewEvent) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                )
            }
        },
        trailingContent = {
            if (isMuted) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = "Muted",
                )
            }
        }
    )
}