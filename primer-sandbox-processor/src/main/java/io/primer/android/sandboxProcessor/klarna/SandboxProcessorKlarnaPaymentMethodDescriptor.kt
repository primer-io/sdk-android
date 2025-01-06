package io.primer.android.sandboxProcessor.klarna

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.sandboxProcessor.descriptors.base.SandboxProcessorPaymentMethodDescriptor

internal class SandboxProcessorKlarnaPaymentMethodDescriptor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : SandboxProcessorPaymentMethodDescriptor(config, localConfig) {
    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.KLARNA))
}
