package io.primer.android.paymentmethods

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse

abstract class PaymentMethodDescriptor(
    val config: PaymentMethodConfigDataResponse,
    val localConfig: PrimerConfig
) {

    abstract val vaultCapability: VaultCapability

    abstract val headlessDefinition: HeadlessDefinition?

    open val sdkCapabilities = listOf(SDKCapability.HEADLESS, SDKCapability.DROP_IN)
}
