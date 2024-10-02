package io.primer.android.data.configuration.models

import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.domain.session.models.Configuration
import io.primer.android.domain.session.models.toCheckoutModule

internal data class ConfigurationData(
    val pciUrl: String,
    val coreUrl: String,
    val binDataUrl: String,
    val assetsUrl: String,
    val paymentMethods: List<PaymentMethodConfigDataResponse>,
    val checkoutModules: List<CheckoutModuleDataResponse> = listOf(),
    val keys: ConfigurationKeysDataResponse?,
    val clientSession: ClientSessionDataResponse,
    val environment: Environment,
    val primerAccountId: String?,
    val iconsDisplayMetadata: List<Map<String, List<IconDisplayMetadata>>>
) {
    fun toConfiguration() = Configuration(
        environment,
        paymentMethods.map { it.toPaymentMethodConfig() },
        clientSession.toClientSession(),
        checkoutModules.map { it.toCheckoutModule() }
    )
}
