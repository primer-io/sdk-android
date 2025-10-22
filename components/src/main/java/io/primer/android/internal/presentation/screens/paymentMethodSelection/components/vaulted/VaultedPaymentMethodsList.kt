package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

/**
 * Default container composable for the vaulted payment methods section.
 * Provides consistent styling and layout for the vault section.
 */
@Composable
internal fun VaultedPaymentMethodsList(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LocalPrimerTheme.current.spacingTokens.medium),
        verticalArrangement = Arrangement.spacedBy(LocalPrimerTheme.current.spacingTokens.small),
    ) {
        // Section header
        Text(
            text = stringResource(R.string.primer_components_saved_payment_methods),
            style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = LocalPrimerTheme.current.spacingTokens.large),
        )

        // Content (payment methods list)
        content()
    }
}
