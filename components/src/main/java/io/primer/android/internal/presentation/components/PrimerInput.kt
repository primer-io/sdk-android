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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
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
    forceLtrForNumbers: Boolean = false,
    onFocusChange: ((Boolean) -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedFocus,
        unfocusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
    ),
) {
    val layoutDirection = LocalLayoutDirection.current

    // Determine if we should force LTR direction
    val shouldForceLtr = forceLtrForNumbers && (
        keyboardOptions.keyboardType == KeyboardType.Number ||
            keyboardOptions.keyboardType == KeyboardType.Phone
        )

    // Use LTR layout for numeric inputs to ensure proper cursor positioning and formatting
    val textFieldLayoutDirection = if (shouldForceLtr) LayoutDirection.Ltr else layoutDirection

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            textAlign = if (layoutDirection == LayoutDirection.Rtl) TextAlign.End else TextAlign.Start,
        )

        Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))

        CompositionLocalProvider(LocalLayoutDirection provides textFieldLayoutDirection) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .let { modifier ->
                        onFocusChange?.let { focusCallback ->
                            modifier.onFocusChanged { focusState ->
                                focusCallback(focusState.isFocused)
                            }
                        } ?: modifier
                    },
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
        }

        error?.let {
            Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
            Text(
                text = it,
                style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextNegative,
                textAlign = if (layoutDirection == LayoutDirection.Rtl) TextAlign.End else TextAlign.Start,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
            Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
        }
    }
}
