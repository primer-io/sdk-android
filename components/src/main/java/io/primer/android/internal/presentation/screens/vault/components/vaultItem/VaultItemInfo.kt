package io.primer.android.internal.presentation.screens.vault.components.vaultItem

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.presentation.screens.card.components.CardNetworkIcon
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal data class VaultItemInfo(
    val title: String,
    val subtitle: String,
    val trailingText: String? = null,
    val expiryText: String? = null,
    val icon: @Composable () -> Unit = {},
)

@Composable
internal fun rememberInfo(paymentMethod: PrimerVaultedPaymentMethod): VaultItemInfo {
    val theme = LocalPrimerTheme.current
    val iconModifier = Modifier.size(theme.sizeTokens.large)
    val data = paymentMethod.paymentInstrumentData

    return when (paymentMethod.paymentMethodType) {
        PaymentMethodType.PAYPAL.name -> VaultItemInfo(
            title = data.externalPayerInfo?.let {
                "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim().ifEmpty { it.email }
            } ?: stringResource(R.string.primer_vault_default_paypal),
            subtitle = data.externalPayerInfo?.email
                ?: stringResource(R.string.primer_vault_default_paypal),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_primer_paypal_icon),
                    contentDescription = stringResource(R.string.accessibility_vaulted_paypal),
                    modifier = iconModifier,
                    tint = Color.Unspecified,
                )
            },
        )

        PaymentMethodType.STRIPE_ACH.name -> VaultItemInfo(
            title = data.bankName
                ?: stringResource(R.string.primer_vault_default_bank),
            subtitle = data.accountNumberLast4Digits?.let {
                stringResource(R.string.primer_vault_format_masked, it)
            } ?: stringResource(R.string.primer_vault_default_bank),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_bank_16),
                    contentDescription = stringResource(R.string.primer_vault_default_bank),
                    modifier = iconModifier,
                    tint = Color.Unspecified,
                )
            },
        )

        else -> VaultItemInfo(
            title = data.cardholderName
                ?: stringResource(R.string.primer_vault_default_cardholder),
            subtitle = data.network.orEmpty(),
            trailingText = stringResource(
                R.string.primer_vault_format_masked,
                data.last4Digits?.toString().orEmpty(),
            ),
            expiryText = formatExpiry(data.expirationMonth, data.expirationYear),
            icon = {
                val network = CardNetwork.Type.valueOrNull(data.network)
                CardNetworkIcon(network, iconModifier)
            },
        )
    }
}

@Composable
private fun formatExpiry(month: Int?, year: Int?): String? {
    if (month == null || year == null) return null
    return stringResource(
        R.string.primer_vault_format_expires,
        month.toString().padStart(2, '0'),
        year.toString().takeLast(2),
    )
}
