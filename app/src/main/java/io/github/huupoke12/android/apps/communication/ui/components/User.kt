package io.github.huupoke12.android.apps.communication.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.huupoke12.android.apps.communication.R
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.displayNameOrUsername
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem

@Composable
fun UserListItem(
    userItem: MatrixItem.UserItem,
    resolveAvatar: (String, AvatarSize) -> String?,
    navigateToUserScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    isMyself: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(userItem.displayNameOrUsername() + if (isMyself) " (You)" else "")
        },
        modifier = modifier.clickable(onClick = { navigateToUserScreen(userItem.id) }),
        leadingContent = {
             UserAvatar(
                 userItem = userItem,
                 resolveAvatar = resolveAvatar,
             )
        },
        supportingContent = {
            Text(userItem.id)
        },
        trailingContent = trailingContent,
    )
}

@Composable
fun UserList(
    userList: List<User>,
    resolveAvatar: (String, AvatarSize) -> String?,
    modifier: Modifier = Modifier,
    navigateToUserScreen: (String) -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
    displayCount: Boolean = false,
) {
    LazyColumn(
        modifier = modifier
    ) {
        if (displayCount) {
            item {
                Text(
                    text = "There are ${userList.size} users",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(userList) { user ->
            UserListItem(
                userItem = user.toMatrixItem(),
                navigateToUserScreen = { navigateToUserScreen(user.userId) },
                trailingContent = trailingContent,
                resolveAvatar =  resolveAvatar,
            )
        }
    }
}

@Composable
fun UserProfileCard(
    userItem: MatrixItem.UserItem,
    resolveAvatar: (String, AvatarSize) -> String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    additionalContent: @Composable (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Column(
        modifier = modifier
            .clickable(onClick = onClick ?: {
                clipboardManager.setText(AnnotatedString(userItem.id))
                Toast.makeText(context, R.string.clipboard_copied, Toast.LENGTH_SHORT).show()
            })
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserAvatar(
            userItem = userItem,
            modifier = Modifier.padding(vertical = 8.dp),
            avatarSize = AvatarSize.LARGE,
            resolveAvatar = resolveAvatar,
        )
        Text(
            text = userItem.displayNameOrUsername(),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = userItem.id,
        )
        additionalContent?.invoke()
    }
}