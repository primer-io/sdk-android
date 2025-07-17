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
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens

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
        focusedBorderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedFocus,
        unfocusedBorderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
    ),
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = LocalPrimerTypographyTokens.current.bodySmall.toTextStyle(),
            color = LocalPrimerColorTokens.current.primerColorTextPrimary
        )

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.xsmall))

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
            shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.small),
            colors = colors,
        )

        error?.let {
            Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.xsmall))
            Text(
                text = it,
                style = LocalPrimerTypographyTokens.current.bodySmall.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorTextNegative,
            )
            Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.xsmall))
        }
    }

}
