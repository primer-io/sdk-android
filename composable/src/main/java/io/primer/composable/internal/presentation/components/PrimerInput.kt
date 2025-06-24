package io.primer.composable.internal.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PrimerInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    error: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly : Boolean = false,
    enabled : Boolean = true,
    colors : TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { placeholder?.let { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        readOnly = readOnly,
        colors = colors
    )
}
