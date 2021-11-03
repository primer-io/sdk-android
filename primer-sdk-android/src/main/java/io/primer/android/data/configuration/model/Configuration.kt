package io.primer.android.data.configuration.model

import io.primer.android.model.dto.ConfigurationKeys
import io.primer.android.model.dto.Customer
import io.primer.android.model.dto.Order
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodRemoteConfig>,
    val keys: ConfigurationKeys? = null,
    val clientSession: ClientSession? = null,
    @SerialName("env") val environment: Environment,
)

@Serializable
data class ClientSession(
    val customer: Customer? = null,
    val order: Order? = null,
)

enum class Environment(val environment: String) {
    LOCAL_DOCKER("local_dev"),
    DEV("dev"),
    SANDBOX("sandbox"),
    STAGING("staging"),
    PRODUCTION("production"),
}
