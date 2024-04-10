package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.failure.Failure

class ResetPasswordViewModel(): ViewModel() {
    val matrixService = ServiceLocator.matrixService
    val uiState = MutableStateFlow(ResetPasswordUiState())

    fun updateEmailField(email: String) {
        uiState.update {
            it.copy(
                email = email,
            )
        }
    }

    fun updatePasswordField(password: String) {
        uiState.update {
            it.copy(
                password = password,
            )
        }
    }

    fun updateConfirmPasswordField(confirmPassword: String) {
        uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
            )
        }
    }

    fun sendResetEmail() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    failureText = "",
                    noticeText = "",
                )
            }
            try {
                matrixService.sendResetEmail(uiState.value.email)
                uiState.update {
                    it.copy(
                        emailSent = true,
                    )
                }
            } catch (e: Failure.ServerError) {
                uiState.update {
                    it.copy(
                        failureText = e.error.message
                    )
                }
            }
            uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    failureText = "",
                    noticeText = "",
                )
            }
            try {
                matrixService.resetPasswordMailConfirmed(uiState.value.password)
                uiState.update {
                    it.copy(
                        success = true,
                    )
                }
            } catch (e: Failure.ServerError) {
                uiState.update {
                    it.copy(
                        failureText = e.error.message
                    )
                }
            }
            uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }
}

data class ResetPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val failureText: String = "",
    val noticeText: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val success: Boolean = false,
)