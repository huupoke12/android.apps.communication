package io.github.huupoke12.android.apps.communication.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun IconText(
    imageVector: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textDecoration: TextDecoration? = null,
    color: Color = Color.Unspecified,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = if (color != Color.Unspecified) color else LocalContentColor.current,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            color = color,
        )
    }
}

@Composable
fun IconTextBlock(
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
            )
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = if (color != Color.Unspecified) color else LocalContentColor.current,
        )
        Text(
            text = text,
            color = color,
        )
    }
}

@Composable
fun RoundedIconTextAction(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = LocalContentColor.current,
    textColor: Color = Color.Unspecified,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .sizeIn(minWidth = 64.dp, minHeight = 64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .background(backgroundColor)
                .padding(8.dp),
            tint = iconColor,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun ConnectionStatusDisplay(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isOnline) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconText(
                imageVector = Icons.Default.CloudOff,
                text = "Connection error",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun DividerWithContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        content()
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onQueryClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var active by rememberSaveable {
        mutableStateOf(false)
    }
    androidx.compose.material3.SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = active,
        onActiveChange = { active = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (active) {
                Row() {
                    IconButton(
                        onClick = onQueryClear,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Clear query"
                        )
                    }
                    IconButton(
                        onClick = { active = false },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close search"
                        )
                    }
                }
            }
        }
    ) {
        content()
    }
}

@Composable
fun AddRemoveButton(
    isAdded: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = if (!isAdded) onAdd else onRemove,
        modifier = modifier,
        colors = if (!isAdded) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        IconText(
            text = if (!isAdded) "Add" else "Remove",
            imageVector = if (!isAdded) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
        )
    }
}

@Composable
fun ReverseLayout(
    reverse: Boolean = true,
    content: @Composable () -> Unit,
) {
    val targetLayout = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> LayoutDirection.Rtl
        LayoutDirection.Rtl -> LayoutDirection.Ltr
    }
    if (reverse) {
        CompositionLocalProvider(LocalLayoutDirection provides targetLayout) {
            content()
        }
    } else {
        content()
    }

}

@Composable
fun Centered(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun LoadingContainer(
    modifier: Modifier = Modifier,
    fillMaxSize: Boolean = true,
    topContent: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = if (fillMaxSize) modifier.fillMaxSize() else modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topContent?.let { it() }
            CircularProgressIndicator(
            )
        }

    }
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun VideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = false,
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            this.playWhenReady = playWhenReady
            prepare()
        }
    }
    DisposableEffect(null) {
        onDispose {
            player.release()
        }
    }
    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            player.pause()
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PlayerView(viewContext)
        },
        update = { playerView ->
            playerView.player = player
        }
    )
}