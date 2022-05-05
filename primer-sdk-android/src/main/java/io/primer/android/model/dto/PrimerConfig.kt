package io.primer.android.model.dto

import io.primer.android.PrimerTheme
import io.primer.android.PaymentMethodIntent
import io.primer.android.analytics.data.models.Place
import kotlinx.serialization.Serializable

@Serializable
data class PrimerConfig(
    var theme: PrimerTheme = PrimerTheme.build(),
    var settings: PrimerSettings = PrimerSettings(),
) {

    internal var clientTokenBase64: String? = null

    internal var intent: PrimerIntent = PrimerIntent()

    internal var fromHUC: Boolean = false

    internal val monetaryAmount: MonetaryAmount?
        get() {
            val currency = settings.order.currency
            val amount = settings.currentAmount
            return MonetaryAmount.create(currency, amount)
        }

    internal val paymentMethodIntent: PaymentMethodIntent
        get() = intent.paymentMethodIntent

    internal val isStandalonePaymentMethod: Boolean
        get() = intent.paymentMethod != PrimerPaymentMethod.ANY

    internal fun getMonetaryAmountWithSurcharge(): MonetaryAmount? {
        val amount = settings.currentAmount
        val currency = settings.order.currency
        return MonetaryAmount.create(currency, amount)
    }

    internal fun toPlace() =
        if (intent.paymentMethodIntent == PaymentMethodIntent.CHECKOUT)
            Place.UNIVERSAL_CHECKOUT else Place.VAULT_MANAGER
}
