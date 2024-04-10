package io.github.huupoke12.android.apps.communication.data.models

import io.github.huupoke12.android.apps.communication.util.CallType
import org.matrix.android.sdk.api.session.events.model.Content

data class JitsiRoomInfo(
    val serverUrl: String,
    val id: String,
    val displayName: String?,
    val callType: CallType,
)

fun Content.toJitsiRoomInfo(): JitsiRoomInfo? {
    val serverUrl = this["server_url"]
    val roomId = this["room_id"]
    val displayName = this["display_name"]
    return if (serverUrl != null && roomId != null) {
        val callType = try {
            CallType.valueOf(this["type"]?.toString()?.uppercase().orEmpty())
        } catch (_: IllegalArgumentException) {
            CallType.VOICE
        }
        JitsiRoomInfo(
            serverUrl = serverUrl.toString(),
            id = roomId.toString(),
            displayName = displayName?.toString().orEmpty(),
            callType = callType,
        )
    } else {
        null
    }
}

fun JitsiRoomInfo.toContent(): Content {
    return mapOf(
        "server_url" to serverUrl,
        "room_id" to id,
        "display_name" to displayName.orEmpty(),
        "type" to callType.name.lowercase(),
    )
}