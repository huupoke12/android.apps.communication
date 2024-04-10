package io.github.huupoke12.android.apps.communication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CommunicationApp(
) {
    val navController = rememberNavController()
    MainNavHost(navController = navController)
}