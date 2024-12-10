package io.primer.android.sandboxProcessor.descriptors.base

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.SDKCapability
import io.primer.android.paymentmethods.VaultCapability

internal abstract class SandboxProcessorPaymentMethodDescriptor(
    config: PaymentMethodConfigDataResponse,
    localConfig: PrimerConfig
) : PaymentMethodDescriptor(config, localConfig) {
    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)

    override val vaultCapability = VaultCapability.SINGLE_USE_ONLY

    override val headlessDefinition: HeadlessDefinition?
        get() = null
}
