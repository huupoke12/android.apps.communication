package io.github.huupoke12.android.apps.communication.data.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.data.models.JitsiRoomInfo
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.CallType
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.matrix.android.sdk.api.util.toMatrixItem
import java.net.MalformedURLException
import java.net.URL

class JitsiService(
) {
    private val defaultOptions = JitsiMeetConferenceOptions.Builder()
        .setFeatureFlag("add-people.enabled", false)
        .setFeatureFlag("breakout-rooms.enabled", false)
        .setFeatureFlag("calendar.enabled", false)
        .setFeatureFlag("chat.enabled", true)
        .setFeatureFlag("car-mode.enabled", false)
        .setFeatureFlag("invite.enabled", false)
        .setFeatureFlag("lobby-mode.enabled", false)
        .setFeatureFlag("prejoinpage.enabled", false)
        .setFeatureFlag("prejoinpage.hideDisplayName", false)
        .setFeatureFlag("raise-hand.enabled", false)
        .setFeatureFlag("reactions.enabled", false)
        .setFeatureFlag("security-options.enabled", false)
        .setFeatureFlag("settings.enabled", false)
        .setFeatureFlag("welcomepage.enabled", false)
        .setConfigOverride("p2p.enabled", true)
        .setConfigOverride("p2p.backToP2PDelay", 5)
        .build()

    init {
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)
    }

    fun getIntent(context: Context, options: JitsiMeetConferenceOptions): Intent {
        val intent = Intent(context, JitsiMeetActivity::class.java)
        intent.action = "org.jitsi.meet.CONFERENCE"
        intent.putExtra("JitsiMeetConferenceOptions", options)
        if (context !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return intent
    }

    fun getOptions(
        roomInfo: JitsiRoomInfo,
    ): JitsiMeetConferenceOptions {
        val userItem = ServiceLocator.matrixService.getMyUserInfo().toMatrixItem()
        val userInfo = JitsiMeetUserInfo().apply {
            displayName = userItem.displayNameOrUsername()
            try {
                if (!userItem.avatarUrl.isNullOrBlank()) {
                    avatar = URL(ServiceLocator.matrixService.resolveAvatarThumbnail(
                        contentUrl = userItem.avatarUrl,
                        size = AvatarSize.LARGER,
                    ))
                }
            } catch (_: MalformedURLException) {

            }
        }
        return JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL(roomInfo.serverUrl))
            .setRoom(roomInfo.id)
            .setSubject(roomInfo.displayName.orEmpty())
            .setUserInfo(userInfo)
            .setAudioOnly(roomInfo.callType == CallType.VOICE)
            .build()
    }

}
