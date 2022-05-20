package io.primer.android.data.settings.internal

import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.PrimerPaymentMethodIntent
import kotlinx.serialization.Serializable

@Serializable
internal data class PrimerIntent(
    val paymentMethodIntent: PrimerPaymentMethodIntent = PrimerPaymentMethodIntent.CHECKOUT,
    val paymentMethod: PrimerPaymentMethodType? = null,
)
