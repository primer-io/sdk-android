package io.primer.android.model.dto

import io.primer.android.payment.PaymentMethodRemoteConfig
import kotlinx.serialization.Serializable

@Serializable
internal data class ClientSession(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodRemoteConfig>
)