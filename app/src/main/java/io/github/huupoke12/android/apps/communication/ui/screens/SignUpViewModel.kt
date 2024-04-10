package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.auth.registration.RegistrationResult
import org.matrix.android.sdk.api.auth.registration.Stage
import org.matrix.android.sdk.api.failure.Failure

class SignUpViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val uiState = MutableStateFlow(SignUpUiState())

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

    fun updateConfirmPassword(confirmPassword: String) {
        uiState.value = uiState.value.copy(
            confirmPassword = confirmPassword
        )
    }

    fun updateEmail(email: String) {
        uiState.update {
            it.copy(
                email = email
            )
        }
    }

    fun checkIfEmailVerified() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    noticeText = "",
                    failureText = "",
                )
            }
            try {
                if (matrixService.checkIfRegistrationEmailHasBeenValidated() is RegistrationResult.Success) {
                    uiState.update {
                        it.copy(
                            success = true
                        )
                    }
                } else {
                    uiState.update {
                        it.copy(
                            failureText = "Failed to verify email",
                        )
                    }
                }
            } catch (e: Failure.ServerError) {
                uiState.update {
                    it.copy(
                        failureText = e.error.message,
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

    fun sendVerifyEmail() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    noticeText = "",
                    failureText = "",
                )
            }
            try {
                matrixService.addRegistrationEmail(uiState.value.email)
                uiState.update {
                    it.copy(
                        emailSent = true,
                        noticeText = "Email sent. Please check your inbox.",
                    )
                }
            } catch (e: Failure.ServerError) {
                if (e.error.message.lowercase() == "Unable to get validated threepid".lowercase()) {
                    uiState.update {
                        it.copy(
                            emailSent = true,
                            noticeText = "Email sent. Please check your inbox.",
                        )
                    }
                } else {
                    uiState.update {
                        it.copy(
                            failureText = e.error.message,
                        )
                    }
                }

            }
            uiState.update {
                it.copy(
                    isLoading = false
                )
            }
        }
    }

    fun signUp() = viewModelScope.launch {
        uiState.update {
            it.copy(
                noticeText = "",
                failureText = "",
                isLoading = true,
            )
        }
        try {
            val result = matrixService.signUp(
                username = uiState.value.username,
                password = uiState.value.password,
                email = uiState.value.email,
            )
            if (result is RegistrationResult.Success) {
                uiState.update {
                    it.copy(
                        success = true
                    )
                }
            } else if (
                result is RegistrationResult.FlowResponse &&
                result.flowResult.missingStages.firstOrNull() is Stage.Email
            ) {
                uiState.update {
                    it.copy(
                        isVerifyingEmail = true,
                    )
                }
            } else {
                uiState.value = uiState.value.copy(
                    failureText = "Sign up failed."
                )
            }
        } catch (e: Failure.ServerError) {
            uiState.update {
                it.copy(
                    failureText = e.error.message,
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

data class SignUpUiState (
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val email: String = "",
    val isVerifyingEmail: Boolean = false,
    val isLoading: Boolean = false,
    val noticeText: String = "",
    val failureText: String = "",
    val emailSent: Boolean = false,
    val success: Boolean = false,
)