package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.UserList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockListScreen(
    navigateToUserScreen: (String) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    blockListViewModel: BlockListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val blockedUserList by blockListViewModel.blockedUserListLive.observeAsState()
    blockedUserList?.let {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text("Blocked users")
                    },
                    navigationIcon = {
                        IconButton(onClick = navigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                        }
                    }
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
                Text(
                    text = "Blocked users: ${it.size}",
                    modifier = Modifier.padding(8.dp)
                )
                UserList(
                    userList = it,
                    navigateToUserScreen = navigateToUserScreen,
                    resolveAvatar = blockListViewModel::resolveAvatar,
                )
            }

        }
    } ?: run {
        LoadingContainer()
    }


}