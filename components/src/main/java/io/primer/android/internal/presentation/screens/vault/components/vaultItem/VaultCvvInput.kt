package io.primer.android.internal.presentation.screens.vault.components.vaultItem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerInput
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.internal.presentation.utils.toWesternNumeralsOnly

@Composable
internal fun VaultCvvInput(
    cvvValue: String,
    onCvvChange: (String) -> Unit,
    enabled: Boolean,
    cvvLength: Int,
) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_lock),
            contentDescription = stringResource(R.string.accessibility_card_form_cvv_icon),
            tint = theme.colorTokens().primerColorTextSecondary,
            modifier = Modifier.size(theme.sizeTokens.small),
        )
        Text(
            stringResource(R.string.primer_vault_cvv_hint),
            style = theme.typographyTokens.bodySmall.toTextStyle(),
            color = theme.colorTokens().primerColorTextSecondary,
        )
        Spacer(Modifier.weight(1f))
        PrimerInput(
            value = cvvValue,
            onValueChange = { onCvvChange(it.toWesternNumeralsOnly().take(cvvLength)) },
            placeholder = stringResource(R.string.primer_card_form_label_cvv),
            enabled = enabled,
            modifier = Modifier
                .width(120.dp)
                .testTag("primer_vault_cvv_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PasswordVisualTransformation(),
            forceLtrForNumbers = false,
            accessibilityLabel = stringResource(R.string.accessibility_card_form_cvc_label),
            isRequired = true,
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun VaultCvvInputPreview() = PreviewContainer {
    VaultCvvInput(
        cvvValue = "",
        onCvvChange = {},
        enabled = true,
        cvvLength = 3,
    )
}
