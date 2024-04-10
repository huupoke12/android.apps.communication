package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.failure.Failure

class SignInViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val uiState = MutableStateFlow(SignInUiState())
    val isSingedIn = matrixService.isSignedIn()

    fun updateUsername(username: String) {
        uiState.value = uiState.value.copy(
            username = username
        )
    }

    fun updatePassword(password: String) {
        uiState.value = uiState.value.copy(
            password = password
        )
    }


    fun signIn() = viewModelScope.launch {
        uiState.update {
            it.copy(
                isLoading = true,
                failureText = "",
            )
        }
        try {
            matrixService.signIn(
                username = uiState.value.username,
                password = uiState.value.password,
            )
            uiState.update {
                it.copy(
                    isManualSignIn = true
                )
            }
        } catch (e: Failure.ServerError) {
            uiState.value = uiState.value.copy(
                failureText = e.error.message
            )
        }
        uiState.update {
            it.copy(
                isLoading = false,
            )
        }
    }
}

data class SignInUiState(
    val isManualSignIn: Boolean = false,
    val username: String = "",
    val password: String = "",
    val failureText: String = "",
    val isLoading: Boolean = false,
)