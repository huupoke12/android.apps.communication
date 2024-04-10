package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer

@Composable
fun SignOutScreen(
    navigateToSplash: () -> Unit,
    modifier: Modifier = Modifier,
    signOutViewModel: SignOutViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val isSignedInState by signOutViewModel.isSignedIn.collectAsStateWithLifecycle(null)
    isSignedInState.let {
        LaunchedEffect(it) {
            when (it) {
                false -> navigateToSplash()
                true -> signOutViewModel.signOut()
                else -> {}
            }
        }
    }
    LoadingContainer(
        modifier = modifier,
        topContent = {
            Text("Signing out ...")
        }
    )
}