package io.primer.android.data.configuration.model

import androidx.annotation.Keep
import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.model.dto.PaymentMethodType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Configuration(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodRemoteConfig>,
    val checkoutModules: List<CheckoutModule> = listOf(),
    val keys: ConfigurationKeys? = null,
    val clientSession: ClientSessionResponse? = null,
    @SerialName("env") val environment: Environment,
    val primerAccountId: String? = null,
)

@Keep
@Serializable
internal data class PaymentMethodRemoteConfig(
    val id: String? = null, // payment card has null only
    val type: PaymentMethodType = PaymentMethodType.UNKNOWN,
    val options: PaymentMethodRemoteConfigOptions? = null,
)

@Keep
@Serializable
internal data class PaymentMethodRemoteConfigOptions(
    val merchantId: String? = null,
    val merchantAccountId: String? = null,
    val threeDSecureEnabled: Boolean? = null,
)

@Serializable
internal data class ClientSessionResponse(
    val clientSessionId: String? = null,
    val customerId: String? = null,
    val orderId: String? = null,
    val amount: Int? = null,
    val currencyCode: String? = null,
    val customer: CustomerDataResponse? = null,
    val order: OrderDataResponse? = null,
    val paymentMethod: PaymentMethod? = null,
) {

    fun getCalculatedAmount(paymentMethodType: String): Int {
        val baseAmount = order?.amount ?: 0
        val option = paymentMethod?.options?.find { it.type == paymentMethodType }
        val surcharge = option?.surcharge ?: 0
        return baseAmount + surcharge
    }

    @Serializable
    data class PaymentMethod(
        val vaultOnSuccess: Boolean? = null,
        val options: List<PaymentMethodOption> = listOf(),
    ) {

        val surcharges: Map<String, Int>
            get() {
                val map = mutableMapOf<String, Int>()
                options.forEach { option ->
                    if (option.type == "PAYMENT_CARD") {
                        option.networks?.forEach { network ->
                            map[network.type] = network.surcharge
                        }
                    } else {
                        map[option.type] = option.surcharge ?: 0
                    }
                }
                return map
            }
    }

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
        val surcharge: Int,
    )

    fun toClientSessionData() = ClientSessionData(
        PrimerClientSession(
            customer?.customerId ?: customerId,
            order?.id ?: orderId,
            order?.currency ?: currencyCode,
            order?.totalOrderAmount ?: amount,
            order?.lineItems?.map { it.toLineItem() },
            order?.toOrder(),
            customer?.toCustomer(),
        )
    )
}

internal enum class Environment(val environment: String) {
    LOCAL_DOCKER("local_dev"),
    DEV("dev"),
    SANDBOX("sandbox"),
    STAGING("staging"),
    PRODUCTION("production"),
}

@Serializable
internal data class CheckoutModule(
    val type: CheckoutModuleType = CheckoutModuleType.UNKNOWN,
    val requestUrl: String? = null,
    val options: Map<String, Boolean>? = null,
)

@Serializable
internal enum class CheckoutModuleType {

    BILLING_ADDRESS,
    CARD_INFORMATION,
    UNKNOWN
}
