package io.primer.android.data.configuration.models

import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.domain.session.models.Configuration

internal data class ConfigurationData(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodConfigDataResponse>,
    val checkoutModules: List<CheckoutModuleDataResponse> = listOf(),
    val keys: ConfigurationKeys?,
    val clientSession: ClientSessionResponse?,
    val environment: Environment,
    val primerAccountId: String?,
    val iconsDisplayMetadata: List<Map<String, List<IconDisplayMetadata>>>
) {
    fun toConfiguration() = Configuration(
        paymentMethods.map { it.toPaymentMethodConfig() },
        clientSession?.toClientSession(),
        checkoutModules.map { it.toCheckoutModule() }
    )
}
