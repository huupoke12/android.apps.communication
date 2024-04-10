package io.github.huupoke12.android.apps.communication.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.huupoke12.android.apps.communication.data.models.NotificationService

enum class HomeSubScreen(val displayName: String) {
    ROOM_LIST("Rooms"),
    CONTACT_LIST("Contacts"),
    ACCOUNT("Account"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToRoomScreen: (String) -> Unit,
    navigateToUserScreen: (String) -> Unit,
    navigateToBlockListScreen: () -> Unit,
    navigateToCreateRoomScreen: () -> Unit,
    navigateToSignOutScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        context.startService(Intent(context, NotificationService::class.java))
    }
    var currentSubScreen by rememberSaveable {
        mutableStateOf(HomeSubScreen.ROOM_LIST)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(currentSubScreen.displayName) },
                    actions = {
                        if (currentSubScreen == HomeSubScreen.ROOM_LIST) {
                            IconButton(
                                onClick = navigateToCreateRoomScreen,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create new room"
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    label = { Text(HomeSubScreen.ROOM_LIST.displayName) },
                    icon = {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                        )
                    },
                    onClick = { currentSubScreen = HomeSubScreen.ROOM_LIST },
                    selected = currentSubScreen == HomeSubScreen.ROOM_LIST,
                )
                NavigationBarItem(
                    label = { Text(HomeSubScreen.CONTACT_LIST.displayName) },
                    icon = {
                        Icon(
                            Icons.Default.Contacts,
                            contentDescription = null,
                        )
                    },
                    onClick = { currentSubScreen = HomeSubScreen.CONTACT_LIST },
                    selected = currentSubScreen == HomeSubScreen.CONTACT_LIST,
                )
                NavigationBarItem(
                    label = { Text(HomeSubScreen.ACCOUNT.displayName) },
                    icon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                        )
                    },
                    onClick = { currentSubScreen = HomeSubScreen.ACCOUNT },
                    selected = currentSubScreen == HomeSubScreen.ACCOUNT,
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            when (currentSubScreen) {
                HomeSubScreen.ROOM_LIST -> RoomListingSubScreen(
                    navigateToRoomScreen = navigateToRoomScreen
                )

                HomeSubScreen.CONTACT_LIST -> ContactListingSubScreen(
                    navigateToUserScreen = navigateToUserScreen
                )

                HomeSubScreen.ACCOUNT -> AccountSubScreen(
                    navigateToBlockListScreen = navigateToBlockListScreen,
                    navigateToUserScreen = navigateToUserScreen,
                    navigateToSignOutScreen = navigateToSignOutScreen,
                )
            }
        }
    }
}