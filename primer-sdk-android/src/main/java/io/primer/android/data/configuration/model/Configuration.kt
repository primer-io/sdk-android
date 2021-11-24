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
    val customerId: String? = null,
    val orderId: String? = null,
    val amount: Int? = null,
    val currencyCode: String? = null,
    val customer: Customer? = null,
    val order: Order? = null,
    val paymentMethod: PaymentMethod? = null,
) {

    @Serializable
    data class PaymentMethod(
        val vaultOnSuccess: Boolean? = null,
        val options: List<PaymentMethodOption> = listOf(),
    )

    // todo: may be better to use sealed class/polymorphism
    @Serializable
    data class PaymentMethodOption(
        val type: String,
        val surcharge: Int? = null,
        val networks: List<NetworkOption>? = null,
    )

    @Serializable
    data class NetworkOption(
        val type: String,
        val surcharge: Int = 0
    )
}

enum class Environment(val environment: String) {
    LOCAL_DOCKER("local_dev"),
    DEV("dev"),
    SANDBOX("sandbox"),
    STAGING("staging"),
    PRODUCTION("production"),
}
