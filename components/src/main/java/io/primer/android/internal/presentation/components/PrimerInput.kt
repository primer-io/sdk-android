package io.primer.android.internal.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import io.primer.android.LocalPrimerTheme

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
    readOnly: Boolean = false,
    enabled: Boolean = true,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedFocus,
        unfocusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
    ),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
        )

        Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { placeholder?.let { Text(it) } },
            singleLine = true,
            isError = error != null,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(LocalPrimerTheme.current.radiusTokens.small),
            colors = colors,
        )

        error?.let {
            Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
            Text(
                text = it,
                style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextNegative,
            )
            Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
        }
    }
}
