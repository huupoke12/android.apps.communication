package io.github.huupoke12.android.apps.communication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.EmailField
import io.github.huupoke12.android.apps.communication.ui.components.SecretField

@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    resetPasswordViewModel: ResetPasswordViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by resetPasswordViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (uiState.success) {
        LaunchedEffect(null) {
            Toast.makeText(context, "Password reset successfully", Toast.LENGTH_SHORT).show()
            navigateBack()
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Reset password",
            style = MaterialTheme.typography.headlineLarge,
        )
        if (!uiState.emailSent) {
            EmailField(
                value = uiState.email,
                onValueChange = resetPasswordViewModel::updateEmailField,
            )
            Button(
                onClick = resetPasswordViewModel::sendResetEmail,
                enabled = uiState.email.isNotBlank(),
            ) {
                Text("Send reset email")
            }
        } else {
            SecretField(
                value = uiState.password,
                onValueChange = resetPasswordViewModel::updatePasswordField,
                label = { Text("New password") }
            )
            SecretField(
                value = uiState.confirmPassword,
                onValueChange = resetPasswordViewModel::updateConfirmPasswordField,
                label = { Text("Confirm new password") },
                supportingText = {
                    if (uiState.password != uiState.confirmPassword) {
                        Text(
                            text = "Confirmation password must be the same as new password",
                        )
                    }
                },
                isError = uiState.password != uiState.confirmPassword,
            )
            Text(
                text = "Email sent.\nPlease confirm the mail first before clicking reset here."
            )
            Button(
                onClick = resetPasswordViewModel::resetPassword,
                enabled = uiState.password.isNotBlank() && uiState.confirmPassword.isNotBlank() &&
                        uiState.password == uiState.confirmPassword
            ) {
                Text("Reset password")
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.noticeText.isNotBlank()) {
            Text(uiState.noticeText)
        } else {
            Text(
                text = uiState.failureText,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TextButton(
            onClick = navigateBack,
        ) {
            Text("Go back")
        }
    }
}