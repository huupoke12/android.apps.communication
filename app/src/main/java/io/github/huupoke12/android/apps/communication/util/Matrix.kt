package io.github.huupoke12.android.apps.communication.util

import io.github.huupoke12.android.apps.communication.data.models.CustomEventType
import org.matrix.android.sdk.api.query.QueryStringValue
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.isTextMessage
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.members.RoomMemberQueryParams
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomGuestAccessContent
import org.matrix.android.sdk.api.session.room.model.RoomHistoryVisibilityContent
import org.matrix.android.sdk.api.session.room.model.RoomJoinRulesContent
import org.matrix.android.sdk.api.session.room.model.RoomMemberContent
import org.matrix.android.sdk.api.session.room.model.RoomNameContent
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.util.MatrixItem

enum class CallType {
    VOICE, VIDEO,
}

val MatrixItem.UserItem.username
    get() = id.removePrefix("@").substringBefore(':')

fun MatrixItem.UserItem.displayNameOrUsername(): String {
    return displayName ?: username
}

fun TimelineEvent.getPreviewText(
    isSenderMyself: Boolean = false,
    isDirectRoom: Boolean = false,
): String? {
    val senderName = if (isSenderMyself) "You" else senderInfo.disambiguatedDisplayName
    val eventType = root.getClearType()
    val content = root.getClearContent()
    return when(eventType) {
        EventType.STATE_ROOM_CREATE -> "$senderName created the room"
        EventType.STATE_ROOM_NAME -> "$senderName set the room name to \u201c" +
                content.toModel<RoomNameContent>()!!.name + "\u201d"
        EventType.STATE_ROOM_HISTORY_VISIBILITY -> "$senderName set history visibility to " +
                content.toModel<RoomHistoryVisibilityContent>()!!.historyVisibility
        EventType.STATE_ROOM_JOIN_RULES -> "$senderName set join rules to " +
                content.toModel<RoomJoinRulesContent>()!!.joinRules
        EventType.STATE_ROOM_GUEST_ACCESS -> "$senderName set guest access to " +
                content.toModel<RoomGuestAccessContent>()!!.guestAccess
        EventType.STATE_ROOM_AVATAR -> "$senderName updated the room avatar"
        EventType.STATE_ROOM_POWER_LEVELS -> "$senderName set power levels"
        EventType.STATE_ROOM_MEMBER -> content.toModel<RoomMemberContent>()!!.let {
            val targetName = it.displayName ?: root.stateKey 
            when (it.membership) {
                Membership.JOIN -> "$senderName joined"
                Membership.BAN -> "$senderName banned $targetName"
                Membership.KNOCK -> "$senderName asked to join"
                Membership.INVITE -> "$senderName invited $targetName"
                Membership.LEAVE -> if (senderInfo.userId == root.stateKey) {
                    "$senderName left"
                } else {
                    "$senderName kicked $targetName"
                }
                else -> "$senderName unknown membership ${it.membership}"
            }
        }
        EventType.STATE_ROOM_ENCRYPTION -> "$senderName set encryption configurations"
        EventType.REDACTION -> "$senderName redacted a message"
        CustomEventType.Types.JITSI_CALL.matrixName -> {
            val callType = try {
                CallType.valueOf(content?.get("type")?.toString()?.uppercase() ?: "")
            } catch (_: IllegalArgumentException) {
                CallType.VOICE
            }
            "$senderName created a ${callType.name.lowercase()} call"
        }
        else -> root.run {
            getDecryptedTextSummary()?.let {
                when {
                    isTextMessage() -> if (isDirectRoom && !isSenderMyself) it else "$senderName: $it"
                    else -> "$senderName $it"
                }
            }
        }
    }
}

fun roomMemberQueryParamsByMemberships(memberships: List<Membership>): RoomMemberQueryParams {
    return RoomMemberQueryParams(
        displayName = QueryStringValue.NoCondition,
        memberships = memberships,
        userId = QueryStringValue.NoCondition,
        excludeSelf = false,
        displayNameOrUserId = QueryStringValue.NoCondition,
    )
}

fun RoomNotificationState.isMuted(): Boolean {
    return setOf(RoomNotificationState.MUTE, RoomNotificationState.MENTIONS_ONLY).contains(this)
}