package io.primer.android.configuration.data.extensions

import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ClientSessionDataResponse.PaymentMethodDataResponse.Companion.PAYMENT_CARD_TYPE
import io.primer.android.configuration.domain.model.Surcharge

fun ClientSessionDataResponse.PaymentMethodDataResponse.surcharges(): Map<String, Surcharge> {
    val map = mutableMapOf<String, Surcharge>()
    options.forEach { option ->
        if (option.type == PAYMENT_CARD_TYPE) {
            map[option.type] = Surcharge.CardNetworksSurcharge(
                option.networks?.associate { network ->
                    network.type to network.surcharge
                }.orEmpty(),
            )
        } else {
            map[option.type] = Surcharge.PaymentMethodSurcharge(option.surcharge ?: 0)
        }
    }
    return map
}
