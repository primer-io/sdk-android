package io.primer.android.nolpay

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.SDKCapability
import io.primer.android.paymentmethods.VaultCapability

internal class NolPayDescriptor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config, localConfig) {
    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_ONLY
    override val headlessDefinition: HeadlessDefinition =
        HeadlessDefinition(
            listOf(
                PrimerPaymentMethodManagerCategory.NOL_PAY,
            ),
        )

    override val sdkCapabilities: List<SDKCapability> = listOf(SDKCapability.HEADLESS)
}
