package io.github.huupoke12.android.apps.communication.ui.screens.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.huupoke12.android.apps.communication.data.ServiceLocator
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.failure.Failure

class AccountViewModel(): ViewModel() {
    private val matrixService = ServiceLocator.matrixService
    val userLive = matrixService.getLiveUser(matrixService.getMyUserId())
    val threePidLive = matrixService.getLiveThreePids()
    val uiState = MutableStateFlow(AccountUiState())

    fun startEditingName(currentName: String) {
        uiState.value = uiState.value.copy(
            isEditingName = true,
            newName = currentName,
        )
    }

    fun stopEditingName() {
        uiState.value = uiState.value.copy(
            isEditingName = false,
            newName = "",
        )
    }

    fun updateNameField(name: String) {
        uiState.value = uiState.value.copy(
            newName = name
        )
    }

    fun setDisplayName() = viewModelScope.launch {
        matrixService.setDisplayName(
            uiState.value.newName,
        )
        stopEditingName()
    }

    fun startChangingPassword() {
        uiState.update {
            it.copy(
                isChangingPassword = true,
            )
        }
    }

    fun startChangingEmail() {
        uiState.update {
            it.copy(
                isChangingEmail = true,
            )
        }
        viewModelScope.launch { matrixService.cancelCurrentPendingEmailThreePid() }
    }

    fun stopChangingEmail() {
        viewModelScope.launch {
            matrixService.cancelCurrentPendingEmailThreePid()
        }
        uiState.update {
            it.copy(
                isChangingEmail = false,
                newEmail = "",
                isLoading = false,
                emailSent = false,
            )
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                )
            }
            try {
                matrixService.sendNewEmailVerification(
                    email = uiState.value.newEmail
                )
                uiState.update {
                    it.copy(
                        emailSent = true,
                    )
                }
            } catch (e: Failure.ServerError) {
                uiState.update {
                    it.copy(
                        emailChangeFailure = e.error.message,
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

    suspend fun checkNewEmailVerified(): Boolean {
        uiState.update {
            it.copy(
                isLoading = true,
            )
        }
        try {
            matrixService.finaliseChangingEmail()
            stopChangingEmail()
            return true
        } catch (e: Failure.ServerError) {
            uiState.update {
                it.copy(
                    emailChangeFailure = e.error.message,
                    isLoading = false,
                )
            }
            return false
        }
    }

    fun stopChangingPassword() {
        uiState.update {
            it.copy(
                isChangingPassword = false,
                currentPassword = "",
                newPassword = "",
                newConfirmPassword = "",
                passwordChangingError = "",
            )
        }
    }

    fun updateCurrentPasswordField(currentPassword: String) {
        uiState.update {
            it.copy(
                currentPassword = currentPassword
            )
        }
    }

    fun updateNewPasswordField(newPassword: String) {
        uiState.update {
            it.copy(
                newPassword = newPassword
            )
        }
    }

    fun updateNewConfirmPasswordField(newConfirmPassword: String) {
        uiState.update {
            it.copy(
                newConfirmPassword = newConfirmPassword
            )
        }
    }

    fun updateEmailField(newEmail: String) {
        uiState.update {
            it.copy(
                newEmail = newEmail
            )
        }
    }


    suspend fun changePassword(): Boolean {
        uiState.value.let { it ->
            try {
                matrixService.changePassword(
                    password = it.currentPassword,
                    newPassword = it.newPassword,
                )
                stopChangingPassword()
                return true
            } catch (e: Failure.ServerError) {
                uiState.update {
                    it.copy(
                        passwordChangingError = e.error.message,
                    )
                }
                return false
            }
        }
    }

    fun startUpdatingAvatar() {
        uiState.update {
            it.copy(
                isUpdatingAvatar = true,
                isUploadingAvatar = false,
                avatarUri = null,
                avatarFileName = null,
            )
        }
    }

    fun stopUpdatingAvatar() {
        uiState.update {
            it.copy(
                isUpdatingAvatar = false,
                isUploadingAvatar = false,
                avatarUri = null,
                avatarFileName = null,
            )
        }
    }

    fun setAvatar(avatarUri: Uri, avatarFileName: String) {
        uiState.update {
            it.copy(
                avatarUri = avatarUri,
                avatarFileName = avatarFileName,
            )
        }
    }

    fun startRemovingAvatar() {
        uiState.update {
            it.copy(
                isRemovingAvatar = true,
            )
        }
    }

    fun stopRemovingAvatar() {
        uiState.update {
            it.copy(
                isRemovingAvatar = false,
            )
        }
    }

    fun removeAvatar() = viewModelScope.launch {
        matrixService.removeUserAvatar()
    }

    fun updateAvatar() = viewModelScope.launch {
        uiState.update {
            it.copy(
                isUploadingAvatar = true
            )
        }
        matrixService.setUserAvatar(
            avatarUri = uiState.value.avatarUri!!,
            fileName = uiState.value.avatarFileName!!,
        )
        stopUpdatingAvatar()
    }

    fun resolveAvatar(uri: String, size: AvatarSize) = matrixService.resolveAvatarThumbnail(uri, size)
}

data class AccountUiState(
    val isEditingName: Boolean = false,
    val newName: String = "",
    val isUpdatingAvatar: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val isRemovingAvatar: Boolean = false,
    val avatarUri: Uri? = null,
    val avatarFileName: String? = null,
    val isChangingPassword: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val newConfirmPassword: String = "",
    val passwordChangingError: String = "",
    val isChangingEmail: Boolean = false,
    val newEmail: String = "",
    val emailChangeFailure: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
)