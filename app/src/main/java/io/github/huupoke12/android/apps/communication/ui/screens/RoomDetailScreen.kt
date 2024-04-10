package io.github.huupoke12.android.apps.communication.ui.screens

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.AvatarContainer
import io.github.huupoke12.android.apps.communication.ui.components.Centered
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.RoomAvatar
import io.github.huupoke12.android.apps.communication.ui.components.RoundedIconTextAction
import io.github.huupoke12.android.apps.communication.ui.components.SearchBar
import io.github.huupoke12.android.apps.communication.ui.components.UserListItem
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.isMuted
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.PowerLevelsContent
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.session.room.powerlevels.PowerLevelsHelper
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import org.matrix.android.sdk.api.util.Optional
import org.matrix.android.sdk.api.util.toMatrixItem

@Composable
fun RoomDetailScreen(
    navigateBack: () -> Unit,
    navigateToUserScreen: (String) -> Unit,
    navigateToHomeScreen: () -> Unit,
    modifier: Modifier = Modifier,
    roomDetailViewModel: RoomDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val roomSummaryState by roomDetailViewModel.roomSummaryLive.observeAsState()
    val roomMemberState by roomDetailViewModel.roomMembersLive.observeAsState()
    val notificationState by roomDetailViewModel.getLiveRoomNotificationState()
        .observeAsState(RoomNotificationState.ALL_MESSAGES)
    val powerLevelsContentState by roomDetailViewModel.roomPowerLevelsLive.observeAsState()
    val uiState by roomDetailViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.query(
                it, null, null, null, null
            )?.apply {
                moveToFirst()
                roomDetailViewModel.setAvatar(
                    avatarUri = it,
                    avatarFileName = getString(getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                )
                close()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            RoomDetailTopBar(
                navigateBack = navigateBack,
            )
        }
    ) { paddingValues ->
        roomSummaryState?.let { roomSummaryWrapper ->
            roomSummaryWrapper.getOrNull()?.let { roomSummary ->
                val powerLevelsContent = powerLevelsContentState?.getOrNull() ?: PowerLevelsContent()
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                ) {
                    item {
                        if (uiState.isEditingName) {
                            AlertDialog(
                                onDismissRequest = roomDetailViewModel::stopEditingName,
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            roomDetailViewModel.setRoomName()
                                            roomDetailViewModel.stopEditingName()
                                        }
                                    ) {
                                        Text("Save")
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = roomDetailViewModel::stopEditingName,
                                    ) {
                                        Text("Cancel")
                                    }
                                },
                                title = {
                                    Text("Change room name")
                                },
                                text = {
                                    TextField(
                                        value = uiState.newRoomName,
                                        onValueChange = roomDetailViewModel::updateNewRoomName,
                                        modifier = Modifier.padding(16.dp),
                                        label = {
                                            Text("Room name")
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                        )
                                    )
                                }
                            )
                        } else if (uiState.isInvitingUser) {
                            AlertDialog(
                                onDismissRequest = roomDetailViewModel::stopInvitingUser,
                                confirmButton = {
                                    Button(
                                        onClick = roomDetailViewModel::stopInvitingUser,
                                    ) {
                                        Text("Close")
                                    }
                                },
                                title = {
                                    Text("Invite users")
                                },
                                text = {
                                    SearchBar(
                                        query = uiState.searchQuery,
                                        onQueryChange = roomDetailViewModel::updateSearchQuery,
                                        onQueryClear = { roomDetailViewModel.updateSearchQuery("") },
                                        onSearch = { roomDetailViewModel.searchUsers() },
                                        placeholder = {
                                            Text("Search for users")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.PersonSearch,
                                                contentDescription = null,
                                            )
                                        }
                                    ) {
                                        Text(
                                            text = "Found ${uiState.searchResult.size} users",
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(vertical = 8.dp),
                                        )
                                        LazyColumn() {
                                            items(uiState.searchResult) { user ->
                                                val userMembership by roomDetailViewModel
                                                    .getLiveUserRoomMembership(user.userId)
                                                    .observeAsState(Optional.empty())
                                                UserListItem(
                                                    userItem = user.toMatrixItem(),
                                                    navigateToUserScreen = navigateToUserScreen,
                                                    resolveAvatar = roomDetailViewModel::resolveAvatar,
                                                    trailingContent = {
                                                        InviteButton(
                                                            membership = userMembership.getOrNull(),
                                                            onInvite = {
                                                                roomDetailViewModel.inviteUser(
                                                                    user.userId
                                                                )
                                                            }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        } else if (uiState.isViewingBannedMembers) {
                            AlertDialog(
                                onDismissRequest = roomDetailViewModel::stopViewingBannedMembers,
                                confirmButton = {
                                    Button(
                                        onClick = roomDetailViewModel::stopViewingBannedMembers,
                                    ) {
                                        Text("Close")
                                    }
                                },
                                title = {
                                    Text("Banned members")
                                },
                                text = {
                                    roomMemberState?.let { roomMemberList ->
                                        val bannedMemberList =
                                            roomMemberList.filter { roomMember ->
                                                roomMember.membership == Membership.BAN
                                            }
                                        Column {
                                            Text(
                                                text = "There are ${bannedMemberList.size} members",
                                            )
                                            LazyColumn {
                                                items(bannedMemberList) { user ->
                                                    UserListItem(
                                                        userItem = user.toMatrixItem(),
                                                        navigateToUserScreen = navigateToUserScreen,
                                                        resolveAvatar = roomDetailViewModel::resolveAvatar,
                                                        trailingContent = {
                                                            OutlinedButton(
                                                                onClick = {
                                                                    roomDetailViewModel.unbanMember(
                                                                        user.userId
                                                                    )
                                                                },
                                                                enabled = roomDetailViewModel.canUnban(
                                                                    powerLevelsContent = powerLevelsContent,
                                                                    targetUserId = user.userId,
                                                                    isDirect = roomSummary.isDirect
                                                                ),
                                                            ) {
                                                                Text("Unban")
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } ?: run {
                                        LoadingContainer()
                                    }
                                }
                            )
                        } else if (uiState.isLeavingRoom) {
                            AlertDialog(
                                onDismissRequest = roomDetailViewModel::stopLeavingRoom,
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            roomDetailViewModel.leaveRoom()
                                            navigateToHomeScreen()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    ) {
                                        Text("Leave room")
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = roomDetailViewModel::stopLeavingRoom,
                                    ) {
                                        Text("Cancel")
                                    }
                                },
                                text = {
                                    Text(
                                        text = "Are you sure?",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                            )
                        } else if (uiState.isUpdatingAvatar) {
                            AlertDialog(
                                onDismissRequest = roomDetailViewModel::stopUpdatingAvatar,
                                confirmButton = {
                                    Button(
                                        onClick = roomDetailViewModel::updateAvatar,
                                        enabled = (uiState.avatarUri != null && uiState.avatarFileName != null),
                                    ) {
                                        Text("Save")
                                    }
                                },
                                title = {
                                    Text("Update avatar")
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = roomDetailViewModel::stopUpdatingAvatar,
                                    ) {
                                        Text("Cancel")
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        val avatarSize = AvatarSize.LARGE
                                        AvatarContainer(
                                            avatarSize = avatarSize,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            uiState.avatarUri?.let {
                                                AsyncImage(
                                                    model = it,
                                                    contentDescription = "Avatar",
                                                )
                                            } ?: run {
                                                RoomAvatar(
                                                    roomItem = roomSummary.toMatrixItem(),
                                                    avatarSize = avatarSize,
                                                    directUserItem = roomDetailViewModel.getDirectRoomMember()
                                                        ?.toMatrixItem(),
                                                    resolveAvatar = roomDetailViewModel::resolveAvatar,
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                avatarPickerLauncher.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        ) {
                                            Text("Choose file")
                                        }
                                        if (uiState.isUploadingAvatar) {
                                            LoadingContainer(
                                                modifier = Modifier.padding(8.dp),
                                                fillMaxSize = false,
                                            ) {
                                                Text("Uploading")
                                            }
                                        }
                                    }
                                }
                            )
                        } else if (uiState.isRemovingAvatar) {
                            AlertDialog(
                                onDismissRequest = roomDetailViewModel::stopRemovingAvatar,
                                confirmButton = {
                                    Button(
                                        onClick = roomDetailViewModel::removeAvatar,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    ) {
                                        Text("Remove avatar")
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = roomDetailViewModel::stopRemovingAvatar,
                                    ) {
                                        Text("Cancel")
                                    }
                                },
                                text = {
                                    Text(
                                        text = "Are you sure?",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                            )
                        }
                        Column(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            RoomAvatar(
                                roomItem = roomSummary.toMatrixItem(),
                                resolveAvatar = roomDetailViewModel::resolveAvatar,
                                directUserItem = roomDetailViewModel.getDirectRoomMember()
                                    ?.toMatrixItem(),
                                avatarSize = AvatarSize.LARGE,
                            )
                            Text(
                                text = roomSummary.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (notificationState.isMuted()) {
                                RoundedIconTextAction(
                                    icon = Icons.Default.NotificationsOff,
                                    text = "Unmute",
                                    onClick = { roomDetailViewModel.unmuteNotifications() },
                                    iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                                )
                            } else {
                                RoundedIconTextAction(
                                    icon = Icons.Default.Notifications,
                                    text = "Mute",
                                    onClick = { roomDetailViewModel.muteNotifications() }
                                )
                            }
                        }
                        if (roomDetailViewModel.canInviteUsers(powerLevelsContent, roomSummary.isDirect)) {
                            ListItem(
                                headlineContent = {
                                    Text("Invite users")
                                },
                                modifier = Modifier.clickable(
                                    onClick = roomDetailViewModel::startInvitingUser,
                                ),
                                leadingContent = {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                        if (roomDetailViewModel.canEditRoomName(powerLevelsContent, roomSummary.isDirect)) {
                            ListItem(
                                headlineContent = {
                                    Text("Edit room name")
                                },
                                modifier = Modifier.clickable(
                                    onClick = { roomDetailViewModel.startEditingName(roomSummary.displayName) },
                                ),
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                    )
                                }
                            )
                        }
                        if (roomDetailViewModel.canUpdateRoomAvatar(powerLevelsContent, roomSummary.isDirect)) {
                            ListItem(
                                headlineContent = {
                                    Text("Update avatar")
                                },
                                modifier = Modifier.clickable(
                                    onClick = roomDetailViewModel::startUpdatingAvatar,
                                ),
                                leadingContent = {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                    )
                                }
                            )
                        }
                        if (roomDetailViewModel.canUpdateRoomAvatar(powerLevelsContent, roomSummary.isDirect)
                            && roomSummary.avatarUrl.isNotBlank()
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text("Remove avatar")
                                },
                                modifier = Modifier.clickable(
                                    onClick = roomDetailViewModel::startRemovingAvatar,
                                ),
                                leadingContent = {
                                    Icon(
                                        Icons.Default.NoAccounts,
                                        contentDescription = null,
                                    )
                                }
                            )
                        }
                        if (!roomSummary.isDirect) {
                            ListItem(
                                headlineContent = {
                                    Text("View banned members")
                                },
                                modifier = Modifier.clickable(
                                    onClick = roomDetailViewModel::startViewingBannedMembers,
                                ),
                                leadingContent = {
                                    Icon(
                                        Icons.Default.PersonOff,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                        if (roomSummary.membership.isActive()) {
                            ListItem(
                                headlineContent = {
                                    Text("Leave room")
                                },
                                modifier = Modifier.clickable(
                                    onClick = roomDetailViewModel::startLeavingRoom,
                                ),
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Logout,
                                        contentDescription = null,
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    headlineColor = MaterialTheme.colorScheme.error,
                                    leadingIconColor = MaterialTheme.colorScheme.error,
                                ),
                            )
                        }
                        Divider()
                        Text(
                            text = "Members",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    roomMemberState?.let { memberList ->
                        val joinedMemberList = memberList.filter { roomMember ->
                            roomMember.membership == Membership.JOIN
                        }
                        item {
                            Text(
                                text = "There are ${joinedMemberList.size} members",
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                        items(joinedMemberList) { user ->
                            var userDropDownMenuExpanded by remember { mutableStateOf(false) }
                            UserListItem(
                                userItem = user.toMatrixItem(),
                                isMyself = user.userId == roomDetailViewModel.myUserId,
                                navigateToUserScreen = { navigateToUserScreen(user.userId) },
                                resolveAvatar = roomDetailViewModel::resolveAvatar,
                                trailingContent = {
                                    Row {
                                        when (PowerLevelsHelper(powerLevelsContent).getUserRole(user.userId)) {
                                            Role.Admin -> Icon(
                                                imageVector = Icons.Default.Stars,
                                                contentDescription = "Admin",
                                            )

                                            Role.Moderator -> Icon(
                                                imageVector = Icons.Default.LocalPolice,
                                                contentDescription = "Moderator",
                                            )

                                            else -> {}
                                        }
                                        Box {
                                            IconButton(
                                                onClick = {
                                                    userDropDownMenuExpanded =
                                                        !userDropDownMenuExpanded
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Open more actions"
                                                )
                                            }
                                            RoomMemberDropdownMenu(
                                                expanded = userDropDownMenuExpanded,
                                                onDismissRequest = {
                                                    userDropDownMenuExpanded = false
                                                },
                                                isKickable = roomDetailViewModel.canKick(
                                                    powerLevelsContent = powerLevelsContent,
                                                    targetUserId = user.userId,
                                                    isDirect = roomSummary.isDirect
                                                ),
                                                isBannable = roomDetailViewModel.canBan(
                                                    powerLevelsContent = powerLevelsContent,
                                                    targetUserId = user.userId,
                                                    isDirect = roomSummary.isDirect
                                                ),
                                                isPromotable = roomDetailViewModel.canPromote(
                                                    powerLevelsContent = powerLevelsContent,
                                                    targetUserId = user.userId,
                                                    isDirect = roomSummary.isDirect,
                                                ),
                                                isDemotable = roomDetailViewModel.canDemote(
                                                    powerLevelsContent = powerLevelsContent,
                                                    targetUserId = user.userId,
                                                    isDirect = roomSummary.isDirect,
                                                ),
                                                onKickMember = {
                                                    roomDetailViewModel.kickMember(user.userId)
                                                },
                                                onBanMember = {
                                                    roomDetailViewModel.banMember(user.userId)
                                                },
                                                onPromote = {
                                                    roomDetailViewModel.promoteUserAsModerator(user.userId)
                                                },
                                                onDemote = {
                                                    roomDetailViewModel.demoteUser(user.userId)
                                                },
                                                onNavigateToUserScreen = {
                                                    navigateToUserScreen(
                                                        user.userId
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
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
            Text("Room does not exist")
            LoadingContainer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailTopBar(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text("Room detail") },
        navigationIcon = {
            IconButton(
                onClick = navigateBack,

                ) {
                Icon(Icons.Default.ArrowBack, "Go back")
            }
        },
        modifier = modifier,
    )
}

@Composable
fun RoomMemberDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    isKickable: Boolean,
    isBannable: Boolean,
    isPromotable: Boolean,
    isDemotable: Boolean,
    onKickMember: () -> Unit,
    onBanMember: () -> Unit,
    onPromote: () -> Unit,
    onDemote: () -> Unit,
    onNavigateToUserScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        DropdownMenuItem(
            text = {
                Text("View profile")
            },
            onClick = {
                onNavigateToUserScreen()
                onDismissRequest()
            }
        )
        if (isPromotable) {
            DropdownMenuItem(
                text = {
                    Text("Promote")
                },
                onClick = {
                    onPromote()
                    onDismissRequest()
                }
            )
        }
        if (isDemotable) {
            DropdownMenuItem(
                text = {
                    Text("Demote", color = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    onDemote()
                    onDismissRequest()
                }
            )
        }
        if (isKickable) {
            DropdownMenuItem(
                text = {
                    Text("Kick", color = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    onKickMember()
                    onDismissRequest()
                }
            )
        }
        if (isBannable) {
            DropdownMenuItem(
                text = {
                    Text("Ban", color = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    onBanMember()
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
fun InviteButton(
    membership: Membership?,
    onInvite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        when (membership) {
            Membership.NONE, Membership.KNOCK, Membership.LEAVE, null -> Button(
                onClick = onInvite,
            ) {
                Text("Invite")
            }

            Membership.INVITE -> Text("Invited")
            Membership.JOIN -> Text("Joined")
            Membership.BAN -> Text("Banned")
        }
    }
}