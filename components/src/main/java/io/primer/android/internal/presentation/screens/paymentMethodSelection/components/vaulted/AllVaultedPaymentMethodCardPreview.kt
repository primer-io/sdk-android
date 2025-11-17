package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerVaultedScope

@Preview(showBackground = true, name = "All Vaulted Card - View Mode")
@Composable
private fun AllVaultedPaymentMethodCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AllVaultedPaymentMethodCard(
                paymentMethod = createPreviewCardPaymentMethod(),
                state = VaultedMethodCardState(
                    isSelected = true,
                    editMode = PrimerVaultedScope.State.EditMode.View,
                    isDeleting = false,
                ),
                onClick = {},
                onDeleteClick = {},
            )
            AllVaultedPaymentMethodCard(
                paymentMethod = createPreviewPayPalPaymentMethod(),
                state = VaultedMethodCardState(
                    isSelected = false,
                    editMode = PrimerVaultedScope.State.EditMode.Edit,
                    isDeleting = false,
                ),
                onClick = {},
                onDeleteClick = {},
            )
        }
    }
}

private fun createPreviewCardPaymentMethod(): PrimerVaultedPaymentMethod {
    return createPreviewVaultedPaymentMethod(
        id = "preview-card-1",
        type = PaymentMethodType.PAYMENT_CARD.name,
    )
}

private fun createPreviewPayPalPaymentMethod(): PrimerVaultedPaymentMethod {
    return createPreviewVaultedPaymentMethod(
        id = "preview-paypal-1",
        type = PaymentMethodType.PAYPAL.name,
    )
}

private fun createPreviewVaultedPaymentMethod(
    id: String,
    type: String,
): PrimerVaultedPaymentMethod {
    return PrimerVaultedPaymentMethod(
        id = id,
        analyticsId = "preview-$id",
        paymentMethodType = type,
        paymentInstrumentType = type,
        paymentInstrumentData = PaymentInstrumentData(
            network = "MASTERCARD",
            cardholderName = "John Appleseed",
            first6Digits = 543210,
            last4Digits = 1234,
            accountNumberLast4Digits = null,
            expirationMonth = 12,
            expirationYear = 2026,
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
