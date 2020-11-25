package io.primer.android.session

import io.primer.android.payment.PaymentMethodRemoteConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject

@Serializable
data class ClientSession(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodRemoteConfig>
) {
    companion object {
        private val format = Json { ignoreUnknownKeys = true }
        fun fromJSON(data: JSONObject): ClientSession {
            return format.decodeFromString(data.toString())
        }
    }
}