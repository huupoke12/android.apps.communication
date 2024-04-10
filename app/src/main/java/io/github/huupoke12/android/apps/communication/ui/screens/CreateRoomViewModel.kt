package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.user.model.User

class CreateRoomViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val uiState = MutableStateFlow(CreateRoomUiState())

    fun updateRoomName(name: String) {
        uiState.value = uiState.value.copy(
            name = name,
        )
    }

    fun updateQuery(query: String) {
        uiState.value = uiState.value.copy(
            query = query,
        )
    }

    fun searchUsers(query: String) = viewModelScope.launch {
        uiState.update {
            it.copy(
                isSearching = true,
            )
        }
        val result = matrixService.searchUsers(query = query)
        uiState.update {
            it.copy(
                isSearching = false,
                searchResult = result
            )
        }
    }

    fun addUserToInviteList(user: User) {
        uiState.value = uiState.value.copy(
            inviteUserList = uiState.value.inviteUserList + user
        )
    }

    fun removeUserFromInviteList(user: User) {
        uiState.value = uiState.value.copy(
            inviteUserList = uiState.value.inviteUserList - user
        )
    }

    suspend fun createRoom(): String {
        return matrixService.createRoom(
            name = uiState.value.name,
            invitedUserIds = uiState.value.inviteUserList.map {
                it.userId
            },
        )
    }

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}

data class CreateRoomUiState(
    val name: String = "",
    val query: String = "",
    val inviteUserList: List<User> = emptyList(),
    val searchResult: List<User>? = null,
    val isSearching: Boolean = false,
)