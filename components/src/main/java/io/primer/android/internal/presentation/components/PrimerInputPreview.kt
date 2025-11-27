@file:Suppress("UnusedPrivateMember")

package io.primer.android.internal.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme

/**
 * Preview wrapper that provides PrimerTheme and MaterialTheme with consistent styling.
 */
@Composable
private fun PreviewContainer(
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPrimerTheme provides PrimerTheme(),
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme {
            val theme = LocalPrimerTheme.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(theme.spacingTokens.large),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.large),
            ) {
                content()
            }
        }
    }
}

/**
 * Section title text for preview grouping.
 */
@Composable
private fun SectionTitle(text: String) {
    val theme = LocalPrimerTheme.current
    Text(text, style = theme.typographyTokens.bodyMedium.toTextStyle())
}

@Preview(showBackground = true, name = "PrimerInput - Empty State")
@Composable
private fun PrimerInputEmptyPreview() {
    PreviewContainer {
        SectionTitle("Empty Input")
        PrimerInput(
            value = "",
            onValueChange = {},
            label = "Card Number",
            placeholder = "1234 5678 9012 3456",
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - No Label")
@Composable
private fun PrimerInputNoLabelPreview() {
    PreviewContainer {
        SectionTitle("Input Without Label")
        PrimerInput(
            value = "",
            onValueChange = {},
            label = "",
            placeholder = "Enter your email",
        )

        SectionTitle("With Value, No Label")
        PrimerInput(
            value = "user@example.com",
            onValueChange = {},
            label = "",
            placeholder = "Enter your email",
        )

        SectionTitle("With Error, No Label")
        PrimerInput(
            value = "invalid-email",
            onValueChange = {},
            label = "",
            placeholder = "Enter your email",
            error = "Please enter a valid email address",
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - With Value")
@Composable
private fun PrimerInputWithValuePreview() {
    PreviewContainer {
        SectionTitle("Input With Value")
        PrimerInput(
            value = "John Appleseed",
            onValueChange = {},
            label = "Cardholder Name",
            placeholder = "Enter name",
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - With Error")
@Composable
private fun PrimerInputWithErrorPreview() {
    PreviewContainer {
        SectionTitle("Input With Error")
        PrimerInput(
            value = "123",
            onValueChange = {},
            label = "CVV",
            placeholder = "123",
            error = "CVV must be 3 digits",
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - States")
@Composable
private fun PrimerInputStatesPreview() {
    PreviewContainer {
        SectionTitle("Disabled State")
        PrimerInput(
            value = "Disabled Value",
            onValueChange = {},
            label = "Disabled Input",
            enabled = false,
        )

        SectionTitle("Read-Only State")
        PrimerInput(
            value = "Read-Only Value",
            onValueChange = {},
            label = "Read-Only Input",
            readOnly = true,
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - Keyboard Types")
@Composable
private fun PrimerInputKeyboardTypesPreview() {
    PreviewContainer {
        SectionTitle("Number Keyboard")
        PrimerInput(
            value = "4242424242424242",
            onValueChange = {},
            label = "Card Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            forceLtrForNumbers = true,
        )

        SectionTitle("Email Keyboard")
        PrimerInput(
            value = "user@example.com",
            onValueChange = {},
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        SectionTitle("Phone Keyboard")
        PrimerInput(
            value = "+1 (555) 123-4567",
            onValueChange = {},
            label = "Phone Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            forceLtrForNumbers = true,
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - With Trailing Icon")
@Composable
private fun PrimerInputWithTrailingIconPreview() {
    PreviewContainer {
        SectionTitle("With Icon")
        PrimerInput(
            value = "MySecurePassword123",
            onValueChange = {},
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = io.primer.android.components.R.drawable.ic_lock),
                        contentDescription = "Lock icon",
                    )
                }
            },
        )

        SectionTitle("With Checkmark")
        PrimerInput(
            value = "Valid Input",
            onValueChange = {},
            label = "Validated Field",
            trailingIcon = {
                val theme = LocalPrimerTheme.current
                Icon(
                    painter = painterResource(id = io.primer.android.components.R.drawable.ic_primer_check),
                    contentDescription = "Valid",
                    tint = theme.colorTokens().primerColorBrand,
                )
            },
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - Visual Transformation")
@Composable
private fun PrimerInputVisualTransformationPreview() {
    PreviewContainer {
        SectionTitle("Password Masked")
        PrimerInput(
            value = "SecretPassword",
            onValueChange = {},
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
        )

        SectionTitle("CVV Masked")
        PrimerInput(
            value = "123",
            onValueChange = {},
            label = "CVV",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - RTL Support", locale = "ar")
@Composable
private fun PrimerInputRtlPreview() {
    PreviewContainer(layoutDirection = LayoutDirection.Rtl) {
        SectionTitle("RTL Text Input")
        PrimerInput(
            value = "نص تجريبي",
            onValueChange = {},
            label = "اسم حامل البطاقة",
            placeholder = "أدخل الاسم",
        )

        SectionTitle("RTL Number (LTR)")
        PrimerInput(
            value = "4242424242424242",
            onValueChange = {},
            label = "رقم البطاقة",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            forceLtrForNumbers = true,
        )
    }
}

@Preview(showBackground = true, name = "PrimerInput - Edge Cases")
@Composable
private fun PrimerInputEdgeCasesPreview() {
    PreviewContainer {
        SectionTitle("Long Label & Error")
        PrimerInput(
            value = "",
            onValueChange = {},
            label = "Very Long Label That Might Wrap To Multiple Lines",
            placeholder = "Placeholder text",
            error = "This is a very long error message that explains exactly " +
                "what went wrong with the input validation",
        )

        SectionTitle("Special Characters")
        PrimerInput(
            value = "Test@#$%^&*()",
            onValueChange = {},
            label = "Special Input",
        )

        SectionTitle("Empty Error")
        PrimerInput(
            value = "Value",
            onValueChange = {},
            label = "Input",
            error = "",
        )
    }
}
