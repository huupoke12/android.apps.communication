package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import kotlinx.coroutines.launch

class UserViewModel(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    private val userId: String = savedStateHandle["userId"]!!

    init {
        viewModelScope.launch {
            matrixService.resolveUser(userId)
        }
    }

    val userIsBlockedLive = matrixService.getLiveIgnoredUserList().map { userList ->
        userList.any { user ->
            user.userId == userId
        }
    }
    val userIsContactLive = matrixService.getLiveContactList().map { userList ->
        userList.any { user ->
            user.userId == userId
        }
    }
    val userLive = matrixService.getLiveUser(userId)


    fun addUser() {
        viewModelScope.launch {
            matrixService.addUsersToContactList(listOf(userId))
        }
    }

    fun removeUser() {
        viewModelScope.launch {
            matrixService.removeUsersFromContactList(listOf(userId))
        }
    }

    fun blockUser() {
        viewModelScope.launch {
            matrixService.ignoreUsers(listOf(userId))
        }
    }

    fun unBlockUser() {
        viewModelScope.launch {
            matrixService.unIgnoreUsers(listOf(userId))
        }
    }

    fun isMyself(): Boolean {
        return userId == matrixService.getMyUserId()
    }

    suspend fun getDirectRoomId() = matrixService.getOrCreateDirectRoom(userId)

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}