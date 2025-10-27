package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Default composable for displaying an individual vaulted payment method.
 * Shows payment method icon, details, and selection state.
 */
@Suppress("LongMethod")
@Composable
internal fun VaultedPaymentMethodItem(
    paymentMethod: PrimerVaultedPaymentMethod,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val theme = LocalPrimerTheme.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Payment method details - 2x2 grid layout
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
        ) {
            // First row: Cardholder name (left) | Card number (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = resolveCardHolderNameFromPaymentMethod(paymentMethod),
                    style = theme.typographyTokens.bodyMedium.toTextStyle(),
                    color = theme.colorTokens().primerColorTextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f, fill = false),
                )

                Text(
                    text = getPaymentMethodDetails(paymentMethod),
                    style = theme.typographyTokens.bodyMedium.toTextStyle(),
                    color = theme.colorTokens().primerColorTextPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Second row: Card brand with logo (left) | Expiry date (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left side: Card brand with logo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    if (paymentMethod.paymentMethodType == PaymentMethodType.PAYMENT_CARD.name) {
                        PaymentMethodIcon(
                            network = paymentMethod.paymentInstrumentData.network,
                            modifier = Modifier.size(theme.sizeTokens.large),
                        )
                        Text(
                            text = paymentMethod.paymentInstrumentData.network ?: "",
                            style = theme.typographyTokens.bodySmall.toTextStyle(),
                            color = theme.colorTokens().primerColorTextSecondary,
                        )
                    }
                }

                // Right side: Expiry date
                Text(
                    text = getPaymentMethodExpiry(paymentMethod),
                    style = theme.typographyTokens.bodySmall.toTextStyle(),
                    color = theme.colorTokens().primerColorTextSecondary,
                )
            }
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_blue),
                contentDescription = stringResource(
                    id = R.string.primer_components_vaulted_selected_content_description,
                ),
                modifier = Modifier.size(theme.sizeTokens.small),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun PaymentMethodIcon(
    network: String?,
    modifier: Modifier = Modifier,
) {
    val cardNetworkType = CardNetwork.Type.valueOrNull(network)
    val contentDescription = network?.let {
        stringResource(id = R.string.primer_components_content_description_card_network, it)
    } ?: stringResource(id = R.string.primer_components_vaulted_generic_card_content_description)

    if (cardNetworkType != null) {
        val iconResId = cardNetworkType.getCardImageAsset(
            io.primer.android.displayMetadata.domain.model.ImageColor.COLORED,
        )
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = Color.Unspecified,
        )
    } else {
        // Fallback to generic card icon
        Icon(
            painter = painterResource(id = R.drawable.ic_generic_card),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun resolveCardHolderNameFromPaymentMethod(paymentMethod: PrimerVaultedPaymentMethod): String {
    return when (paymentMethod.paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> {
            paymentMethod.paymentInstrumentData.cardholderName
                ?: stringResource(id = R.string.primer_components_vaulted_default_cardholder_name)
        }
        PaymentMethodType.PAYPAL.name -> {
            paymentMethod.paymentInstrumentData.externalPayerInfo?.let { info ->
                "${info.firstName ?: ""} ${info.lastName ?: ""}".trim().ifEmpty { info.email }
            } ?: stringResource(id = R.string.primer_components_vaulted_default_paypal_account)
        }
        PaymentMethodType.STRIPE_ACH.name -> {
            paymentMethod.paymentInstrumentData.bankName
                ?: stringResource(id = R.string.primer_components_vaulted_default_bank_account)
        }
        else -> paymentMethod.paymentInstrumentType
    }
}

@Composable
private fun getPaymentMethodDetails(paymentMethod: PrimerVaultedPaymentMethod): String {
    return when (paymentMethod.paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> {
            val last4 = paymentMethod.paymentInstrumentData.last4Digits
            stringResource(
                id = R.string.primer_components_vaulted_masked_card_number,
                last4?.toString().orEmpty(),
            )
        }
        PaymentMethodType.PAYPAL.name -> {
            paymentMethod.paymentInstrumentData.externalPayerInfo?.email
                ?: stringResource(id = R.string.primer_components_vaulted_default_paypal_account)
        }
        PaymentMethodType.STRIPE_ACH.name -> {
            val last4 = paymentMethod.paymentInstrumentData.accountNumberLast4Digits
            if (last4 != null) {
                stringResource(
                    id = R.string.primer_components_vaulted_masked_bank_account_number,
                    last4,
                )
            } else {
                stringResource(id = R.string.primer_components_vaulted_default_bank_account)
            }
        }
        else -> paymentMethod.paymentInstrumentType
    }
}

/**
 * Returns the expiry date for the payment method (e.g., "Expires 12/26").
 */
@Composable
private fun getPaymentMethodExpiry(paymentMethod: PrimerVaultedPaymentMethod): String {
    return when (paymentMethod.paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> {
            paymentMethod.paymentInstrumentData.let { data ->
                if (data.expirationMonth != null && data.expirationYear != null) {
                    val month = data.expirationMonth.toString().padStart(2, '0')
                    val year = data.expirationYear.toString().takeLast(2)
                    stringResource(
                        id = R.string.primer_components_vaulted_expires_date,
                        month,
                        year,
                    )
                } else {
                    ""
                }
            }
        }
        else -> ""
    }
}

/**
 * Preview of the vaulted payment method item in different states.
 * Shows selected and unselected states with a mock Mastercard.
 */
@Preview(showBackground = true, name = "Vaulted Payment Method Item - Selected")
@Composable
internal fun DefaultVaultedPaymentMethodItemSelectedPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Selected State:", style = MaterialTheme.typography.labelMedium)
            VaultedPaymentMethodItem(
                paymentMethod = createPreviewVaultedPaymentMethod(),
                isSelected = true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Click handler (no-op in preview)
            }
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Unselected")
@Composable
internal fun DefaultVaultedPaymentMethodItemUnselectedPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Unselected State:", style = MaterialTheme.typography.labelMedium)
            VaultedPaymentMethodItem(
                paymentMethod = createPreviewVaultedPaymentMethod(),
                isSelected = false,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Click handler (no-op in preview)
            }
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Different Cards")
@Composable
internal fun DefaultVaultedPaymentMethodItemVariationsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Mastercard:", style = MaterialTheme.typography.labelMedium)
            VaultedPaymentMethodItem(
                paymentMethod = createPreviewVaultedPaymentMethod(
                    network = "MasterCard",
                    last4 = 1234,
                    cardholderName = "John Appleseed",
                ),
                isSelected = true,
                modifier = Modifier.fillMaxWidth(),
            ) { }

            Text("Visa:", style = MaterialTheme.typography.labelMedium)
            VaultedPaymentMethodItem(
                paymentMethod = createPreviewVaultedPaymentMethod(
                    network = "VISA",
                    last4 = 5678,
                    cardholderName = "Jane Smith",
                ),
                isSelected = false,
                modifier = Modifier.fillMaxWidth(),
            ) { }

            Text("American Express:", style = MaterialTheme.typography.labelMedium)
            VaultedPaymentMethodItem(
                paymentMethod = createPreviewVaultedPaymentMethod(
                    network = "AMEX",
                    last4 = 9012,
                    cardholderName = "Bob Johnson",
                ),
                isSelected = false,
                modifier = Modifier.fillMaxWidth(),
            ) { }
        }
    }
}

/**
 * Creates a mock vaulted payment method for preview purposes.
 */
private fun createPreviewVaultedPaymentMethod(
    network: String = "MASTERCARD",
    last4: Int = 1234,
    cardholderName: String = "John Appleseed",
    expirationMonth: Int = 12,
    expirationYear: Int = 2026,
): PrimerVaultedPaymentMethod {
    return PrimerVaultedPaymentMethod(
        id = "preview-card-${network.lowercase()}-$last4",
        analyticsId = "preview-analytics-123",
        paymentMethodType = PaymentMethodType.PAYMENT_CARD.name,
        paymentInstrumentType = PaymentMethodType.PAYMENT_CARD.name,
        paymentInstrumentData = io.primer.android.data.tokenization.models.PaymentInstrumentData(
            network = network,
            cardholderName = cardholderName,
            first6Digits = 543210,
            last4Digits = last4,
            accountNumberLast4Digits = null,
            expirationMonth = expirationMonth,
            expirationYear = expirationYear,
            externalPayerInfo = null,
            klarnaCustomerToken = null,
            sessionData = null,
            paymentMethodType = null,
            sessionInfo = null,
            binData = null,
            bankName = null,
        ),
    )
}
