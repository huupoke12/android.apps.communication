package io.github.huupoke12.android.apps.communication.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun UsernameField(
    username: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = username,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text("Username") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
        ),
    )
}

@Composable
fun SecretField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        supportingText = supportingText,
        isError = isError,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
        ),
        trailingIcon = {
            IconButton(
                onClick = { visible = !visible },
            ) {
                if (visible) {
                    Icon(Icons.Default.VisibilityOff, "Hide")
                } else {
                    Icon(Icons.Default.Visibility, "Show")
                }
            }
        }
    )
}

@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = { Text("Email") },
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
) {
    TextField(
        value = value, onValueChange = onValueChange,
        label = label,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
        ),
        trailingIcon = trailingIcon,
        singleLine = true,
        readOnly = readOnly,
        modifier = modifier,
    )
}