package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.AddRemoveButton
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.SearchBar
import io.github.huupoke12.android.apps.communication.ui.components.UserListItem
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.util.toMatrixItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    navigateBack: () -> Unit,
    navigateToRoomScreen: (String) -> Unit,
    navigateToUserScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    createRoomViewModel: CreateRoomViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by createRoomViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Create new group room")
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        navigateToRoomScreen(createRoomViewModel.createRoom())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text("Create room")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Set room name",
                style = MaterialTheme.typography.titleMedium,
            )
            TextField(
                value = uiState.name,
                onValueChange = createRoomViewModel::updateRoomName,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Room name")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                )
            )
            Divider()
            Text(
                text = "Invite users",
                style = MaterialTheme.typography.titleMedium,
            )
            SearchBar(
                query = uiState.query,
                onQueryChange = createRoomViewModel::updateQuery,
                onQueryClear = { createRoomViewModel.updateQuery("") },
                onSearch = createRoomViewModel::searchUsers,
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
                uiState.searchResult?.let { searchResult ->
                    Text(
                        text = "Found ${searchResult.size} users",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp),
                    )
                    LazyColumn() {
                        items(searchResult) { user ->
                            UserListItem(
                                userItem = user.toMatrixItem(),
                                navigateToUserScreen = navigateToUserScreen,
                                resolveAvatar = createRoomViewModel::resolveAvatar,
                                trailingContent = {
                                    AddRemoveButton(
                                        isAdded = uiState.inviteUserList.contains(
                                            user,
                                        ),
                                        onAdd = { createRoomViewModel.addUserToInviteList(user) },
                                        onRemove = { createRoomViewModel.removeUserFromInviteList(user) },
                                    )
                                }
                            )
                        }
                    }
                }
                if (uiState.isSearching) {
                    LoadingContainer {
                        Text("Searching")
                    }
                }
            }
            Text(
                text = "Invite list",
                style = MaterialTheme.typography.titleSmall,
            )
            LazyColumn() {
                items(uiState.inviteUserList) { user ->
                    UserListItem(
                        userItem = user.toMatrixItem(),
                        navigateToUserScreen = navigateToUserScreen,
                        resolveAvatar = createRoomViewModel::resolveAvatar,
                        trailingContent = {
                            AddRemoveButton(
                                isAdded = uiState.inviteUserList.contains(
                                    user,
                                ),
                                onAdd = { createRoomViewModel.addUserToInviteList(user) },
                                onRemove = { createRoomViewModel.removeUserFromInviteList(user) },
                            )
                        }
                    )
                }
            }
        }
    }
}