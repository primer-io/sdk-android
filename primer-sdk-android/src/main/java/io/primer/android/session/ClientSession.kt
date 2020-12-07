package io.primer.android.session

import io.primer.android.model.json
import io.primer.android.payment.PaymentMethodRemoteConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject

@Serializable
internal data class ClientSession(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodRemoteConfig>
)