package io.primer.android.model.dto

import io.primer.android.PrimerTheme
import io.primer.android.PaymentMethodIntent
import kotlinx.serialization.Serializable

@Serializable
data class PrimerConfig(
    var theme: PrimerTheme = PrimerTheme.build(),
    var settings: PrimerSettings = PrimerSettings(),
) {

    internal var clientTokenBase64: String? = null

    internal var intent: PrimerIntent = PrimerIntent()

    internal val monetaryAmount: MonetaryAmount?
        get() {
            return MonetaryAmount.create(
                settings.order.currency,
                settings.order.amount,
            )
        }

    internal val paymentMethodIntent: PaymentMethodIntent
        get() {
            return intent.paymentMethodIntent
        }

    internal val isStandalonePaymentMethod: Boolean
        get() {
            return intent.paymentMethod != PrimerPaymentMethod.ANY
        }
}
