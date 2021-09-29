package io.primer.android.threeds.domain.models

import io.primer.android.PaymentMethodIntent
import io.primer.android.model.dto.PrimerConfig

internal data class ThreeDsConfigParams(
    val paymentMethodIntent: PaymentMethodIntent,
    val amount: Int,
    val currency: String,
    val orderId: String,
    val userDetailsAvailable: Boolean,
    val customerFirstName: String,
    val customerLastName: String,
    val customerEmail: String,
    val addressLine1: String,
    val city: String,
    val postalCode: String,
    val countryCode: String,
) {

    constructor(
        config: PrimerConfig,
    ) : this(
        config.paymentMethodIntent,
        config.settings.order.amount ?: 0,
        config.settings.order.currency.orEmpty(),
        config.settings.order.id.orEmpty(),
        config.settings.customer.detailsAvailable, // can't this be a getter param instead?
        config.settings.customer.firstName.orEmpty(),
        config.settings.customer.lastName.orEmpty(),
        config.settings.customer.email.orEmpty(),
        config.settings.customer.billingAddress?.line1.orEmpty(),
        config.settings.customer.billingAddress?.city.orEmpty(),
        config.settings.customer.billingAddress?.postalCode.orEmpty(),
        config.settings.customer.billingAddress?.country.orEmpty(),
    )
}
