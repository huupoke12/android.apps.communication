package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.ViewModel
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize

class BlockListViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val blockedUserListLive = matrixService.getLiveIgnoredUserList()
    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}
