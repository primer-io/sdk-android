package io.primer.android.data.settings.internal

import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.PrimerSessionIntent
import kotlinx.serialization.Serializable

@Serializable
internal data class PrimerIntent(
    val paymentMethodIntent: PrimerSessionIntent = PrimerSessionIntent.CHECKOUT,
    val paymentMethod: PrimerPaymentMethodType? = null,
)
