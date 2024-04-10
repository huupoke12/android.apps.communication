package io.github.huupoke12.android.apps.communication.ui.screens.home

import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.AvatarContainer
import io.github.huupoke12.android.apps.communication.ui.components.EmailField
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.SecretField
import io.github.huupoke12.android.apps.communication.ui.components.UserAvatar
import io.github.huupoke12.android.apps.communication.ui.components.UserProfileCard
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.util.Optional
import org.matrix.android.sdk.api.util.toMatrixItem

@Composable
fun AccountSubScreen(
    navigateToBlockListScreen: () -> Unit,
    navigateToUserScreen: (String) -> Unit,
    navigateToSignOutScreen: () -> Unit,
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val userState by accountViewModel.userLive.observeAsState(Optional.empty())
    val threePidsState by accountViewModel.threePidLive.observeAsState(emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.query(
                it, null, null, null, null
            )?.apply {
                moveToFirst()
                accountViewModel.setAvatar(
                    avatarUri = it,
                    avatarFileName = getString(getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                )
                close()
            }
        }
    }
    userState.getOrNull()?.let { user ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.isEditingName) {
                AlertDialog(
                    onDismissRequest = accountViewModel::stopEditingName,
                    confirmButton = {
                        Button(
                            onClick = accountViewModel::setDisplayName,
                            enabled = uiState.newName.isNotBlank(),
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = accountViewModel::stopEditingName,
                        ) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Change display name")
                    },
                    text = {
                        TextField(
                            value = uiState.newName,
                            onValueChange = accountViewModel::updateNameField,
                            modifier = Modifier.padding(16.dp),
                            label = {
                                Text("Display name")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                            )
                        )
                    }
                )
            } else if (uiState.isChangingPassword) {
                AlertDialog(
                    onDismissRequest = accountViewModel::stopChangingPassword,
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (accountViewModel.changePassword()) {
                                        Toast.makeText(
                                            context,
                                            "Password changed successfully",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            },
                            enabled = uiState.currentPassword.isNotBlank() &&
                                    uiState.newPassword.isNotBlank() &&
                                    uiState.newConfirmPassword == uiState.newPassword
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = accountViewModel::stopChangingPassword,
                        ) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Change password")
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SecretField(
                                value = uiState.currentPassword,
                                onValueChange = accountViewModel::updateCurrentPasswordField,
                                label = {
                                    Text("Current password")
                                },
                            )
                            SecretField(
                                value = uiState.newPassword,
                                onValueChange = accountViewModel::updateNewPasswordField,
                                label = {
                                    Text("New password")
                                },
                            )
                            SecretField(
                                value = uiState.newConfirmPassword,
                                onValueChange = accountViewModel::updateNewConfirmPasswordField,
                                label = {
                                    Text("Confirm password")
                                },
                                isError = uiState.newPassword != uiState.newConfirmPassword,
                                supportingText = if (uiState.newPassword != uiState.newConfirmPassword) {
                                    { Text("Confirm password must be the same as new password") }
                                } else null,
                            )
                            Text(
                                text = uiState.passwordChangingError,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                )
            } else if (uiState.isUpdatingAvatar) {
                AlertDialog(
                    onDismissRequest = accountViewModel::stopUpdatingAvatar,
                    confirmButton = {
                        Button(
                            onClick = accountViewModel::updateAvatar,
                            enabled = (uiState.avatarUri != null && uiState.avatarFileName != null),
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = accountViewModel::stopUpdatingAvatar,
                        ) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Update avatar")
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val avatarSize = AvatarSize.LARGE
                            AvatarContainer(
                                avatarSize = avatarSize,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                uiState.avatarUri?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = "Avatar",
                                    )
                                } ?: run {
                                    UserAvatar(
                                        userItem = user.toMatrixItem(),
                                        avatarSize = avatarSize,
                                        resolveAvatar = accountViewModel::resolveAvatar,
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    avatarPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Text("Choose file")
                            }
                            if (uiState.isUploadingAvatar) {
                                LoadingContainer(
                                    modifier = Modifier.padding(8.dp),
                                    fillMaxSize = false,
                                ) {
                                    Text("Uploading")
                                }
                            }
                        }
                    }
                )
            } else if (uiState.isRemovingAvatar) {
                AlertDialog(
                    onDismissRequest = accountViewModel::stopRemovingAvatar,
                    confirmButton = {
                        Button(
                            onClick = accountViewModel::removeAvatar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        ) {
                            Text("Remove")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = accountViewModel::stopRemovingAvatar,
                        ) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Remove avatar")
                    },
                    text = {
                        Text(
                            text = "Are you sure",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                )
            } else if (uiState.isChangingEmail) {
                AlertDialog(
                    onDismissRequest = accountViewModel::stopChangingEmail,
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (accountViewModel.checkNewEmailVerified()) {
                                        Toast.makeText(
                                            context,
                                            "Email changed succesfully",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }
                            },
                            enabled = uiState.emailSent,
                        ) {
                            Text("Check")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = accountViewModel::stopChangingEmail,
                        ) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Change email")
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            EmailField(
                                value = uiState.newEmail,
                                onValueChange = accountViewModel::updateEmailField,
                                modifier = Modifier.padding(16.dp),
                                label = { Text("New email") },
                            )
                            Button(
                                onClick = accountViewModel::sendVerificationEmail,
                                enabled = uiState.newEmail.isNotBlank(),
                            ) {
                                Text("Send verify email")
                            }
                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else if (uiState.emailSent) {
                                Text("Email sent.")
                            } else {
                                Text(
                                    text = uiState.emailChangeFailure,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                )
            }
            UserProfileCard(
                userItem = user.toMatrixItem(),
                onClick = {
                    navigateToUserScreen(user.userId)
                },
                resolveAvatar = accountViewModel::resolveAvatar,
            ) {
                threePidsState.forEach { threePid ->
                    Text(threePid.value)
                }
            }
            Divider()
            ListItem(
                headlineContent = {
                    Text("Edit display name")
                },
                modifier = Modifier.clickable(
                    onClick = { accountViewModel.startEditingName(user.displayName ?: "") },
                ),
                leadingContent = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                    )
                }
            )
            ListItem(
                headlineContent = {
                    Text("Update avatar")
                },
                modifier = Modifier.clickable(
                    onClick = accountViewModel::startUpdatingAvatar,
                ),
                leadingContent = {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                    )
                }
            )
            // FIXME: Wait for specs
//            if (!user.avatarUrl.isNullOrBlank()) {
//                ListItem(
//                    headlineContent = {
//                        Text("Remove avatar")
//                    },
//                    modifier = Modifier.clickable(
//                        onClick = accountViewModel::startRemovingAvatar,
//                    ),
//                    leadingContent = {
//                        Icon(
//                            Icons.Default.NoAccounts,
//                            contentDescription = null,
//                        )
//                    }
//                )
//            }
            ListItem(
                headlineContent = {
                    Text("Change password")
                },
                modifier = Modifier.clickable(
                    onClick = accountViewModel::startChangingPassword,
                ),
                leadingContent = {
                    Icon(
                        Icons.Default.Password,
                        contentDescription = null,
                    )
                }
            )
//            FIXME
//            ListItem(
//                headlineContent = {
//                    Text("Change email")
//                },
//                modifier = Modifier.clickable(
//                    onClick = accountViewModel::startChangingEmail,
//                ),
//                leadingContent = {
//                    Icon(
//                        Icons.Default.Email,
//                        contentDescription = null,
//                    )
//                }
//            )
            ListItem(
                headlineContent = {
                    Text("View blocked users")
                },
                modifier = Modifier.clickable(
                    onClick = navigateToBlockListScreen,
                ),
                leadingContent = {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = null,
                    )
                }
            )
            ListItem(
                headlineContent = {
                    Text("Sign out")
                },
                modifier = Modifier
                    .clickable(onClick = navigateToSignOutScreen),
                leadingContent = {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                    )
                },
                colors = ListItemDefaults.colors(
                    headlineColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                ),
            )
        }
    } ?: run {
        LoadingContainer()
    }
}