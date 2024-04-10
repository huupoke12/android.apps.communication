package io.github.huupoke12.android.apps.communication.ui.screens

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.PowerLevelsContent
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.session.room.powerlevels.PowerLevelsHelper
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.Optional

class RoomDetailViewModel(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    private val roomId: String = savedStateHandle["roomId"]!!
    val roomSummaryLive = matrixService.getLiveRoomSummary(roomId)
    val roomMembersLive = matrixService.getLiveRoomMembers(roomId)
    val roomPowerLevelsLive = matrixService.getLiveRoomPowerLevels(roomId)
    private val powerLevelsHelper = PowerLevelsHelper(
        matrixService.getRoomPowerLevels(roomId) ?: PowerLevelsContent()
    )
    val uiState = MutableStateFlow(RoomDetailUiState())
    val myUserId = matrixService.getMyUserId()

    init {
        viewModelScope.launch {
            matrixService.loadAllRoomMembers(roomId)
        }
    }

    fun startEditingName(currentName: String) {
        uiState.value = uiState.value.copy(
            isEditingName = true,
            newRoomName = currentName
        )
    }

    fun stopEditingName() {
        uiState.value = uiState.value.copy(
            isEditingName = false,
            newRoomName = "",
        )
    }

    fun updateNewRoomName(roomName: String) {
        uiState.value = uiState.value.copy(
            newRoomName = roomName,
        )
    }

    fun startInvitingUser() {
        uiState.value = uiState.value.copy(
            isInvitingUser = true,
        )
    }

    fun stopInvitingUser() {
        uiState.value = uiState.value.copy(
            isInvitingUser = false,
            searchQuery = "",
            searchResult = emptyList(),
        )
    }

    fun updateSearchQuery(query: String) {
        uiState.value = uiState.value.copy(
            searchQuery = query,
        )
    }

    fun startViewingBannedMembers() {
        uiState.value = uiState.value.copy(
            isViewingBannedMembers = true,
        )
    }

    fun stopViewingBannedMembers() {
        uiState.value = uiState.value.copy(
            isViewingBannedMembers = false,
        )
    }

    fun getLiveUserRoomMembership(userId: String): LiveData<Optional<Membership>> {
        return matrixService.getLiveUserRoomMembership(
            roomId = roomId,
            userId = userId,
        )
    }

    fun searchUsers() = viewModelScope.launch {
        val result = matrixService.searchUsers(query = uiState.value.searchQuery)
        uiState.value = uiState.value.copy(
            searchResult = result,
        )
    }


    fun inviteUser(userId: String) = viewModelScope.launch {
        matrixService.inviteUser(
            roomId = roomId,
            userId = userId,
        )
    }

    fun canInviteUsers(powerLevelsContent: PowerLevelsContent, isDirect: Boolean): Boolean {
        return !isDirect && PowerLevelsHelper(powerLevelsContent).isUserAbleToInvite(myUserId)
    }

    fun canEditRoomName(powerLevelsContent: PowerLevelsContent, isDirect: Boolean): Boolean {
        return !isDirect && PowerLevelsHelper(powerLevelsContent).isUserAllowedToSend(
            userId = myUserId,
            isState = true,
            eventType = EventType.STATE_ROOM_NAME,
        )
    }

    fun canUpdateRoomAvatar(powerLevelsContent: PowerLevelsContent, isDirect: Boolean): Boolean {
        return !isDirect && PowerLevelsHelper(powerLevelsContent).isUserAllowedToSend(
            userId = myUserId,
            isState = true,
            eventType = EventType.STATE_ROOM_AVATAR,
        )
    }


    fun startUpdatingAvatar() {
        uiState.update {
            it.copy(
                isUpdatingAvatar = true,
                isUploadingAvatar = false,
                isRemovingAvatar = false,
                avatarUri = null,
                avatarFileName = null,
            )
        }
    }

    fun stopUpdatingAvatar() {
        uiState.update {
            it.copy(
                isUpdatingAvatar = false,
                isUploadingAvatar = false,
                isRemovingAvatar = false,
                avatarUri = null,
                avatarFileName = null,
            )
        }
    }

    fun setAvatar(avatarUri: Uri, avatarFileName: String) {
        uiState.update {
            it.copy(
                avatarUri = avatarUri,
                avatarFileName = avatarFileName,
            )
        }
    }

    fun updateAvatar() = viewModelScope.launch {
        uiState.update {
            it.copy(
                isUploadingAvatar = true
            )
        }
        matrixService.setRoomAvatar(
            roomId = roomId,
            avatarUri = uiState.value.avatarUri!!,
            avatarFileName = uiState.value.avatarFileName!!,
        )
        stopUpdatingAvatar()
    }

    fun startRemovingAvatar() {
        uiState.update {
            it.copy(
                isRemovingAvatar = true
            )
        }
    }

    fun stopRemovingAvatar() {
        uiState.update {
            it.copy(
                isRemovingAvatar = false,
            )
        }
    }

    fun removeAvatar() = viewModelScope.launch {
        matrixService.removeRoomAvatar(
            roomId = roomId
        )
        stopRemovingAvatar()
    }



    fun canKick(powerLevelsContent: PowerLevelsContent, targetUserId: String, isDirect: Boolean): Boolean {
        return PowerLevelsHelper(powerLevelsContent).run {
            isUserAbleToKick(myUserId) &&
                    getUserPowerLevelValue(myUserId) > getUserPowerLevelValue(targetUserId) &&
                    !isDirect && !isMyself(targetUserId)
        }
    }

    fun canBan(powerLevelsContent: PowerLevelsContent, targetUserId: String, isDirect: Boolean): Boolean {
        return PowerLevelsHelper(powerLevelsContent).run {
            isUserAbleToBan(myUserId) &&
                    getUserPowerLevelValue(myUserId) > getUserPowerLevelValue(targetUserId) &&
                    !isDirect && !isMyself(targetUserId)
        }
    }

    fun canUnban(powerLevelsContent: PowerLevelsContent, targetUserId: String, isDirect: Boolean): Boolean {
        return PowerLevelsHelper(powerLevelsContent).run {
            isUserAbleToBan(myUserId) &&
                    getUserPowerLevelValue(myUserId) > getUserPowerLevelValue(targetUserId) &&
                    !isDirect && !isMyself(targetUserId)
        }
    }

    fun canPromote(powerLevelsContent: PowerLevelsContent, targetUserId: String, isDirect: Boolean): Boolean {
        return PowerLevelsHelper(powerLevelsContent).run {
            isUserAllowedToSend(
                userId = myUserId,
                isState = true,
                eventType = EventType.STATE_ROOM_POWER_LEVELS,
            ) && !isDirect && !isMyself(targetUserId) &&
                getUserPowerLevelValue(myUserId) >= Role.Admin.value &&
                getUserPowerLevelValue(targetUserId) < Role.Moderator.value
        }
    }

    fun canDemote(powerLevelsContent: PowerLevelsContent, targetUserId: String, isDirect: Boolean): Boolean {
        return PowerLevelsHelper(powerLevelsContent).run {
            isUserAllowedToSend(
                userId = myUserId,
                isState = true,
                eventType = EventType.STATE_ROOM_POWER_LEVELS,
            ) && !isDirect && !isMyself(targetUserId) &&
                    getUserPowerLevelValue(myUserId) >= Role.Admin.value &&
                    getUserPowerLevelValue(myUserId) > getUserPowerLevelValue(targetUserId) &&
                    getUserPowerLevelValue(targetUserId) > Role.Default.value
        }
    }

    fun kickMember(userId: String) = viewModelScope.launch {
        matrixService.kickMember(
            roomId = roomId,
            userId = userId,
        )
    }

    fun banMember(userId: String) = viewModelScope.launch {
        matrixService.banMember(
            roomId = roomId,
            userId = userId,
        )
    }

    fun unbanMember(userId: String) = viewModelScope.launch {
        matrixService.unbanMember(
            roomId = roomId,
            userId = userId,
        )
    }

    fun promoteUserAsModerator(userId: String) = viewModelScope.launch {
        matrixService.promoteUserAsModerator(
            roomId = roomId,
            userId = userId,
        )
    }

    fun demoteUser(userId: String) = viewModelScope.launch {
        matrixService.demoteUser(
            roomId = roomId,
            userId = userId,
        )
    }

    fun startLeavingRoom() {
        uiState.update {
            it.copy(
                isLeavingRoom = true
            )
        }
    }

    fun stopLeavingRoom() {
        uiState.update {
            it.copy(
                isLeavingRoom = false
            )
        }
    }

    fun leaveRoom() = viewModelScope.launch {
        matrixService.leaveRoom(roomId)
    }


    fun isMyself(userId: String): Boolean {
        return userId == myUserId
    }

    fun setRoomName() = viewModelScope.launch {
        matrixService.setRoomName(
            roomId = roomId,
            roomName = uiState.value.newRoomName,
        )
    }

    fun getLiveRoomNotificationState() = matrixService.getLiveRoomNotificationState(roomId)

    fun muteNotifications() = viewModelScope.launch {
        matrixService.setRoomNotificationState(
            roomId = roomId,
            roomNotificationState = RoomNotificationState.MENTIONS_ONLY,
        )
    }

    fun unmuteNotifications() = viewModelScope.launch {
        matrixService.setRoomNotificationState(
            roomId = roomId,
            roomNotificationState = RoomNotificationState.ALL_MESSAGES,
        )
    }

    fun setRoomNotificationState(roomNotificationState: RoomNotificationState) = viewModelScope.launch {
        matrixService.setRoomNotificationState(roomId, roomNotificationState)
    }

    fun getDirectRoomMember() = matrixService.getDirectRoomMember(roomId)

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}

data class RoomDetailUiState(
    val newRoomName: String = "",
    val searchQuery: String = "",
    val searchResult: List<User> = emptyList(),
    val isEditingName: Boolean = false,
    val isInvitingUser: Boolean = false,
    val isViewingBannedMembers: Boolean = false,
    val isLeavingRoom: Boolean = false,
    val isUpdatingAvatar: Boolean = false,
    val isRemovingAvatar: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val avatarUri: Uri? = null,
    val avatarFileName: String? = null,
)