package io.primer.android.configuration.data.model

import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.toCheckoutModule
import io.primer.android.displayMetadata.domain.model.IconDisplayMetadata

data class ConfigurationData(
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
