package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Composable
internal fun resolveCardHolderNameFromPaymentMethod(paymentMethod: PrimerVaultedPaymentMethod): String {
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
internal fun getPaymentMethodDetails(paymentMethod: PrimerVaultedPaymentMethod): String {
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

@Composable
internal fun getPaymentMethodExpiry(paymentMethod: PrimerVaultedPaymentMethod): String {
    if (paymentMethod.paymentMethodType != PaymentMethodType.PAYMENT_CARD.name) {
        return ""
    }

    val data = paymentMethod.paymentInstrumentData
    val expirationMonth = data.expirationMonth
    val expirationYear = data.expirationYear

    return if (expirationMonth != null && expirationYear != null) {
        val month = expirationMonth.toString().padStart(2, '0')
        val year = expirationYear.toString().takeLast(2)
        stringResource(
            id = R.string.primer_components_vaulted_expires_date,
            month,
            year,
        )
    } else {
        ""
    }
}
