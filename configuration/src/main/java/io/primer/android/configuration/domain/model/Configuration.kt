package io.primer.android.configuration.domain.model

import io.primer.android.configuration.data.model.CheckoutModuleDataResponse
import io.primer.android.configuration.data.model.CheckoutModuleType
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.data.model.PaymentMethodRemoteConfigOptions
import io.primer.android.configuration.data.model.ShippingMethod

data class Configuration(
    val environment: Environment,
    val paymentMethods: List<PaymentMethodConfig>,
    val clientSession: ClientSession,
    val checkoutModules: List<CheckoutModule>,
)

data class PaymentMethodConfig(
    val id: String?,
    val name: String?,
    val type: String,
    val options: PaymentMethodRemoteConfigOptions?,
)

data class ClientSession(
    val clientSessionDataResponse: ClientSessionDataResponse,
)

sealed class CheckoutModule {
    data class BillingAddress(val options: Map<String, Boolean>?) : CheckoutModule()

    data class CardInformation(val options: Map<String, Boolean>?) : CheckoutModule()

    data class Shipping(val shippingMethods: List<ShippingMethod>, val selectedMethod: String?) : CheckoutModule()

    data object Unknown : CheckoutModule()
}

fun CheckoutModuleDataResponse.toCheckoutModule() =
    when (type) {
        CheckoutModuleType.BILLING_ADDRESS -> CheckoutModule.BillingAddress(options)
        CheckoutModuleType.CARD_INFORMATION -> CheckoutModule.CardInformation(options)
        CheckoutModuleType.SHIPPING -> {
            CheckoutModule.Shipping(
                shippingMethods = shippingOptions?.shippingMethods ?: emptyList(),
                selectedMethod = shippingOptions?.selectedShippingMethod,
            )
        }

        CheckoutModuleType.UNKNOWN -> CheckoutModule.Unknown
    }

inline fun <reified T : CheckoutModule> List<CheckoutModule>.findFirstInstance(): T? {
    return this.firstOrNull { it is T } as? T
}
