package io.primer.android.data.settings.internal

import io.primer.android.PrimerSessionIntent

internal data class PrimerIntent(
    val paymentMethodIntent: PrimerSessionIntent = PrimerSessionIntent.CHECKOUT,
    val paymentMethod: String? = null,
)
