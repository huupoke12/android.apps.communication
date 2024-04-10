package io.github.huupoke12.android.apps.communication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.Centered
import io.github.huupoke12.android.apps.communication.ui.components.IconText
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.RoundedIconTextAction
import io.github.huupoke12.android.apps.communication.ui.components.UserProfileCard
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem

@Composable
fun UserScreen(
    navigateBack: () -> Unit,
    navigateToRoomScreen: (String, RoomAction?) -> Unit,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val userState by userViewModel.userLive.observeAsState()
    val userIsBlocked by userViewModel.userIsBlockedLive.observeAsState(false)
    val userIsContact by userViewModel.userIsContactLive.observeAsState(false)
    userState?.let { userWrapper ->
        userWrapper.getOrNull()?.let { user ->
            Scaffold(
                modifier = modifier,
                topBar = {
                    UserTopBar(
                        userItem = user.toMatrixItem(),
                        navigateBack = navigateBack,
                        isMyself = userViewModel.isMyself(),
                        isBlock = userIsBlocked,
                        isContact = userIsContact,
                        onBlockUser = userViewModel::blockUser,
                        onUnblockUser = userViewModel::unBlockUser,
                        onRemoveUser = userViewModel::removeUser,
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues),
                ) {
                    UserProfileCard(
                        userItem = user.toMatrixItem(),
                        resolveAvatar = userViewModel::resolveAvatar,
                    )
                    if (userIsBlocked) {
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            IconText(
                                imageVector = Icons.Default.Block,
                                text = "Blocked",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    if (!userViewModel.isMyself()) {
                        val coroutineScope = rememberCoroutineScope()
                        UserActionRow(
                            isInContact = userIsContact,
                            onAddUser = userViewModel::addUser,
                            navigateToUserRoom = { roomAction ->
                                coroutineScope.launch {
                                    navigateToRoomScreen(userViewModel.getDirectRoomId(), roomAction)
                                }
                            },
                        )
                    }
                }
            }
        } ?: run {
            Centered {
                Text(
                    text = "User does not exist",
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
fun UserTopBar(
    userItem: MatrixItem.UserItem,
    isMyself: Boolean,
    isContact: Boolean,
    isBlock: Boolean,
    onBlockUser: () -> Unit,
    onUnblockUser: () -> Unit,
    onRemoveUser: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(userItem.displayNameOrUsername()) },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
            }
        },
        actions = {
            if (!isMyself) {
                Box {
                    var userDropDownMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            userDropDownMenuExpanded = !userDropDownMenuExpanded
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Open more actions"
                        )
                    }

                    UserDropDownMenu(
                        expanded = userDropDownMenuExpanded,
                        onDismissRequest = { userDropDownMenuExpanded = false },
                        isInContact = isContact,
                        isInBlock = isBlock,
                        onBlockUser = onBlockUser,
                        onRemoveUser = onRemoveUser,
                        onUnblockUser = onUnblockUser,
                    )
                }
            }
        }
    )
}

@Composable
fun UserActionRow(
    isInContact: Boolean,
    onAddUser: () -> Unit,
    navigateToUserRoom: (RoomAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        RoundedIconTextAction(
            icon = Icons.Default.Chat,
            text = "Chat",
            onClick = { navigateToUserRoom(null) },
            modifier = Modifier.weight(1f),
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            textColor = MaterialTheme.colorScheme.secondary,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        RoundedIconTextAction(
            icon = Icons.Default.Call,
            text = "Call",
            onClick = { navigateToUserRoom(RoomAction.CALL) },
            modifier = Modifier.weight(1f),
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            textColor = MaterialTheme.colorScheme.secondary,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        RoundedIconTextAction(
            icon = Icons.Default.VideoCall,
            text = "Video call",
            onClick = { navigateToUserRoom(RoomAction.VIDEO_CALL) },
            modifier = Modifier.weight(1f),
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            textColor = MaterialTheme.colorScheme.secondary,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        if (!isInContact) {
            RoundedIconTextAction(
                icon = Icons.Default.PersonAdd,
                text = "Add",
                onClick = {
                    onAddUser()
                    Toast.makeText(context, "Added as contact", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                textColor = MaterialTheme.colorScheme.tertiary,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            )
        }
    }
}


@Composable
fun UserDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    isInContact: Boolean,
    isInBlock: Boolean,
    onRemoveUser: () -> Unit,
    onBlockUser: () -> Unit,
    onUnblockUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        if (isInContact) {
            DropdownMenuItem(
                text = {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    onRemoveUser()
                    Toast.makeText(context, "Removed as contact", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                }
            )
        }
        if (!isInBlock) {
            DropdownMenuItem(
                text = {
                    Text("Block", color = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    onBlockUser()
                    Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                }
            )
        } else {
            DropdownMenuItem(
                text = {
                    Text("Unblock")
                },
                onClick = {
                    onUnblockUser()
                    Toast.makeText(context, "Unblocked", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                }
            )
        }
    }
}