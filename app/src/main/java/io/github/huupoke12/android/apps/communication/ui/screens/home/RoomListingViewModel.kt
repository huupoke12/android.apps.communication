package io.github.huupoke12.android.apps.communication.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.getPreviewText
import io.github.huupoke12.android.apps.communication.util.roomMemberQueryParamsByMemberships
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState

class RoomListingViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val roomListLive = matrixService.getLiveRoomList()

    fun getPreviewText(roomSummary: RoomSummary): String? {
        return roomSummary.latestPreviewableEvent?.run {
            getPreviewText(
                isSenderMyself = senderInfo.userId == matrixService.getMyUserId(),
                isDirectRoom = roomSummary.isDirect,
            )
        }
    }

    fun getShortJoinedRoomMembers(roomId: String): List<RoomMemberSummary> {
        return matrixService.getRoomMembers(
            roomId = roomId,
            queryParams = roomMemberQueryParamsByMemberships(
                listOf(Membership.JOIN)
            )
        ).take(4)
    }

    fun getLiveRoomNotificationState(roomId: String) = matrixService.getLiveRoomNotificationState(roomId)

    fun setRoomNotificationState(roomId: String, roomNotificationState: RoomNotificationState) = viewModelScope.launch {
        matrixService.setRoomNotificationState(roomId, roomNotificationState)
    }

    fun getDirectRoomMember(roomId: String) = matrixService.getDirectRoomMember(roomId)

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}