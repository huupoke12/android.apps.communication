package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer

@Composable
fun SplashScreen(
    onLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(null) {
        onLoad()
    }
    LoadingContainer(modifier = modifier)
}