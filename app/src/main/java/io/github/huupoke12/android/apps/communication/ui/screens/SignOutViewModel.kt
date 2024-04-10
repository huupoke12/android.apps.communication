package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import kotlinx.coroutines.launch

class SignOutViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val isSignedIn = matrixService.isSignedIn()

    fun signOut() = viewModelScope.launch {
        matrixService.signOut()
    }
}