package io.github.huupoke12.android.apps.communication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import io.github.huupoke12.android.apps.communication.ui.components.LoadingContainer
import io.github.huupoke12.android.apps.communication.ui.components.SecretField
import io.github.huupoke12.android.apps.communication.ui.components.UsernameField

@Composable
fun SignInScreen(
    navigateToSignUp: () -> Unit,
    navigateToResetPassword: () -> Unit,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    signInViewModel: SignInViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by signInViewModel.uiState.collectAsStateWithLifecycle()
    val isSignedInState by signInViewModel.isSingedIn.collectAsStateWithLifecycle(null)
    val context = LocalContext.current
    when (isSignedInState) {
        null -> LoadingContainer()
        true -> LaunchedEffect(null) {
                    if (uiState.isManualSignIn) {
                        Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
                    }
                    onSignedIn()
        }
        false -> Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.headlineLarge,
            )
            UsernameField(
                username = uiState.username,
                onValueChange = signInViewModel::updateUsername,
            )
            SecretField(
                value = uiState.password,
                onValueChange = signInViewModel::updatePassword,
                label = { Text("Password") }
            )
            Button(
                onClick = signInViewModel::signIn,
                enabled = uiState.username.isNotBlank() && uiState.password.isNotBlank()
            ) {
                Text("Sign in")
            }
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = uiState.failureText,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Don't have an account?")
            TextButton(
                onClick = navigateToSignUp,
            ) {
                Text("Sign up")
            }
            Text("Forget password?")
            TextButton(
                onClick = navigateToResetPassword,
            ) {
                Text("Reset password")
            }
        }
    }
}