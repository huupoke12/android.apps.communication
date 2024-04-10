package io.github.huupoke12.android.apps.communication.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.user.model.User

class ContactListingViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val uiState = MutableStateFlow(ContactListingUiState())
    val contactListLive = matrixService.getLiveContactList()

    fun searchUser() = viewModelScope.launch {
        uiState.update {
            it.copy(
                isSearching = true,
            )
        }
        val result = matrixService.searchUsers(uiState.value.searchQuery)
        uiState.update {
            it.copy(
                isSearching = false,
                searchResult = result
            )
        }
    }


    fun updateSearchQuery(query: String) {
        uiState.value = uiState.value.copy(
            searchQuery = query,
        )
    }

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}

data class ContactListingUiState(
    val searchQuery: String = "",
    val searchResult: List<User>? = null,
    val isSearching: Boolean = false,
    val contactList: List<User> = emptyList(),
)