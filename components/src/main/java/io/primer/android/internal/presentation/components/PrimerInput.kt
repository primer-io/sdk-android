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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
private fun buildAccessibilityLabel(accessibilityLabel: String?, isRequired: Boolean): String? {
    val requiredText = if (isRequired) stringResource(R.string.accessibility_common_required) else null
    return buildList {
        accessibilityLabel?.let { add(it) }
        requiredText?.let { add(it) }
    }.joinToString(", ").ifEmpty { null }
}

private fun resolveTextFieldLayoutDirection(
    forceLtrForNumbers: Boolean,
    keyboardOptions: KeyboardOptions,
    layoutDirection: LayoutDirection,
): LayoutDirection {
    val shouldForceLtr = forceLtrForNumbers && (
        keyboardOptions.keyboardType == KeyboardType.Number ||
            keyboardOptions.keyboardType == KeyboardType.Phone
        )
    return if (shouldForceLtr) LayoutDirection.Ltr else layoutDirection
}

@Composable
internal fun PrimerInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    forceLtrForNumbers: Boolean = false,
    onFocusChange: ((Boolean) -> Unit)? = null,
    accessibilityLabel: String?,
    isRequired: Boolean = false,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
        unfocusedContainerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
        disabledContainerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
        errorContainerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
        focusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedFocus,
        unfocusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
    ),
) {
    val fullAccessibilityLabel = buildAccessibilityLabel(accessibilityLabel, isRequired)
    val layoutDirection = LocalLayoutDirection.current
    val textFieldLayoutDirection = resolveTextFieldLayoutDirection(forceLtrForNumbers, keyboardOptions, layoutDirection)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .contentDescriptionIfNotNull(fullAccessibilityLabel),
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                textAlign = if (layoutDirection == LayoutDirection.Rtl) TextAlign.End else TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
        }

        CompositionLocalProvider(LocalLayoutDirection provides textFieldLayoutDirection) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChangedIfNotNull(onFocusChange),
                placeholder = {
                    placeholder?.let {
                        Text(text = it, color = LocalPrimerTheme.current.colorTokens().primerColorTextPlaceholder)
                    }
                },
                singleLine = true,
                // isError=false to prevent Material's TalkBack announcements
                // Error announcement handled solely by ErrorSection with liveRegion
                isError = false,
                trailingIcon = trailingIcon,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                enabled = enabled,
                readOnly = readOnly,
                shape = RoundedCornerShape(LocalPrimerTheme.current.radiusTokens.small),
                // Custom colors to show error border since isError=false
                colors = if (error != null) {
                    val errorColor = LocalPrimerTheme.current.colorTokens().primerColorTextNegative
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = errorColor,
                        unfocusedBorderColor = errorColor,
                    )
                } else {
                    colors
                },
            )
        }

        error?.let { ErrorSection(it, layoutDirection) }
    }
}

@Composable
private fun ErrorSection(errorText: String, layoutDirection: LayoutDirection) {
    Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
    Text(
        text = errorText,
        style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
        color = LocalPrimerTheme.current.colorTokens().primerColorTextNegative,
        textAlign = if (layoutDirection == LayoutDirection.Rtl) TextAlign.End else TextAlign.Start,
        // Announce error via liveRegion (Material's isError is disabled)
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun PrimerInputEmptyPreview() = PreviewContainer {
    PrimerInput(
        value = "",
        onValueChange = {},
        label = "Card Number",
        placeholder = "1234 5678 9012 3456",
        accessibilityLabel = "",
    )
}

@Preview(name = "Filled", showBackground = true)
@Composable
private fun PrimerInputFilledPreview() = PreviewContainer {
    PrimerInput(
        value = "4242 4242 4242 4242",
        onValueChange = {},
        label = "Card Number",
        accessibilityLabel = "",
    )
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun PrimerInputErrorPreview() = PreviewContainer {
    PrimerInput(
        value = "1234",
        onValueChange = {},
        label = "Card Number",
        error = "Invalid card number",
        accessibilityLabel = "",
    )
}

@Preview(name = "Disabled", showBackground = true)
@Composable
private fun PrimerInputDisabledPreview() = PreviewContainer {
    PrimerInput(
        value = "4242 4242 4242 4242",
        onValueChange = {},
        label = "Card Number",
        enabled = false,
        accessibilityLabel = "",
    )
}
