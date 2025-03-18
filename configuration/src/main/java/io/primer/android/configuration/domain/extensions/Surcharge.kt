package io.primer.android.configuration.domain.extensions

import io.primer.android.configuration.domain.model.Surcharge

fun Surcharge.disabled(): Boolean = when (this) {
    is Surcharge.CardNetworksSurcharge -> surcharges.all { surcharge -> surcharge.value == 0 }
    is Surcharge.PaymentMethodSurcharge -> amount == 0
}
