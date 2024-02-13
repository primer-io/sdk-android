package io.primer.android.domain.session.models

import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.ClientSessionDataResponse
import io.primer.android.data.configuration.models.Environment

internal data class Configuration(
    val environment: Environment,
    val paymentMethods: List<PaymentMethodConfig>,
    val clientSession: ClientSession,
    val checkoutModules: List<CheckoutModule>
)

internal data class PaymentMethodConfig(val type: String)

internal data class ClientSession(
    val clientSessionDataResponse: ClientSessionDataResponse
)

internal data class CheckoutModule(val type: CheckoutModuleType, val options: Map<String, Boolean>?)
