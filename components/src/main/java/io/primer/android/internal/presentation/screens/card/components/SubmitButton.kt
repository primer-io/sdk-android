package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String,
) {
    val currentState by state.collectAsStateWithLifecycle()

    PrimerButton(
        onClick = { onSubmit() },
        modifier = modifier.fillMaxWidth(),
        backgroundColor = LocalPrimerTheme.current.colorTokens().primerColorBrand,
        enabled = !currentState.isLoading && currentState.isFormValid,
    ) {
        Text(
            text = if (currentState.isLoading) stringResource(R.string.primer_components_checkout_loading) else text,
            style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
        )
    }
}

/**
 * Preview of the Submit Button in different states.
 * Shows enabled, disabled, and loading states.
 */
@Preview(showBackground = true, name = "Submit Button States")
@Composable
private fun SubmitButtonPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        androidx.compose.material3.MaterialTheme {
            val theme = LocalPrimerTheme.current
            Column(
                modifier = Modifier.padding(theme.spacingTokens.large),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
            ) {
                PrimerButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    backgroundColor = theme.colorTokens().primerColorBrand,
                ) {
                    Text(
                        text = stringResource(R.string.primer_components_pay),
                        style = theme.typographyTokens.titleLarge.toTextStyle(),
                        color = theme.colorTokens().primerColorBackground,
                    )
                }

                PrimerButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    backgroundColor = theme.colorTokens().primerColorBrand,
                ) {
                    Text(
                        text = stringResource(R.string.primer_components_pay),
                        style = theme.typographyTokens.titleLarge.toTextStyle(),
                        color = theme.colorTokens().primerColorBackground,
                    )
                }

                PrimerButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    backgroundColor = theme.colorTokens().primerColorBrand,
                ) {
                    Text(
                        text = stringResource(R.string.primer_components_processing),
                        style = theme.typographyTokens.titleLarge.toTextStyle(),
                        color = theme.colorTokens().primerColorBackground,
                    )
                }
            }
        }
    }
}
