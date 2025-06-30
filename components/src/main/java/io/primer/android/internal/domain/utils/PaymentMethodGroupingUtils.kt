package io.primer.android.internal.domain.utils

import io.primer.android.configuration.domain.model.Surcharge

internal fun Surcharge?.getValue(): Int {
    return when (this) {
        is Surcharge.CardNetworksSurcharge -> if (surcharges.any { it.value != 0 }) UNKNOWN_SURCHARGE else 0
        is Surcharge.PaymentMethodSurcharge -> amount
        null -> 0
    }
}

internal const val UNKNOWN_SURCHARGE = 100000
