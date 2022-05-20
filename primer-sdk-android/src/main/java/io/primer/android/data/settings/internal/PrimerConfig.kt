package io.primer.android.data.settings.internal

import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.analytics.data.models.Place
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.model.MonetaryAmount
import kotlinx.serialization.Serializable

@Serializable
internal data class PrimerConfig(
    var settings: PrimerSettings = PrimerSettings(),
) {

    internal var clientTokenBase64: String? = null

    internal var intent: PrimerIntent = PrimerIntent()

    internal val monetaryAmount: MonetaryAmount?
        get() {
            val currency = settings.order.currency
            val amount = settings.currentAmount
            return MonetaryAmount.create(currency, amount)
        }

    internal val paymentMethodIntent: PrimerPaymentMethodIntent
        get() = intent.paymentMethodIntent

    internal val isStandalonePaymentMethod: Boolean
        get() = intent.paymentMethod != PrimerPaymentMethod.ANY

    internal fun getMonetaryAmountWithSurcharge(): MonetaryAmount? {
        val amount = settings.currentAmount
        val currency = settings.order.currency
        return MonetaryAmount.create(currency, amount)
    }

    internal fun toPlace() =
        if (intent.paymentMethodIntent == PrimerPaymentMethodIntent.CHECKOUT)
            Place.UNIVERSAL_CHECKOUT else Place.VAULT_MANAGER
}
