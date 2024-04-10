package io.github.huupoke12.android.apps.communication.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.SearchBar
import io.github.huupoke12.android.apps.communication.ui.components.UserList

@Composable
fun ContactListingSubScreen(
    navigateToUserScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    contactListingViewModel: ContactListingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by contactListingViewModel.uiState.collectAsStateWithLifecycle()
    val contactList by contactListingViewModel.contactListLive.observeAsState()
    Column(
        modifier = modifier,
    ) {
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { contactListingViewModel.updateSearchQuery(it) },
            onQueryClear = { contactListingViewModel.updateSearchQuery("") },
            onSearch = { contactListingViewModel.searchUser() },
            placeholder = {
                Text("Search for any users")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = null
                )
            },
        ) {
            if (uiState.isSearching) {
                LoadingContainer {
                    Text("Searching")
                }
            }
            uiState.searchResult?.let {
                UserList(
                    userList = it,
                    navigateToUserScreen = navigateToUserScreen,
                    resolveAvatar = contactListingViewModel::resolveAvatar,
                    displayCount = true,
                )
            }
        }
        contactList?.let {
            Text(
                text = "Contacts: ${it.size}",
                modifier = Modifier.padding(8.dp)
            )
            UserList(
                userList = it,
                navigateToUserScreen = navigateToUserScreen,
                resolveAvatar = contactListingViewModel::resolveAvatar,
            )
        } ?: run {
            LoadingContainer()
        }
    }
}

