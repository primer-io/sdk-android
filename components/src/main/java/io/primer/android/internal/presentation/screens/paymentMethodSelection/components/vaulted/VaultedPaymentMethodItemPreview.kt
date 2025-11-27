@file:Suppress("UnusedPrivateMember")

package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Selected")
@Composable
private fun VaultedPaymentMethodItemSelectedPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            val theme = LocalPrimerTheme.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(theme.spacingTokens.large),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.large),
            ) {
                Text("Selected State:", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(),
                    isSelected = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Unselected")
@Composable
private fun VaultedPaymentMethodItemUnselectedPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            val theme = LocalPrimerTheme.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(theme.spacingTokens.large),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.large),
            ) {
                Text("Unselected State:", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(),
                    isSelected = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Variants")
@Composable
private fun VaultedPaymentMethodItemVariantsPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            val theme = LocalPrimerTheme.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(theme.spacingTokens.large),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
            ) {
                Text("Mastercard (Selected)", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "MASTERCARD",
                        cardholderName = "John Appleseed",
                        last4 = 1234,
                    ),
                    isSelected = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )

                Text("Visa", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "VISA",
                        cardholderName = "Jane Smith",
                        last4 = 5678,
                    ),
                    isSelected = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )

                Text("Amex", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "AMEX",
                        cardholderName = "Bob Johnson",
                        last4 = 9012,
                    ),
                    isSelected = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )

                Text("Discover", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "DISCOVER",
                        cardholderName = "Alice Williams",
                        last4 = 3456,
                    ),
                    isSelected = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Edge Cases")
@Composable
private fun VaultedPaymentMethodItemEdgeCasesPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            val theme = LocalPrimerTheme.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(theme.spacingTokens.large),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
            ) {
                Text("Long Cardholder Name", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "VISA",
                        cardholderName = "Christopher Alexander Montgomery III",
                        last4 = 1111,
                    ),
                    isSelected = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )

                Text("Expiring Soon (This Month)", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "MASTERCARD",
                        cardholderName = "Expiring User",
                        last4 = 2222,
                        expirationMonth = 12,
                        expirationYear = 2024,
                    ),
                    isSelected = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )

                Text("JCB Card", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "JCB",
                        cardholderName = "Test User",
                        last4 = 3333,
                    ),
                    isSelected = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )

                Text("Non-Clickable (Locked)", style = theme.typographyTokens.bodyMedium.toTextStyle())
                VaultedCardPaymentMethodItem(
                    paymentMethod = previewVaultedPaymentMethod(
                        network = "VISA",
                        cardholderName = "Locked Card",
                        last4 = 4444,
                    ),
                    isSelected = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                    enableClickable = false,
                )
            }
        }
    }
}

private fun previewVaultedPaymentMethod(
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
