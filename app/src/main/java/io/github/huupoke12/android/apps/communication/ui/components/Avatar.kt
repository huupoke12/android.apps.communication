package io.github.huupoke12.android.apps.communication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.colorHash
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import io.github.huupoke12.android.apps.communication.util.getAvatarInitials
import io.github.huupoke12.android.apps.communication.util.getForegroundColorFromBackgroundColor
import org.matrix.android.sdk.api.util.MatrixItem

@Composable
fun AvatarContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    avatarSize: AvatarSize = AvatarSize.SMALL,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(avatarSize.size.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        content()
    }
}


@Composable
private fun Avatar(
    item: MatrixItem,
    resolveAvatar: (String, AvatarSize) -> String?,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.SMALL,
    placeholder: @Composable () -> Unit,
) {
    val backgroundColor = colorHash(item.id)
    val foregroundColor = getForegroundColorFromBackgroundColor(backgroundColor)
    AvatarContainer(
        modifier = modifier,
        backgroundColor = backgroundColor,
        avatarSize = avatarSize,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides foregroundColor
        ) {
            ProvideTextStyle(
                value = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (avatarSize.size / 2).sp,
                )
            ) {
                val avatarUrl: String? = if (!item.avatarUrl.isNullOrBlank()) {
                    resolveAvatar(item.avatarUrl!!, avatarSize)
                } else null
                avatarUrl.takeUnless{ it.isNullOrBlank() }?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } ?: placeholder()
            }
        }
    }
}

@Composable
fun RoomAvatar(
    roomItem: MatrixItem,
    resolveAvatar: (String, AvatarSize) -> String?,
    directUserItem: MatrixItem.UserItem?,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.SMALL,
) {
    directUserItem?.let {
        UserAvatar(
            userItem = it,
            resolveAvatar = resolveAvatar,
            modifier = modifier,
            avatarSize = avatarSize,
        )
    } ?: run {
        Avatar(
            item = roomItem,
            resolveAvatar = resolveAvatar,
            modifier = modifier,
            avatarSize = avatarSize,
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size((avatarSize.size / 2).dp)
            )
        }
    }
}

@Composable
fun UserAvatar(
    userItem: MatrixItem.UserItem,
    resolveAvatar: (String, AvatarSize) -> String?,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.SMALL,
) {
    Avatar(
        item = userItem,
        resolveAvatar = resolveAvatar,
        modifier = modifier,
        avatarSize = avatarSize,
    ) {
        Text(getAvatarInitials(userItem.displayNameOrUsername()))
    }
}