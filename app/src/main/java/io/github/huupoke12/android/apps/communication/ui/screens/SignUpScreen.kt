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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.EmailField
import io.github.huupoke12.android.apps.communication.ui.components.SecretField
import io.github.huupoke12.android.apps.communication.ui.components.UsernameField

@Composable
fun SignUpScreen(
    navigateBackToSplash: () -> Unit,
    modifier: Modifier = Modifier,
    signUpViewModel: SignUpViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by signUpViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (uiState.success) {
        LaunchedEffect(null) {
            Toast.makeText(context, "Signed up successfully", Toast.LENGTH_SHORT).show()
            navigateBackToSplash()
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineLarge,
        )
        if (!uiState.isVerifyingEmail) {
            UsernameField(
                username = uiState.username,
                onValueChange = signUpViewModel::updateUsername,
            )
            SecretField(
                value = uiState.password,
                onValueChange = signUpViewModel::updatePassword,
                label = { Text("Password") }
            )
            SecretField(
                value = uiState.confirmPassword,
                onValueChange = signUpViewModel::updateConfirmPassword,
                label = { Text("Confirm password") },
                supportingText = {
                    if (uiState.password != uiState.confirmPassword) {
                        Text(
                            text = "Confirmation password must be the same as password",
                        )
                    }
                },
                isError = uiState.password != uiState.confirmPassword,
            )
            Button(
                onClick = signUpViewModel::signUp,
                enabled = uiState.username.isNotBlank() && uiState.password.isNotBlank() &&
                        uiState.password == uiState.confirmPassword
            ) {
                Text("Sign up")
            }
        } else {
            EmailField(
                value = uiState.email,
                onValueChange = signUpViewModel::updateEmail,
            )
            OutlinedButton(
                onClick = signUpViewModel::sendVerifyEmail,
                enabled = uiState.email.isNotBlank(),
            ) {
                Text("Send verify email")
            }
            Button(
                onClick = signUpViewModel::checkIfEmailVerified,
                enabled = uiState.emailSent,
            ) {
                Text("Check for verification status")
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.noticeText.isNotBlank()) {
            Text(uiState.noticeText, modifier = Modifier.align(CenterHorizontally))
        } else if (uiState.failureText.isNotBlank()) {
            Text(
                text = uiState.failureText,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TextButton(
            onClick = navigateBackToSplash,
        ) {
            Text("Back to home screen")
        }
    }
}