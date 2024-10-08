package io.primer.android.domain.session.models

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.isEnabled
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.ClientSessionDataResponse
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfigOptions
import io.primer.android.data.configuration.models.ShippingMethod

internal data class Configuration(
    val environment: Environment,
    val paymentMethods: List<PaymentMethodConfig>,
    val clientSession: ClientSession,
    val checkoutModules: List<CheckoutModule>
)

internal data class PaymentMethodConfig(
    val type: String,
    val options: PaymentMethodRemoteConfigOptions?
)

internal data class ClientSession(
    val clientSessionDataResponse: ClientSessionDataResponse
)

internal sealed class CheckoutModule {
    data class BillingAddress(val options: Map<String, Boolean>?) : CheckoutModule()
    data class CardInformation(val options: Map<String, Boolean>?) : CheckoutModule()
    data class Shipping(val shippingMethods: List<ShippingMethod>, val selectedMethod: String?) : CheckoutModule()
    data object Unknown : CheckoutModule()
}

internal fun CheckoutModuleDataResponse.toCheckoutModule() = when (type) {
    CheckoutModuleType.BILLING_ADDRESS -> CheckoutModule.BillingAddress(options)
    CheckoutModuleType.CARD_INFORMATION -> CheckoutModule.CardInformation(options)
    CheckoutModuleType.SHIPPING -> {
        CheckoutModule.Shipping(
            shippingMethods = shippingOptions?.shippingMethods ?: emptyList(),
            selectedMethod = shippingOptions?.selectedShippingMethod
        )
    }
    CheckoutModuleType.UNKNOWN -> CheckoutModule.Unknown
}

internal fun CheckoutModule.CardInformation?.isCardHolderNameEnabled() = when {
    this == null -> true
    options.isNullOrEmpty() -> true
    options.isEnabled(PrimerInputElementType.ALL) -> true
    options.isEnabled(PrimerInputElementType.CARDHOLDER_NAME) -> true
    else -> false
}

internal inline fun <reified T : CheckoutModule> List<CheckoutModule>.findFirstInstance(): T? {
    return this.firstOrNull { it is T } as? T
}
