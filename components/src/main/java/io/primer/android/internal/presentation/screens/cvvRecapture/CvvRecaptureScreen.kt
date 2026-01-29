package io.primer.android.internal.presentation.screens.cvvRecapture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.components.AppBarSpec
import io.primer.android.internal.presentation.checkout.components.WithAppBar
import io.primer.android.internal.presentation.components.DefaultSubmitButton
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.DefaultVaultSectionHeader
import io.primer.android.internal.presentation.screens.vault.components.vaultItem.DefaultVaultItem
import io.primer.android.internal.presentation.screens.vault.components.vaultItem.VaultCvvInput
import io.primer.android.internal.presentation.utils.VaultItemProperties
import io.primer.cardShared.CardNumberFormatter

/**
 * Lean CVV recapture screen for vaulted card payments.
 *
 * This is a top-level screen that shows:
 * - Selected vaulted card
 * - CVV input
 * - Pay button
 *
 * Uses PrimerCheckoutViewModel directly for CVV state management.
 * No vault management UI (no "Show all", no "Other ways to pay").
 */
@Composable
internal fun CvvRecaptureScreen(checkout: CheckoutViewModel) {
    WithAppBar(
        AppBarSpec.Standard(
            title = stringResource(R.string.primer_vault_cvv_title),
        ),
    ) {
        CvvRecaptureContent(checkout)
    }
}

@Composable
private fun CvvRecaptureContent(checkout: CheckoutViewModel) {
    val theme = LocalPrimerTheme.current

    val selectedMethod by checkout.selectedVaultedMethodForCvv.collectAsStateWithLifecycle()
    val cvvState by checkout.cvvState.collectAsStateWithLifecycle()
    val isLoading by checkout.isVaultPaymentLoading.collectAsStateWithLifecycle()

    val method = selectedMethod
    if (method == null) {
        // No method selected - show error state
        Text(
            stringResource(R.string.primer_vault_cvv_error_generic),
            modifier = Modifier.padding(theme.spacingTokens.large),
            style = theme.typographyTokens.bodyMedium.toTextStyle(),
            color = theme.colorTokens().primerColorTextNegative,
        )
        return
    }

    val cvvLength = remember(method) {
        val first6 = method.paymentInstrumentData.first6Digits?.toString().orEmpty()
        CardNumberFormatter.fromString(first6).getCvvLength()
    }

    val shape = RoundedCornerShape(theme.radiusTokens.medium)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(theme.spacingTokens.large),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
    ) {
        DefaultVaultSectionHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(theme.colorTokens().primerColorGray100)
                .padding(theme.spacingTokens.small),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        ) {
            DefaultVaultItem(
                VaultItemProperties(
                    paymentMethod = method,
                    isSelected = true,
                    cvvLayout = {
                        VaultCvvInput(
                            cvvValue = cvvState.value,
                            onCvvChange = { checkout.updateCvv(it) },
                            enabled = !isLoading,
                            cvvLength = cvvLength,
                        )
                    },
                ),
            )

            DefaultSubmitButton(
                isLoading = isLoading,
                enabled = !isLoading && cvvState.isValid,
                onClick = { checkout.submitVaultedPaymentWithCvv() },
            )
        }
    }
}
