@file:Suppress("UnusedPrivateMember")

package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Selected")
@Composable
private fun VaultedPaymentMethodItemSelectedPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Selected State:", style = MaterialTheme.typography.labelMedium)
            VaultedCardPaymentMethodItem(
                paymentMethod = previewVaultedPaymentMethod(),
                isSelected = true,
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Unselected")
@Composable
private fun VaultedPaymentMethodItemUnselectedPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Unselected State:", style = MaterialTheme.typography.labelMedium)
            VaultedCardPaymentMethodItem(
                paymentMethod = previewVaultedPaymentMethod(),
                isSelected = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Vaulted Payment Method Item - Variants")
@Composable
private fun VaultedPaymentMethodItemVariantsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Mastercard", style = MaterialTheme.typography.labelMedium)
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

            Text("Visa", style = MaterialTheme.typography.labelMedium)
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

            Text("Amex", style = MaterialTheme.typography.labelMedium)
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
