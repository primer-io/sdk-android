package io.primer.android.internal.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultSubmitButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.primer_common_button_pay),
) {
    val theme = LocalPrimerTheme.current
    val accessibilityStateDescription = when {
        isLoading -> stringResource(R.string.accessibility_card_form_submit_loading)
        !enabled -> stringResource(R.string.accessibility_card_form_submit_disabled)
        else -> null
    }

    PrimerButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("primer_submit_button"),
        backgroundColor = theme.colorTokens().primerColorBrand,
        enabled = enabled,
        accessibilityLabel = stringResource(R.string.accessibility_card_form_submit_label),
        accessibilityStateDescription = accessibilityStateDescription,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(theme.sizeTokens.large),
                color = theme.colorTokens().primerColorBackground,
                strokeWidth = theme.borderWidthTokens.medium,
            )
        } else {
            Text(
                text = text,
                style = theme.typographyTokens.titleLarge.toTextStyle(),
                color = theme.colorTokens().primerColorBackground,
            )
        }
    }
}

@Preview(name = "Enabled", showBackground = true)
@Composable
private fun DefaultSubmitButtonEnabledPreview() = PreviewContainer {
    DefaultSubmitButton(isLoading = false, enabled = true, onClick = {})
}

@Preview(name = "Disabled", showBackground = true)
@Composable
private fun DefaultSubmitButtonDisabledPreview() = PreviewContainer {
    DefaultSubmitButton(isLoading = false, enabled = false, onClick = {})
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun DefaultSubmitButtonLoadingPreview() = PreviewContainer {
    DefaultSubmitButton(isLoading = true, enabled = true, onClick = {})
}
