package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClientSession(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodRemoteConfig>,
)
