package io.primer.android.configuration.domain.model

sealed interface Surcharge {
    data class PaymentMethodSurcharge(val amount: Int) : Surcharge
    data class CardNetworksSurcharge(val surcharges: Map<String, Int>) : Surcharge
}
