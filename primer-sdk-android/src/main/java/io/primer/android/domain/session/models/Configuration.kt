package io.primer.android.domain.session.models

import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.ClientSessionResponse

internal data class Configuration(
    val paymentMethods: List<PaymentMethodConfig>,
    val clientSession: ClientSession?,
    val checkoutModules: List<CheckoutModule>
)

internal data class PaymentMethodConfig(val type: String)

internal data class ClientSession(val paymentMethod: ClientSessionResponse.PaymentMethod?)

internal data class CheckoutModule(val type: CheckoutModuleType, val options: Map<String, Boolean>?)
